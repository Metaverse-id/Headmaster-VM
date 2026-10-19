package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OfficialGsiCatalog
import com.example.data.RomManifestParser
import com.example.model.RomManifest
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.HeadmasterViewModel

@Composable
fun RomBuilderScreen(viewModel: HeadmasterViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    var osVersion by remember { mutableStateOf("17.0") }
    var displayName by remember { mutableStateOf("Headmaster Custom ROM Android 17") }
    var romId by remember { mutableStateOf("rom_custom_17_0") }
    var romVersion by remember { mutableIntStateOf(1) }
    var supportA32 by remember { mutableStateOf(false) }
    var supportA64 by remember { mutableStateOf(true) }
    var minSdkInt by remember { mutableIntStateOf(30) }

    val romImages = remember {
        mutableStateListOf(
            "system_custom.img?e=x&c=v2",
            "vendor_custom.img?e=x&c=v2"
        )
    }

    var newImageName by remember { mutableStateOf("") }
    var includeMagisk by remember { mutableStateOf(true) }
    var includeSuperuser by remember { mutableStateOf(false) }
    var includeXposed by remember { mutableStateOf(true) }
    var includePlayStore by remember { mutableStateOf(true) }

    val builtManifest = remember(
        romId, displayName, romVersion, osVersion, supportA32, supportA64, minSdkInt,
        romImages.toList(), includeMagisk, includeSuperuser, includeXposed, includePlayStore
    ) {
        RomManifest(
            id = romId,
            displayName = displayName,
            romVersion = romVersion,
            osVersion = osVersion,
            supportA32 = supportA32,
            supportA64 = supportA64,
            minimumSdkInt = minSdkInt,
            romUri = romImages.toList(),
            magiskUri = if (includeMagisk) "asset:///plugins/magisk.zip?h=false" else null,
            suUri = if (includeSuperuser) "asset:///plugins/superuser.zip?h=false" else null,
            xposedUri = if (includeXposed) "asset:///plugins/xposed.zip" else null,
            playUri = if (includePlayStore) "asset:///plugins/play.zip" else null
        )
    }

    val previewJson = remember(builtManifest) {
        RomManifestParser.toJson(builtManifest)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    modifier = Modifier.testTag("builder_back_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "CUSTOM ROM BUILD STUDIO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Custom System Build Integration Tool", fontSize = 11.sp, color = EmeraldAccent)
                }
            }
        }

        // Target OS Version Selector
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "1. GUEST ANDROID OS VERSION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val versionOptions = listOf(
                        "4.4W" to "KitKat Wear (API 20)",
                        "5.1" to "Lollipop (API 22)",
                        "7.1" to "Nougat (API 25)",
                        "9.0" to "Pie (API 28)",
                        "10.0" to "Quince Tart (API 29)",
                        "11.0" to "Red Velvet (API 30)",
                        "12.0" to "Snow Cone (API 31)",
                        "13.0" to "Tiramisu (API 33)",
                        "14.0" to "Upside Down Cake (API 34)",
                        "15.0" to "Vanilla Ice Cream (API 35)",
                        "16.0" to "Baklava Preview (API 36)",
                        "17.0" to "Next-Gen Experimental (API 37)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("4.4W", "9.0", "14.0", "15.0", "17.0").forEach { ver ->
                            Surface(
                                modifier = Modifier
                                    .clickable {
                                        osVersion = ver
                                        romId = "rom_custom_${ver.replace(".", "_").lowercase()}"
                                        displayName = "Headmaster Custom Android $ver"
                                        minSdkInt = OfficialGsiCatalog.getApiLevelForVersion(ver)
                                    }
                                    .testTag("os_quick_select_$ver"),
                                color = if (osVersion == ver) CyanPrimary.copy(0.2f) else DarkSurfaceCard,
                                shape = RoundedCornerShape(8.dp),
                                border = if (osVersion == ver) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary) else null
                            ) {
                                Text(
                                    text = ver,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (osVersion == ver) CyanPrimary else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = romId,
                        onValueChange = { romId = it },
                        label = { Text("ROM Identifier (id)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Architecture & Kernel ABI Configuration
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "2. CPU ARCHITECTURE & ABI EMULATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Support 32-bit ARM (support_a32)", fontSize = 13.sp, color = TextPrimary)
                        Checkbox(
                            checked = supportA32,
                            onCheckedChange = { supportA32 = it },
                            colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Support 64-bit ARM (support_a64)", fontSize = 13.sp, color = TextPrimary)
                        Checkbox(
                            checked = supportA64,
                            onCheckedChange = { supportA64 = it },
                            colors = CheckboxDefaults.colors(checkedColor = CyanPrimary)
                        )
                    }
                }
            }
        }

        // ROM Partition Images (`rom_uri` single or multi-part)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "3. ROM PARTITION IMAGES (rom_uri)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Text(text = "User can specify 1 or 2+ image files (.img?e=x&c=v2)", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))

                    romImages.forEachIndexed { index, uri ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "[$index] $uri", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                            if (romImages.size > 1) {
                                IconButton(onClick = { romImages.removeAt(index) }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = RedDanger, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newImageName,
                            onValueChange = { newImageName = it },
                            placeholder = { Text("product_custom.img?e=x&c=v2", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (newImageName.isNotBlank()) {
                                    romImages.add(newImageName)
                                    newImageName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DarkVoid, modifier = Modifier.size(16.dp))
                            Text("Add", color = DarkVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Plugin Integrations (Magisk, Superuser, Xposed, PlayStore)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "4. PLUGINS & GUEST SUBSYSTEMS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)

                    BuilderPluginToggle(
                        title = "Magisk Systemless Root (magisk_uri)",
                        subtitle = "asset:///plugins/magisk.zip?h=false",
                        checked = includeMagisk,
                        onCheckedChange = {
                            includeMagisk = it
                            if (it) includeSuperuser = false
                        }
                    )

                    BuilderPluginToggle(
                        title = "Superuser App & SU Binary (su_uri)",
                        subtitle = "asset:///plugins/superuser.zip?h=false",
                        checked = includeSuperuser,
                        onCheckedChange = {
                            includeSuperuser = it
                            if (it) includeMagisk = false
                        }
                    )

                    BuilderPluginToggle(
                        title = "LSPosed / Xposed Framework (xposed_uri)",
                        subtitle = "asset:///plugins/xposed.zip",
                        checked = includeXposed,
                        onCheckedChange = { includeXposed = it }
                    )

                    BuilderPluginToggle(
                        title = "Google Play Services & Store (play_uri)",
                        subtitle = "asset:///plugins/play.zip",
                        checked = includePlayStore,
                        onCheckedChange = { includePlayStore = it }
                    )
                }
            }
        }

        // Generated manifest.json Preview
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "GENERATED manifest.json PREVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFF03060B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = previewJson,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyanPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val newVm = viewModel.importRomFromManifest(builtManifest)
                            viewModel.startVm(newVm)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("build_and_deploy_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = DarkVoid, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Compile ROM & Boot Headmaster VM", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BuilderPluginToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
            )
        }
    }
}
