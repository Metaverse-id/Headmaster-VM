package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun RomImportScreen(viewModel: HeadmasterViewModel) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    var jsonInput by remember {
        mutableStateOf(
            """{
    "id": "rom_official_14_upsidedown",
    "display_name": "Android 14 GSI GMS Official",
    "rom_version": 1,
    "os_version": "14.0",
    "support_a32": false,
    "support_a64": true,
    "minimum_sdk_int": 28,
    "rom_uri": [
        "system_u_arm64_gms.img?e=x&c=v2",
        "vendor_u.img?e=x&c=v2"
    ],
    "magisk_uri": "asset:///plugins/magisk.zip?h=false",
    "su_uri": "asset:///plugins/superuser.zip?h=false",
    "xposed_uri": "asset:///plugins/xposed.zip",
    "play_uri": "asset:///plugins/play.zip"
}"""
        )
    }

    val validationResult = remember(jsonInput) {
        RomManifestParser.parseJson(jsonInput)
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
                    modifier = Modifier.testTag("import_rom_back_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "IMPORT CUSTOM ROM ZIP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Validates manifest.json & GSI GMS partitions", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        // Quick Preset Selector from Official Catalog
        item {
            Text(text = "LOAD OFFICIAL GSI GMS PRESET (4.4W -> 17)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(OfficialGsiCatalog.catalog) { preset ->
                    Surface(
                        modifier = Modifier
                            .clickable {
                                jsonInput = RomManifestParser.toJson(preset)
                            }
                            .testTag("preset_chip_${preset.id}"),
                        color = DarkSurfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, CyanPrimary.copy(0.3f))))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(text = "Android ${preset.osVersion}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = if (preset.supportA32 && preset.supportA64) "A32+A64" else if (preset.supportA64) "A64" else "A32", fontSize = 9.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // JSON Manifest Editor
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "manifest.json Specification", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "JSON with comments (#)", fontSize = 10.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .testTag("manifest_json_editor"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF03060B),
                            unfocusedContainerColor = Color(0xFF03060B),
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                }
            }
        }

        // Validation Results Card
        item {
            when (validationResult) {
                is RomManifestParser.ValidationResult.Success -> {
                    val m = validationResult.manifest
                    val sdkOk = Build.VERSION.SDK_INT >= m.minimumSdkInt

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldAccent.copy(0.6f), CyanPrimary.copy(0.4f))))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "VALID MANIFEST SPECIFICATION", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            ValidationItem(label = "ROM ID", value = m.id, ok = true)
                            ValidationItem(label = "Display Name", value = m.displayName, ok = true)
                            ValidationItem(label = "OS Version", value = "Android ${m.osVersion} (rom_version: ${m.romVersion})", ok = true)
                            ValidationItem(label = "ABI Architecture", value = "support_a32: ${m.supportA32}, support_a64: ${m.supportA64}", ok = true)
                            ValidationItem(label = "Min Host SDK", value = "API ${m.minimumSdkInt} (Device: API ${Build.VERSION.SDK_INT})", ok = sdkOk)
                            ValidationItem(label = "Partition Images (${m.romUri.size})", value = m.romUri.joinToString(", "), ok = true)
                            ValidationItem(label = "Magisk URI", value = m.magiskUri ?: "Disabled", ok = m.magiskUri != null)
                            ValidationItem(label = "Superuser URI", value = m.suUri ?: "Disabled", ok = m.suUri != null)
                            ValidationItem(label = "Xposed URI", value = m.xposedUri ?: "Disabled", ok = m.xposedUri != null)
                            ValidationItem(label = "PlayStore URI", value = m.playUri ?: "Disabled", ok = m.playUri != null)

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val vm = viewModel.importRomFromManifest(m)
                                    viewModel.startVm(vm)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("deploy_rom_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = DarkVoid, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Deploy & Start VM (${m.displayName})", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
                is RomManifestParser.ValidationResult.Error -> {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(RedDanger.copy(0.6f), RedDanger.copy(0.4f))))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = RedDanger, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "MANIFEST VALIDATION ERROR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RedDanger)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = validationResult.message, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValidationItem(label: String, value: String, ok: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = if (ok) TextPrimary else RedDanger
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (ok) EmeraldAccent else RedDanger,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
