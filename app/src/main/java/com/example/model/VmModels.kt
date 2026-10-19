package com.example.model

enum class RootMode {
    NONE,
    SUPERUSER,
    MAGISK
}

enum class VmStatus {
    OFF,
    BOOTING,
    RUNNING,
    PAUSED,
    REPAIRING,
    BACKING_UP,
    RESTORING,
    SHUTTING_DOWN,
    ERROR
}

data class GuestApp(
    val packageName: String,
    val name: String,
    val iconCategory: String, // "play", "magisk", "su", "terminal", "settings", "files", "browser", "custom"
    val isSystemApp: Boolean = false,
    val versionName: String = "1.0",
    val description: String = ""
)

data class VmBackup(
    val id: String,
    val vmId: String,
    val vmName: String,
    val osVersion: String,
    val timestamp: Long,
    val sizeMb: Long,
    val checksum: String,
    val note: String
)

data class VmInstance(
    val id: String,
    val name: String,
    val osVersion: String, // "4.4W", "5.1", "7.1", "9.0", "10", "11", "12", "13", "14", "15", "16", "17"
    val apiLevel: Int,
    val cpuCores: Int = 4,
    val ramMb: Int = 3072,
    val storageUsedMb: Long = 1840,
    val maxStorageMb: Long = 32768,
    val resolutionWidth: Int = 1080,
    val resolutionHeight: Int = 2400,
    val dpi: Int = 420,
    val rootMode: RootMode = RootMode.MAGISK,
    val xposedEnabled: Boolean = true,
    val gmsEnabled: Boolean = true,
    val supportA32: Boolean = true,
    val supportA64: Boolean = true,
    val romUris: List<String> = listOf("system.img?e=x&c=v2", "vendor.img?e=x&c=v2"),
    val magiskUri: String? = "asset:///plugins/magisk.zip?h=false",
    val suUri: String? = "asset:///plugins/superuser.zip?h=false",
    val xposedUri: String? = "asset:///plugins/xposed.zip",
    val playUri: String? = "asset:///plugins/play.zip",
    val createdAt: Long = System.currentTimeMillis(),
    val lastBootTime: Long = 0L,
    val status: VmStatus = VmStatus.OFF
)
