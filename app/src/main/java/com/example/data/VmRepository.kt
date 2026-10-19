package com.example.data

import com.example.model.RootMode
import com.example.model.VmBackup
import com.example.model.VmInstance
import com.example.model.VmStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class VmRepository {

    private val _vms = MutableStateFlow<List<VmInstance>>(emptyList())
    val vms: StateFlow<List<VmInstance>> = _vms.asStateFlow()

    private val _backups = MutableStateFlow<List<VmBackup>>(emptyList())
    val backups: StateFlow<List<VmBackup>> = _backups.asStateFlow()

    init {
        seedInitialVms()
    }

    private fun seedInitialVms() {
        val initialList = listOf(
            VmInstance(
                id = "vm_android_17_gms",
                name = "Android 17 Baklava Lab",
                osVersion = "17.0",
                apiLevel = 37,
                cpuCores = 8,
                ramMb = 6144,
                storageUsedMb = 2450,
                maxStorageMb = 65536,
                resolutionWidth = 1080,
                resolutionHeight = 2400,
                dpi = 420,
                rootMode = RootMode.MAGISK,
                xposedEnabled = true,
                gmsEnabled = true,
                supportA32 = false,
                supportA64 = true,
                romUris = listOf("system_17_next_arm64_gms.img?e=x&c=v2", "vendor_17_next.img?e=x&c=v2"),
                magiskUri = "asset:///plugins/magisk.zip?h=false",
                suUri = "asset:///plugins/superuser.zip?h=false",
                xposedUri = "asset:///plugins/xposed.zip",
                playUri = "asset:///plugins/play.zip",
                createdAt = System.currentTimeMillis() - 86400000L * 2
            ),
            VmInstance(
                id = "vm_android_14_upsidedown",
                name = "Android 14 GSI GMS Official",
                osVersion = "14.0",
                apiLevel = 34,
                cpuCores = 4,
                ramMb = 4096,
                storageUsedMb = 1890,
                maxStorageMb = 32768,
                resolutionWidth = 1080,
                resolutionHeight = 2340,
                dpi = 400,
                rootMode = RootMode.MAGISK,
                xposedEnabled = true,
                gmsEnabled = true,
                supportA32 = false,
                supportA64 = true,
                romUris = listOf("system_u_arm64_gms.img?e=x&c=v2", "vendor_u.img?e=x&c=v2"),
                magiskUri = "asset:///plugins/magisk.zip?h=false",
                suUri = "asset:///plugins/superuser.zip?h=false",
                xposedUri = "asset:///plugins/xposed.zip",
                playUri = "asset:///plugins/play.zip",
                createdAt = System.currentTimeMillis() - 86400000L * 5
            ),
            VmInstance(
                id = "vm_android_9_pie",
                name = "Android 9.0 Pie (Dual ABI A32+A64)",
                osVersion = "9.0",
                apiLevel = 28,
                cpuCores = 4,
                ramMb = 3072,
                storageUsedMb = 1420,
                maxStorageMb = 16384,
                resolutionWidth = 1080,
                resolutionHeight = 1920,
                dpi = 420,
                rootMode = RootMode.SUPERUSER,
                xposedEnabled = true,
                gmsEnabled = true,
                supportA32 = true,
                supportA64 = true,
                romUris = listOf("system_pie_arm64_gms.img?e=x&c=v2", "vendor_pie.img?e=x&c=v2"),
                magiskUri = "asset:///plugins/magisk.zip?h=false",
                suUri = "asset:///plugins/superuser.zip?h=false",
                xposedUri = "asset:///plugins/xposed.zip",
                playUri = "asset:///plugins/play.zip",
                createdAt = System.currentTimeMillis() - 86400000L * 10
            ),
            VmInstance(
                id = "vm_android_4_4w_kitkat",
                name = "Android 4.4W KitKat Lite GMS",
                osVersion = "4.4W",
                apiLevel = 20,
                cpuCores = 2,
                ramMb = 1024,
                storageUsedMb = 780,
                maxStorageMb = 8192,
                resolutionWidth = 720,
                resolutionHeight = 1280,
                dpi = 320,
                rootMode = RootMode.SUPERUSER,
                xposedEnabled = false,
                gmsEnabled = true,
                supportA32 = true,
                supportA64 = false,
                romUris = listOf("system_a44w_gsi.img?e=x&c=v2", "boot_44w.img?e=x&c=v2"),
                magiskUri = "asset:///plugins/magisk_legacy.zip?h=false",
                suUri = "asset:///plugins/superuser.zip?h=false",
                xposedUri = "asset:///plugins/xposed_kitkat.zip",
                playUri = "asset:///plugins/play_microg_44w.zip",
                createdAt = System.currentTimeMillis() - 86400000L * 15
            )
        )
        _vms.value = initialList

        _backups.value = listOf(
            VmBackup(
                id = "bak_seed_01",
                vmId = "vm_android_14_upsidedown",
                vmName = "Android 14 GSI GMS Official",
                osVersion = "14.0",
                timestamp = System.currentTimeMillis() - 86400000L,
                sizeMb = 2310,
                checksum = "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                note = "Clean snapshot before Magisk module tests"
            )
        )
    }

    fun addVm(vm: VmInstance) {
        _vms.value = listOf(vm) + _vms.value
    }

    fun updateVm(updated: VmInstance) {
        _vms.value = _vms.value.map { if (it.id == updated.id) updated else it }
    }

    fun deleteVm(vmId: String) {
        _vms.value = _vms.value.filter { it.id != vmId }
    }

    fun cloneVm(vm: VmInstance): VmInstance {
        val newId = "vm_" + UUID.randomUUID().toString().substring(0, 8)
        val clone = vm.copy(
            id = newId,
            name = "${vm.name} (Clone)",
            createdAt = System.currentTimeMillis(),
            lastBootTime = 0L,
            status = VmStatus.OFF
        )
        addVm(clone)
        return clone
    }

    fun addBackup(backup: VmBackup) {
        _backups.value = listOf(backup) + _backups.value
    }

    fun deleteBackup(backupId: String) {
        _backups.value = _backups.value.filter { it.id != backupId }
    }
}
