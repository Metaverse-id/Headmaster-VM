package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.OfficialGsiCatalog
import com.example.data.RomManifestParser
import com.example.model.RootMode
import com.example.model.VmInstance
import com.example.model.VmStatus
import com.example.runtime.HeadmasterVmEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context verifies Headmaster name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Headmaster", appName)
  }

  @Test
  fun `rom manifest parser parses user format specification successfully`() {
    val userJson = """
    {
        "id": "rom_default_osversion",
        "display_name": "os_display_name",
        "rom_version": 1,
        "os_version": "14.0",
        "support_a32": false,
        "support_a64": true,
        "minimum_sdk_int": 28,
        "rom_uri": ["romfile.img?e=x&c=v2", "romfile1.img?e=x&c=v2"], #ini boleh 1 rom file atau 2 rom file+
        "magisk_uri": "asset:///plugins/magisk.zip?h=false", #harus ada magisk.zip yang biss instal magisk
        "su_uri": "asset:///plugins/superuser.zip?h=false", #zip aplikasi untuk install aplikasi superuser
        "xposed_uri": "asset:///plugins/xposed.zip", #install exposed.zip
        "play_uri": "asset:///plugins/play.zip" #install PlayStore
    }
    """.trimIndent()

    val result = RomManifestParser.parseJson(userJson)
    assertTrue(result is RomManifestParser.ValidationResult.Success)

    val manifest = (result as RomManifestParser.ValidationResult.Success).manifest
    assertEquals("rom_default_osversion", manifest.id)
    assertEquals("os_display_name", manifest.displayName)
    assertEquals("14.0", manifest.osVersion)
    assertEquals(false, manifest.supportA32)
    assertEquals(true, manifest.supportA64)
    assertEquals(28, manifest.minimumSdkInt)
    assertEquals(2, manifest.romUri.size)
    assertEquals("romfile.img?e=x&c=v2", manifest.romUri[0])
    assertEquals("asset:///plugins/magisk.zip?h=false", manifest.magiskUri)
    assertEquals("asset:///plugins/superuser.zip?h=false", manifest.suUri)
    assertEquals("asset:///plugins/xposed.zip", manifest.xposedUri)
    assertEquals("asset:///plugins/play.zip", manifest.playUri)
  }

  @Test
  fun `catalog contains Android 4_4W and Android 17`() {
    val catalog = OfficialGsiCatalog.catalog
    val has44w = catalog.any { it.osVersion == "4.4W" }
    val has17 = catalog.any { it.osVersion == "17.0" }
    assertTrue(has44w)
    assertTrue(has17)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `vm engine shell execution works`() = runTest {
    val testScope = TestScope(UnconfinedTestDispatcher())
    val engine = HeadmasterVmEngine(testScope)

    val vm = VmInstance(
      id = "test_vm",
      name = "Test VM",
      osVersion = "17.0",
      apiLevel = 37,
      rootMode = RootMode.MAGISK
    )
    engine.startVm(vm)

    val unameResult = engine.executeShellCommand("uname -a")
    assertTrue(unameResult.output.contains("Linux"))

    val propResult = engine.executeShellCommand("getprop ro.build.version.release")
    assertEquals("17.0", propResult.output)

    val idResult = engine.executeShellCommand("id")
    assertTrue(idResult.output.contains("root") || idResult.output.contains("uid="))
  }
}
