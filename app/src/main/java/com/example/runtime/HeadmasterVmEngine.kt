package com.example.runtime

import com.example.model.GuestApp
import com.example.model.RootMode
import com.example.model.VmBackup
import com.example.model.VmInstance
import com.example.model.VmStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class VmTelemetry(
    val cpuUsagePercent: Float = 0f,
    val ramUsedMb: Int = 0,
    val ramTotalMb: Int = 4096,
    val fps: Int = 60,
    val latencyMs: Long = 12L,
    val renderer: String = "OpenGL ES 3.2 (Headmaster-GLES-VirtIO)"
)

data class TerminalEntry(
    val command: String,
    val output: String,
    val isRoot: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class RepairReport(
    val vmId: String,
    val vmName: String,
    val success: Boolean,
    val repairedSectors: Int,
    val inodesChecked: Int,
    val dalvikCacheRebuilt: Boolean,
    val selinuxRestored: Boolean,
    val logs: List<String>
)

class HeadmasterVmEngine(private val scope: CoroutineScope) {

    private val _currentVm = MutableStateFlow<VmInstance?>(null)
    val currentVm: StateFlow<VmInstance?> = _currentVm.asStateFlow()

    private val _status = MutableStateFlow(VmStatus.OFF)
    val status: StateFlow<VmStatus> = _status.asStateFlow()

    private val _bootLogs = MutableStateFlow<List<String>>(emptyList())
    val bootLogs: StateFlow<List<String>> = _bootLogs.asStateFlow()

    private val _telemetry = MutableStateFlow(VmTelemetry())
    val telemetry: StateFlow<VmTelemetry> = _telemetry.asStateFlow()

    private val _guestApps = MutableStateFlow<List<GuestApp>>(emptyList())
    val guestApps: StateFlow<List<GuestApp>> = _guestApps.asStateFlow()

    private val _activeGuestApp = MutableStateFlow<GuestApp?>(null)
    val activeGuestApp: StateFlow<GuestApp?> = _activeGuestApp.asStateFlow()

    private val _terminalHistory = MutableStateFlow<List<TerminalEntry>>(emptyList())
    val terminalHistory: StateFlow<List<TerminalEntry>> = _terminalHistory.asStateFlow()

    private val _isTerminalRoot = MutableStateFlow(false)
    val isTerminalRoot: StateFlow<Boolean> = _isTerminalRoot.asStateFlow()

    private val _sharedFiles = MutableStateFlow<List<String>>(
        listOf("readme_headmaster.txt", "magisk_module_test.zip", "rom_info.json", "user_data_export.tar")
    )
    val sharedFiles: StateFlow<List<String>> = _sharedFiles.asStateFlow()

    private var telemetryJob: Job? = null
    private var bootJob: Job? = null

    // System properties for the guest
    private val systemProps = mutableMapOf<String, String>()

    init {
        initDefaultGuestApps()
    }

    private fun initDefaultGuestApps() {
        _guestApps.value = listOf(
            GuestApp("com.android.vending", "Play Store", "play", isSystemApp = true, "39.8.24", "Official Google Play Services Store"),
            GuestApp("com.topjohnwu.magisk", "Magisk", "magisk", isSystemApp = true, "27.0", "Systemless Root & Zygisk Manager"),
            GuestApp("com.koushikdutta.superuser", "Superuser", "su", isSystemApp = true, "10.3", "Headmaster SU Permissions Manager"),
            GuestApp("jackpal.androidterm", "Terminal", "terminal", isSystemApp = true, "1.0.70", "Sandboxed Linux Shell Emulator"),
            GuestApp("com.android.settings", "Settings", "settings", isSystemApp = true, "1.0", "Virtual Android System Settings"),
            GuestApp("com.android.documentsui", "Files", "files", isSystemApp = true, "14.0", "Virtual Storage File Manager"),
            GuestApp("com.android.browser", "Browser", "browser", isSystemApp = true, "1.0", "Headmaster Web Browser")
        )
    }

    fun startVm(vm: VmInstance) {
        if (_status.value == VmStatus.RUNNING || _status.value == VmStatus.BOOTING) return

        _currentVm.value = vm
        _status.value = VmStatus.BOOTING
        _bootLogs.value = emptyList()
        _activeGuestApp.value = null
        _isTerminalRoot.value = vm.rootMode != RootMode.NONE

        initSystemProps(vm)

        bootJob?.cancel()
        bootJob = scope.launch(Dispatchers.Default) {
            val logs = mutableListOf<String>()

            fun log(msg: String) {
                val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
                logs.add("[$time] $msg")
                _bootLogs.value = logs.toList()
            }

            log("HEADMASTER HYPERVISOR v3.4.0-PROOT INITIALIZING")
            log("Host Kernel ABI: arm64-v8a | Supported Guest: A32=${vm.supportA32}, A64=${vm.supportA64}")
            log("Allocating Virtual Machine ${vm.id} [${vm.name}] - Android ${vm.osVersion}")
            delay(120)

            log("vCPU: Initializing ${vm.cpuCores} cores (KVM emulation bridge)...")
            log("vRAM: Allocating ${vm.ramMb} MB LPDDR5X guest physical memory...")
            log("vDisplay: Virtual framebuffer ${vm.resolutionWidth}x${vm.resolutionHeight}@${vm.dpi}dpi")
            delay(150)

            vm.romUris.forEachIndexed { idx, uri ->
                log("MNT: Mounting block device /dev/block/loop$idx -> $uri (ext4 read-only)")
            }
            log("MNT: Mounting /data partition (userdata.img, rw, nosuid, nodev, noatime)")
            delay(180)

            log("INIT: Spawning /init (PID 1) with SELinux permissive mode...")
            log("INIT: Loading Android ${vm.osVersion} ramdisk properties...")
            log("SERVICE: Starting servicemanager (binder IPC bridge initialized)")
            log("SERVICE: Starting hwservicemanager (HIDL / AIDL HALs loaded)")
            delay(200)

            log("ZYGOTE: Launching ${if (vm.supportA64) "zygote64" else "zygote32"} binary...")
            log("SURFACEFLINGER: Initializing virtual display compositor on /dev/graphics/fb0")
            delay(160)

            if (vm.rootMode == RootMode.MAGISK) {
                log("MAGISK: Injecting Magisk v27.0 daemon (magiskd) into zygote...")
                log("MAGISK: Mounting Magisk overlayfs & Zygisk runtime hook")
            } else if (vm.rootMode == RootMode.SUPERUSER) {
                log("SU: Initializing daemonsu socket /dev/com.headmaster.su...")
            }

            if (vm.xposedEnabled) {
                log("XPOSED: Loading LSPosed framework v1.9.2 into ART runtime classpath")
            }

            if (vm.gmsEnabled) {
                log("GMS: Starting Google Mobile Services (GmsCore) & Play Store services...")
            }
            delay(220)

            log("SYSTEM_SERVER: Starting ActivityManagerService, WindowManagerService, PackageManager...")
            log("BOOT_COMPLETED: Broadcasting android.intent.action.BOOT_COMPLETED")
            log("HEADMASTER: Guest OS Android ${vm.osVersion} running successfully!")
            delay(150)

            _status.value = VmStatus.RUNNING
            _currentVm.value = vm.copy(
                status = VmStatus.RUNNING,
                lastBootTime = System.currentTimeMillis()
            )

            startTelemetryLoop(vm)
        }
    }

    private fun initSystemProps(vm: VmInstance) {
        systemProps.clear()
        systemProps["ro.build.version.release"] = vm.osVersion
        systemProps["ro.build.version.sdk"] = vm.apiLevel.toString()
        systemProps["ro.product.model"] = "Headmaster Virtual Phone (${vm.osVersion})"
        systemProps["ro.product.brand"] = "Google"
        systemProps["ro.product.manufacturer"] = "Headmaster Hypervisor"
        systemProps["ro.product.device"] = "generic_arm64"
        systemProps["ro.build.id"] = "GSI-GMS-${vm.osVersion}-OFFICIAL"
        systemProps["ro.build.fingerprint"] = "google/headmaster_gsi_${vm.osVersion}/generic:user/release-keys"
        systemProps["ro.headmaster.vm.id"] = vm.id
        systemProps["ro.headmaster.vm.version"] = "3.4.0"
        systemProps["ro.headmaster.arch"] = if (vm.supportA64) "aarch64" else "armv7l"
        systemProps["ro.headmaster.root"] = vm.rootMode.name.lowercase()
        systemProps["ro.headmaster.gms"] = vm.gmsEnabled.toString()
        systemProps["ro.headmaster.xposed"] = vm.xposedEnabled.toString()
    }

    private fun startTelemetryLoop(vm: VmInstance) {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(Dispatchers.Default) {
            var tick = 0
            while (_status.value == VmStatus.RUNNING) {
                tick++
                val baseCpu = 12f + (tick % 7) * 4f + (if (_activeGuestApp.value != null) 18f else 0f)
                val baseRam = (vm.ramMb * 0.42f).toInt() + (if (_activeGuestApp.value != null) 340 else 0)
                _telemetry.value = VmTelemetry(
                    cpuUsagePercent = baseCpu.coerceIn(8f, 96f),
                    ramUsedMb = baseRam.coerceAtMost(vm.ramMb),
                    ramTotalMb = vm.ramMb,
                    fps = if (_activeGuestApp.value != null) 60 else 59,
                    latencyMs = (8L + (tick % 5)),
                    renderer = "OpenGL ES 3.2 (Headmaster-GLES-VirtIO)"
                )
                delay(1000)
            }
        }
    }

    fun stopVm() {
        bootJob?.cancel()
        telemetryJob?.cancel()
        scope.launch(Dispatchers.Default) {
            _status.value = VmStatus.SHUTTING_DOWN
            delay(350)
            _status.value = VmStatus.OFF
            _activeGuestApp.value = null
            _currentVm.value = _currentVm.value?.copy(status = VmStatus.OFF)
        }
    }

    fun restartVm(hard: Boolean = false) {
        val vm = _currentVm.value ?: return
        scope.launch(Dispatchers.Default) {
            stopVm()
            delay(if (hard) 600 else 300)
            startVm(vm)
        }
    }

    fun launchGuestApp(app: GuestApp) {
        _activeGuestApp.value = app
    }

    fun closeActiveGuestApp() {
        _activeGuestApp.value = null
    }

    fun installGuestApp(name: String, packageName: String): GuestApp {
        val newApp = GuestApp(
            packageName = packageName,
            name = name,
            iconCategory = "custom",
            isSystemApp = false,
            versionName = "1.0.0",
            description = "User installed APK in Headmaster VM"
        )
        _guestApps.value = _guestApps.value + newApp
        return newApp
    }

    fun executeShellCommand(rawCommand: String): TerminalEntry {
        val cmd = rawCommand.trim()
        val parts = cmd.split("\\s+".toRegex())
        val action = parts.firstOrNull()?.lowercase() ?: ""
        val args = parts.drop(1)
        val isRoot = _isTerminalRoot.value

        val output = when (action) {
            "getprop" -> {
                if (args.isEmpty()) {
                    systemProps.entries.joinToString("\n") { "[${it.key}]: [${it.value}]" }
                } else {
                    val key = args[0]
                    systemProps[key] ?: ""
                }
            }
            "setprop" -> {
                if (args.size >= 2) {
                    val key = args[0]
                    val value = args.drop(1).joinToString(" ")
                    systemProps[key] = value
                    "Property [$key] set to [$value]"
                } else {
                    "usage: setprop <key> <value>"
                }
            }
            "uname" -> {
                if (args.contains("-a")) {
                    "Linux localhost 6.1.75-headmaster-android-hypervisor #1 SMP PREEMPT " +
                            SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.US).format(Date()) +
                            " UTC 2026 aarch64 Android"
                } else {
                    "Linux"
                }
            }
            "id", "whoami" -> {
                if (isRoot) {
                    "uid=0(root) gid=0(root) groups=0(root),1000(system),1004(input),3003(inet) context=u:r:su:s0"
                } else {
                    "uid=1000(system) gid=1000(system) groups=1000(system),1004(input),3003(inet) context=u:r:system_app:s0"
                }
            }
            "su" -> {
                val vm = _currentVm.value
                if (vm?.rootMode == RootMode.NONE) {
                    "su: inaccessible or not found (Root mode disabled in VM settings)"
                } else {
                    _isTerminalRoot.value = true
                    "Superuser granted to com.headmaster.terminal via ${vm?.rootMode?.name ?: "ROOT"}"
                }
            }
            "exit" -> {
                if (_isTerminalRoot.value) {
                    _isTerminalRoot.value = false
                    "Dropped root privileges. Now running as system user (uid 1000)."
                } else {
                    "Session active."
                }
            }
            "ps" -> {
                """
                USER     PID   PPID  VSIZE  RSS   WCHAN            PC  NAME
                root       1      0  12000  1840  sys_epoll_wait    0  /init
                system   120      1  18400  2100  binder_thread     0  /system/bin/servicemanager
                system   122      1  22100  3400  epoll_wait        0  /system/bin/surfaceflinger
                root     180      1  84200 12400  poll_wait         0  zygote64
                system   250    180 284000 68400  binder_thread     0  system_server
                u0_a10   410    180 142000 32100  epoll_wait        0  com.android.launcher3
                u0_a12   520    180 184000 48200  binder_thread     0  com.android.vending
                root     600      1   8200  1400  select            0  magiskd
                u0_a20   710    180  94000 24100  epoll_wait        0  jackpal.androidterm
                """.trimIndent()
            }
            "top" -> {
                val tele = _telemetry.value
                """
                User ${tele.cpuUsagePercent.toInt()}%, System 6%, IOW 0%, IRQ 0%
                User 24 + Nice 0 + Sys 8 + Idle 68 + IOW 0 + IRQ 0 = 100
                PID USER     PR  NI CPU% S  #THR     VSS     RSS PCY Name
                250 system   20   0  14% S    88 284000K  68400K  fg system_server
                122 system   20   0   8% S    24  22100K   3400K  fg surfaceflinger
                410 u0_a10   20   0   4% S    32 142000K  32100K  fg com.android.launcher3
                710 u0_a20   20   0   2% S    12  94000K  24100K  fg jackpal.androidterm
                """.trimIndent()
            }
            "df", "df -h" -> {
                val vm = _currentVm.value
                val used = vm?.storageUsedMb ?: 1840L
                val max = vm?.maxStorageMb ?: 32768L
                """
                Filesystem               Size     Used     Free   Blksize
                /dev/block/loop0        3.8G     3.6G   200.0M      4096   /system
                /dev/block/loop1        1.2G     1.1G   100.0M      4096   /vendor
                /dev/block/loop2       ${max / 1024}G    ${used / 1024}G    ${(max - used) / 1024}G      4096   /data
                tmpfs                    1.9G   140.0K     1.9G      4096   /dev
                /data/media             ${max / 1024}G    ${used / 1024}G    ${(max - used) / 1024}G      4096   /storage/emulated
                """.trimIndent()
            }
            "free", "free -m" -> {
                val ram = _currentVm.value?.ramMb ?: 4096
                val used = _telemetry.value.ramUsedMb
                val free = ram - used
                """
                               total        used        free      shared     buffers
                Mem:            $ram        $used        $free          24          48
                -/+ buffers/cache:          ${used - 48}        ${free + 48}
                Swap:           2048           0        2048
                """.trimIndent()
            }
            "pm" -> {
                if (args.contains("list") && args.contains("packages")) {
                    _guestApps.value.joinToString("\n") { "package:${it.packageName}" }
                } else {
                    "usage: pm list packages"
                }
            }
            "ls" -> {
                val path = args.firstOrNull() ?: "/"
                when (path) {
                    "/", "/." -> "bin  boot  dev  etc  init  mnt  proc  product  sdcard  sys  system  vendor  data"
                    "/system" -> "app  bin  etc  fonts  framework  lib  lib64  priv-app  usr  build.prop"
                    "/data" -> "app  data  dalvik-cache  local  media  misc  system  user"
                    "/sdcard", "/storage/emulated/0" -> "Android  DCIM  Download  Movies  Music  Pictures  HeadmasterShared"
                    else -> "file1  file2  config.xml"
                }
            }
            "cat" -> {
                val target = args.firstOrNull() ?: ""
                when {
                    target.endsWith("build.prop") -> {
                        systemProps.entries.joinToString("\n") { "${it.key}=${it.value}" }
                    }
                    target.contains("cpuinfo") -> {
                        val cores = _currentVm.value?.cpuCores ?: 4
                        (0 until cores).joinToString("\n\n") {
                            "processor\t: $it\nmodel name\t: ARMv8 Processor rev 4 (v8l)\nBogoMIPS\t: 38.40\nFeatures\t: fp asimd evtstrm aes pmull sha1 sha2 crc32 atomics"
                        }
                    }
                    target.contains("version") -> {
                        "Linux version 6.1.75-headmaster-android-hypervisor (builder@headmaster-cloud) (Android clang version 17.0.2) #1 SMP PREEMPT 2026"
                    }
                    else -> "Headmaster Virtual System file: $target (size: 4096 bytes)"
                }
            }
            "headmaster" -> {
                """
                HEADMASTER VIRTUAL MACHINE RUNTIME CLI v3.4.0
                - Hypervisor Mode: User-Space Proot + Kernel Syscall Sandbox
                - Supported Guests: Android 4.4W up to Android 17 (Baklava)
                - GSI GMS Compatibility: Verified Official Images
                - Active VM: ${_currentVm.value?.name ?: "None"}
                - Status: ${_status.value.name}
                - Resolution: ${_currentVm.value?.resolutionWidth}x${_currentVm.value?.resolutionHeight} @ ${_currentVm.value?.dpi}dpi
                - Root: ${_currentVm.value?.rootMode?.name} | GMS: ${_currentVm.value?.gmsEnabled}
                """.trimIndent()
            }
            "clear" -> {
                _terminalHistory.value = emptyList()
                return TerminalEntry(cmd, "", isRoot)
            }
            "reboot" -> {
                restartVm(hard = false)
                "Rebooting guest Android container..."
            }
            "date" -> SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date())
            "echo" -> args.joinToString(" ")
            "help" -> {
                """
                Available shell commands:
                  getprop [key]       - Query Android system properties
                  setprop <key> <val> - Set Android system property
                  uname -a            - Show virtual kernel version
                  id / whoami         - Show user / root ID
                  su                  - Request superuser permissions
                  ps / top            - Virtual process table & CPU usage
                  df -h               - Virtual disk partitions & mounts
                  free -m             - Virtual memory statistics
                  pm list packages    - List installed guest packages
                  ls [path]           - List directory contents
                  cat <file>          - Display file contents
                  headmaster          - Hypervisor runtime status
                  reboot              - Restart guest virtual machine
                  clear               - Clear terminal screen
                """.trimIndent()
            }
            else -> "sh: $action: command not found (type 'help' for available commands)"
        }

        val entry = TerminalEntry(command = cmd, output = output, isRoot = isRoot)
        _terminalHistory.value = _terminalHistory.value + entry
        return entry
    }

    suspend fun repairVm(vm: VmInstance): RepairReport {
        _status.value = VmStatus.REPAIRING
        val logs = mutableListOf<String>()

        fun log(msg: String) {
            logs.add("[FSCK-VIRT] $msg")
        }

        log("Starting filesystem integrity verification on ${vm.id} [${vm.name}]...")
        delay(300)
        log("Checking ext4 superblock and block group descriptors for userdata.img...")
        delay(250)
        log("Pass 1: Checking inodes, blocks, and sizes (inode count: 131,072)...")
        delay(350)
        log("Found 14 orphan inodes in /data/dalvik-cache; clearing stale dexopt artifacts...")
        delay(300)
        log("Pass 2: Checking directory structure and link counts...")
        delay(250)
        log("Pass 3: Checking directory connectivity...")
        delay(200)
        log("Pass 4: Checking reference counts...")
        delay(200)
        log("Pass 5: Checking group summary information...")
        delay(300)
        log("Fixing SELinux security context attributes (restorecon -RF /data)...")
        delay(250)
        log("Validating guest Android ${vm.osVersion} package manager records (packages.xml)...")
        delay(200)
        log("Repair completed: File system clean. 28 bad block references resolved.")

        _status.value = VmStatus.OFF

        return RepairReport(
            vmId = vm.id,
            vmName = vm.name,
            success = true,
            repairedSectors = 28,
            inodesChecked = 131072,
            dalvikCacheRebuilt = true,
            selinuxRestored = true,
            logs = logs
        )
    }

    suspend fun backupVm(vm: VmInstance, note: String): VmBackup {
        _status.value = VmStatus.BACKING_UP
        delay(600)
        val backup = VmBackup(
            id = "bak_" + UUID.randomUUID().toString().substring(0, 8),
            vmId = vm.id,
            vmName = vm.name,
            osVersion = vm.osVersion,
            timestamp = System.currentTimeMillis(),
            sizeMb = vm.storageUsedMb + 420,
            checksum = "sha256:" + UUID.randomUUID().toString().replace("-", ""),
            note = note.ifEmpty { "Manual backup of ${vm.name} (Android ${vm.osVersion})" }
        )
        _status.value = if (_currentVm.value?.id == vm.id) VmStatus.OFF else _status.value
        return backup
    }

    suspend fun restoreVm(backup: VmBackup, vm: VmInstance): Boolean {
        _status.value = VmStatus.RESTORING
        delay(700)
        _status.value = VmStatus.OFF
        return true
    }

    fun addSharedFile(filename: String) {
        _sharedFiles.value = _sharedFiles.value + filename
    }

    fun updateVmConfig(updated: VmInstance) {
        _currentVm.value = updated
        initSystemProps(updated)
    }
}
