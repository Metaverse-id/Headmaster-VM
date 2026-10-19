package com.example.model

/**
 * Manifest data structure matching the user's required manifest.json specification:
 * {
 *     "id": "rom_default_osversion",
 *     "display_name": "os_display_name",
 *     "rom_version": romversion,
 *     "os_version": osversion,
 *     "support_a32": false or true,
 *     "support_a64": false or true,
 *     "minimum_sdk_int": romsdk,
 *     "rom_uri": ["romfile.img?e=x&c=v2", "romfile1.img?e=x&c=v2"],
 *     "magisk_uri": "asset:///plugins/magisk.zip?h=false",
 *     "su_uri": "asset:///plugins/superuser.zip?h=false",
 *     "xposed_uri": "asset:///plugins/xposed.zip",
 *     "play_uri": "asset:///plugins/play.zip"
 * }
 */
data class RomManifest(
    val id: String,
    val displayName: String,
    val romVersion: Int,
    val osVersion: String,
    val supportA32: Boolean,
    val supportA64: Boolean,
    val minimumSdkInt: Int,
    val romUri: List<String>,
    val magiskUri: String? = null,
    val suUri: String? = null,
    val xposedUri: String? = null,
    val playUri: String? = null
)
