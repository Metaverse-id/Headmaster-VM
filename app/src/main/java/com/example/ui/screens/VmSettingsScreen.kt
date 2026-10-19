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
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RootMode
import com.example.model.VmInstance
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.HeadmasterViewModel

@Composable
fun VmSettingsScreen(viewModel: HeadmasterViewModel) {
    val selectedVm by viewModel.selectedVmForSettings.collectAsState()

    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    if (selectedVm == null) {
        viewModel.navigateTo(AppScreen.DASHBOARD)
        return
    }

    val vm = selectedVm!!

    var name by remember { mutableStateOf(vm.name) }
    var cpuCores by remember { mutableFloatStateOf(vm.cpuCores.toFloat()) }
    var ramMb by remember { mutableFloatStateOf(vm.ramMb.toFloat()) }
    var resW by remember { mutableIntStateOf(vm.resolutionWidth) }
    var resH by remember { mutableIntStateOf(vm.resolutionHeight) }
    var dpi by remember { mutableFloatStateOf(vm.dpi.toFloat()) }
    var rootMode by remember { mutableStateOf(vm.rootMode) }
    var gmsEnabled by remember { mutableStateOf(vm.gmsEnabled) }
    var xposedEnabled by remember { mutableStateOf(vm.xposedEnabled) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CyanPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "VM CONFIGURATION", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Adjust hardware & runtime properties for ${vm.name}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        // General
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "GENERAL SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("VM Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Hardware Emulation (vCPU & vRAM)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "HARDWARE ALLOCATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Virtual CPU Cores: ${cpuCores.toInt()} Cores", fontSize = 12.sp, color = TextPrimary)
                    Slider(
                        value = cpuCores,
                        onValueChange = { cpuCores = it },
                        valueRange = 1f..8f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Virtual RAM: ${ramMb.toInt()} MB", fontSize = 12.sp, color = TextPrimary)
                    Slider(
                        value = ramMb,
                        onValueChange = { ramMb = it },
                        valueRange = 1024f..8192f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                    )
                }
            }
        }

        // Display Resolution & DPI
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "VIRTUAL SCREEN RESOLUTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val presets = listOf(
                        Triple(720, 1280, "720p HD"),
                        Triple(1080, 1920, "1080p FHD"),
                        Triple(1080, 2400, "1080x2400 (20:9)"),
                        Triple(1440, 3200, "2K QHD+")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (w, h, label) ->
                            val isSelected = resW == w && resH == h
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        resW = w
                                        resH = h
                                    },
                                color = if (isSelected) CyanPrimary.copy(0.2f) else DarkSurfaceCard,
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary) else null
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyanPrimary else TextSecondary,
                                    modifier = Modifier.padding(6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Display Density: ${dpi.toInt()} DPI", fontSize = 12.sp, color = TextPrimary)
                    Slider(
                        value = dpi,
                        onValueChange = { dpi = it },
                        valueRange = 240f..560f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = CyanPrimary, activeTrackColor = CyanPrimary)
                    )
                }
            }
        }

        // Root & Subsystems
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "ROOT & INTEGRATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Root Solution: ${rootMode.name}", fontSize = 13.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            RootMode.values().forEach { mode ->
                                Surface(
                                    modifier = Modifier
                                        .clickable { rootMode = mode }
                                        .testTag("root_mode_${mode.name}"),
                                    color = if (rootMode == mode) AmberWarning.copy(0.2f) else DarkSurfaceCard,
                                    shape = RoundedCornerShape(6.dp),
                                    border = if (rootMode == mode) androidx.compose.foundation.BorderStroke(1.dp, AmberWarning) else null
                                ) {
                                    Text(
                                        text = mode.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rootMode == mode) AmberWarning else TextMuted,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Google Play Services (GMS)", fontSize = 13.sp, color = TextPrimary)
                        Switch(
                            checked = gmsEnabled,
                            onCheckedChange = { gmsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "LSPosed / Xposed Framework", fontSize = 13.sp, color = TextPrimary)
                        Switch(
                            checked = xposedEnabled,
                            onCheckedChange = { xposedEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                        )
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val updated = vm.copy(
                        name = name,
                        cpuCores = cpuCores.toInt(),
                        ramMb = ramMb.toInt(),
                        resolutionWidth = resW,
                        resolutionHeight = resH,
                        dpi = dpi.toInt(),
                        rootMode = rootMode,
                        gmsEnabled = gmsEnabled,
                        xposedEnabled = xposedEnabled
                    )
                    viewModel.saveVmSettings(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_vm_settings_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = DarkVoid, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Save Configuration", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
