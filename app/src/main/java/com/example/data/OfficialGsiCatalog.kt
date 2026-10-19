package com.example.data

import com.example.model.RomManifest

object OfficialGsiCatalog {

    val catalog: List<RomManifest> = listOf(
        RomManifest(
            id = "rom_official_4_4w",
            displayName = "Android 4.4W KitKat GMS (Wear/Lite Edition)",
            romVersion = 1,
            osVersion = "4.4W",
            supportA32 = true,
            supportA64 = false,
            minimumSdkInt = 21,
            romUri = listOf("system_a44w_gsi.img?e=x&c=v2", "boot_44w.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk_legacy.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed_kitkat.zip",
            playUri = "asset:///plugins/play_microg_44w.zip"
        ),
        RomManifest(
            id = "rom_official_5_1",
            displayName = "Android 5.1 Lollipop Official AOSP GMS",
            romVersion = 2,
            osVersion = "5.1",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 22,
            romUri = listOf("system_51_gsi.img?e=x&c=v2", "vendor_51.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed_v89.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_7_1",
            displayName = "Android 7.1.2 Nougat Google GMS GSI",
            romVersion = 3,
            osVersion = "7.1",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 24,
            romUri = listOf("system_71_gsi.img?e=x&c=v2", "vendor_71.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_9_0",
            displayName = "Android 9.0 Pie Google GSI Official (v114)",
            romVersion = 4,
            osVersion = "9.0",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 26,
            romUri = listOf("system_pie_arm64_gms.img?e=x&c=v2", "vendor_pie.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_10_0",
            displayName = "Android 10.0 Quince Tart Official GSI GMS",
            romVersion = 5,
            osVersion = "10.0",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 28,
            romUri = listOf("system_q_arm64_gms.img?e=x&c=v2", "vendor_q.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_11_0",
            displayName = "Android 11.0 Red Velvet Official GSI GMS",
            romVersion = 6,
            osVersion = "11.0",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 28,
            romUri = listOf("system_r_arm64_gms.img?e=x&c=v2", "vendor_r.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_12_0",
            displayName = "Android 12.0 Snow Cone Official GSI GMS",
            romVersion = 7,
            osVersion = "12.0",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 28,
            romUri = listOf("system_s_arm64_gms.img?e=x&c=v2", "vendor_s.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_13_0",
            displayName = "Android 13.0 Tiramisu Official GSI GMS",
            romVersion = 8,
            osVersion = "13.0",
            supportA32 = true,
            supportA64 = true,
            minimumSdkInt = 28,
            romUri = listOf("system_t_arm64_gms.img?e=x&c=v2", "vendor_t.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_14_0",
            displayName = "Android 14.0 Upside Down Cake GSI GMS",
            romVersion = 9,
            osVersion = "14.0",
            supportA32 = false,
            supportA64 = true,
            minimumSdkInt = 28,
            romUri = listOf("system_u_arm64_gms.img?e=x&c=v2", "vendor_u.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_15_0",
            displayName = "Android 15.0 Vanilla Ice Cream GSI GMS",
            romVersion = 10,
            osVersion = "15.0",
            supportA32 = false,
            supportA64 = true,
            minimumSdkInt = 29,
            romUri = listOf("system_v_arm64_gms.img?e=x&c=v2", "vendor_v.img?e=x&c=v2", "product_v.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_16_0",
            displayName = "Android 16.0 Baklava Preview GSI GMS",
            romVersion = 11,
            osVersion = "16.0",
            supportA32 = false,
            supportA64 = true,
            minimumSdkInt = 30,
            romUri = listOf("system_baklava_arm64_gms.img?e=x&c=v2", "vendor_baklava.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        ),
        RomManifest(
            id = "rom_official_17_0",
            displayName = "Android 17.0 Experimental Next-Gen GSI GMS",
            romVersion = 12,
            osVersion = "17.0",
            supportA32 = false,
            supportA64 = true,
            minimumSdkInt = 30,
            romUri = listOf("system_17_next_arm64_gms.img?e=x&c=v2", "vendor_17_next.img?e=x&c=v2"),
            magiskUri = "asset:///plugins/magisk.zip?h=false",
            suUri = "asset:///plugins/superuser.zip?h=false",
            xposedUri = "asset:///plugins/xposed.zip",
            playUri = "asset:///plugins/play.zip"
        )
    )

    fun getApiLevelForVersion(osVersion: String): Int {
        return when (osVersion) {
            "4.4W" -> 20
            "5.1" -> 22
            "7.1" -> 25
            "9.0" -> 28
            "10.0", "10" -> 29
            "11.0", "11" -> 30
            "12.0", "12" -> 31
            "13.0", "13" -> 33
            "14.0", "14" -> 34
            "15.0", "15" -> 35
            "16.0", "16" -> 36
            "17.0", "17" -> 37
            else -> 34
        }
    }
}
