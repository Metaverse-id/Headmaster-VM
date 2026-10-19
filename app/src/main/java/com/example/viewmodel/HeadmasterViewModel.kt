package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OfficialGsiCatalog
import com.example.data.RomManifestParser
import com.example.data.VmRepository
import com.example.model.GuestApp
import com.example.model.RomManifest
import com.example.model.RootMode
import com.example.model.VmBackup
import com.example.model.VmInstance
import com.example.model.VmStatus
import com.example.runtime.HeadmasterVmEngine
import com.example.runtime.RepairReport
import com.example.runtime.TerminalEntry
import com.example.runtime.VmTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    DASHBOARD,
    RUNNING_VM,
    IMPORT_ROM,
    ROM_BUILDER,
    BACKUP_MANAGER,
    VM_SETTINGS
}

class HeadmasterViewModel(
    val repository: VmRepository = VmRepository()
) : ViewModel() {

    val engine = HeadmasterVmEngine(viewModelScope)

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedVmForSettings = MutableStateFlow<VmInstance?>(null)
    val selectedVmForSettings: StateFlow<VmInstance?> = _selectedVmForSettings.asStateFlow()

    private val _lastRepairReport = MutableStateFlow<RepairReport?>(null)
    val lastRepairReport: StateFlow<RepairReport?> = _lastRepairReport.asStateFlow()

    private val _showRepairDialog = MutableStateFlow(false)
    val showRepairDialog: StateFlow<Boolean> = _showRepairDialog.asStateFlow()

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()

    private val _activeVmForAction = MutableStateFlow<VmInstance?>(null)
    val activeVmForAction: StateFlow<VmInstance?> = _activeVmForAction.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Hypervisor global stats
    val vms = repository.vms
    val backups = repository.backups
    val currentRunningVm = engine.currentVm
    val vmStatus = engine.status
    val bootLogs = engine.bootLogs
    val telemetry: StateFlow<VmTelemetry> = engine.telemetry
    val guestApps = engine.guestApps
    val activeGuestApp = engine.activeGuestApp
    val terminalHistory: StateFlow<List<TerminalEntry>> = engine.terminalHistory
    val isTerminalRoot = engine.isTerminalRoot
    val sharedFiles = engine.sharedFiles

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun startVm(vm: VmInstance) {
        engine.startVm(vm)
        repository.updateVm(vm.copy(status = VmStatus.RUNNING, lastBootTime = System.currentTimeMillis()))
        _currentScreen.value = AppScreen.RUNNING_VM
    }

    fun stopVm() {
        engine.stopVm()
    }

    fun restartVm(hard: Boolean = false) {
        engine.restartVm(hard)
    }

    fun openSettings(vm: VmInstance) {
        _selectedVmForSettings.value = vm
        _currentScreen.value = AppScreen.VM_SETTINGS
    }

    fun saveVmSettings(updated: VmInstance) {
        repository.updateVm(updated)
        if (engine.currentVm.value?.id == updated.id) {
            engine.updateVmConfig(updated)
        }
        _userFeedbackMessage.value = "Settings saved for ${updated.name}"
        _currentScreen.value = AppScreen.DASHBOARD
    }

    fun cloneVm(vm: VmInstance) {
        val clone = repository.cloneVm(vm)
        _userFeedbackMessage.value = "VM Cloned successfully: ${clone.name}"
    }

    fun deleteVm(vm: VmInstance) {
        if (engine.currentVm.value?.id == vm.id) {
            engine.stopVm()
        }
        repository.deleteVm(vm.id)
        _userFeedbackMessage.value = "VM Deleted: ${vm.name}"
    }

    fun triggerRepairDialog(vm: VmInstance) {
        _activeVmForAction.value = vm
        _showRepairDialog.value = true
    }

    fun executeRepair() {
        val vm = _activeVmForAction.value ?: return
        viewModelScope.launch {
            val report = engine.repairVm(vm)
            _lastRepairReport.value = report
        }
    }

    fun closeRepairDialog() {
        _showRepairDialog.value = false
        _lastRepairReport.value = null
        _activeVmForAction.value = null
    }

    fun triggerBackupDialog(vm: VmInstance) {
        _activeVmForAction.value = vm
        _showBackupDialog.value = true
    }

    fun createBackup(note: String) {
        val vm = _activeVmForAction.value ?: return
        viewModelScope.launch {
            val backup = engine.backupVm(vm, note)
            repository.addBackup(backup)
            _showBackupDialog.value = false
            _userFeedbackMessage.value = "Backup created: ${backup.id} (${backup.sizeMb} MB)"
        }
    }

    fun closeBackupDialog() {
        _showBackupDialog.value = false
        _activeVmForAction.value = null
    }

    fun restoreVmFromBackup(backup: VmBackup) {
        val targetVm = repository.vms.value.find { it.id == backup.vmId }
        viewModelScope.launch {
            if (targetVm != null) {
                engine.restoreVm(backup, targetVm)
                _userFeedbackMessage.value = "VM ${targetVm.name} restored from snapshot ${backup.id}"
            } else {
                // If original VM was deleted, restore as a new VM instance!
                val newVm = VmInstance(
                    id = "vm_restored_" + UUID.randomUUID().toString().substring(0, 6),
                    name = "${backup.vmName} (Restored)",
                    osVersion = backup.osVersion,
                    apiLevel = OfficialGsiCatalog.getApiLevelForVersion(backup.osVersion)
                )
                repository.addVm(newVm)
                _userFeedbackMessage.value = "Restored backup as new VM: ${newVm.name}"
            }
        }
    }

    fun deleteBackup(backup: VmBackup) {
        repository.deleteBackup(backup.id)
        _userFeedbackMessage.value = "Backup deleted: ${backup.id}"
    }

    fun importRomFromManifest(manifest: RomManifest): VmInstance {
        val newId = "vm_" + manifest.id.replace("[^a-zA-Z0-9_]".toRegex(), "_") + "_" + UUID.randomUUID().toString().substring(0, 4)
        val newVm = VmInstance(
            id = newId,
            name = manifest.displayName,
            osVersion = manifest.osVersion,
            apiLevel = OfficialGsiCatalog.getApiLevelForVersion(manifest.osVersion),
            supportA32 = manifest.supportA32,
            supportA64 = manifest.supportA64,
            romUris = manifest.romUri,
            magiskUri = manifest.magiskUri,
            suUri = manifest.suUri,
            xposedUri = manifest.xposedUri,
            playUri = manifest.playUri,
            rootMode = if (manifest.magiskUri != null) RootMode.MAGISK else if (manifest.suUri != null) RootMode.SUPERUSER else RootMode.NONE,
            xposedEnabled = manifest.xposedUri != null,
            gmsEnabled = manifest.playUri != null
        )
        repository.addVm(newVm)
        _userFeedbackMessage.value = "ROM Imported successfully: ${newVm.name}"
        _currentScreen.value = AppScreen.DASHBOARD
        return newVm
    }

    fun deployOfficialGsi(manifest: RomManifest) {
        importRomFromManifest(manifest)
    }

    fun executeTerminalCommand(cmd: String): TerminalEntry {
        return engine.executeShellCommand(cmd)
    }

    fun launchGuestApp(app: GuestApp) {
        engine.launchGuestApp(app)
    }

    fun closeActiveGuestApp() {
        engine.closeActiveGuestApp()
    }

    fun installApkToGuest(name: String, pkg: String) {
        val app = engine.installGuestApp(name, pkg)
        _userFeedbackMessage.value = "App installed inside VM: ${app.name}"
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun addSharedFile(name: String) {
        engine.addSharedFile(name)
        _userFeedbackMessage.value = "Shared file copied to guest /sdcard/HeadmasterShared/"
    }
}
