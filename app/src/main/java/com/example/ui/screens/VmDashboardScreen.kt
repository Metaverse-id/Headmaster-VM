package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RootMode
import com.example.model.VmInstance
import com.example.model.VmStatus
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
fun VmDashboardScreen(viewModel: HeadmasterViewModel) {
    val vms by viewModel.vms.collectAsState()
    val runningVm by viewModel.currentRunningVm.collectAsState()
    val vmStatus by viewModel.vmStatus.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hypervisor Header
        item {
            HypervisorHeader(
                runningVm = runningVm,
                vmStatus = vmStatus,
                telemetryCpu = telemetry.cpuUsagePercent,
                onViewRunningVm = {
                    if (runningVm != null) {
                        viewModel.navigateTo(AppScreen.RUNNING_VM)
                    }
                }
            )
        }

        // Quick Operations Bar
        item {
            Text(
                text = "HYPERVISOR TOOLS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = CyanPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ToolChip(
                        title = "Import ROM ZIP",
                        subtitle = "Parse manifest.json",
                        icon = Icons.Default.Download,
                        accentColor = CyanPrimary,
                        onClick = { viewModel.navigateTo(AppScreen.IMPORT_ROM) },
                        testTag = "import_rom_chip"
                    )
                }
                item {
                    ToolChip(
                        title = "Custom ROM Studio",
                        subtitle = "System build tool",
                        icon = Icons.Default.Build,
                        accentColor = EmeraldAccent,
                        onClick = { viewModel.navigateTo(AppScreen.ROM_BUILDER) },
                        testTag = "rom_builder_chip"
                    )
                }
                item {
                    ToolChip(
                        title = "Backup & Restore",
                        subtitle = "Snapshots & images",
                        icon = Icons.Default.Restore,
                        accentColor = AmberWarning,
                        onClick = { viewModel.navigateTo(AppScreen.BACKUP_MANAGER) },
                        testTag = "backup_manager_chip"
                    )
                }
            }
        }

        // Virtual Machines Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VIRTUAL MACHINES (${vms.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Guest OS from Android 4.4W up to Android 17",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.ROM_BUILDER) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("create_vm_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New VM",
                        tint = DarkVoid,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New VM", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Virtual Machines List
        items(vms, key = { it.id }) { vm ->
            val isCurrentRunning = runningVm?.id == vm.id && vmStatus == VmStatus.RUNNING
            val isCurrentBooting = runningVm?.id == vm.id && vmStatus == VmStatus.BOOTING

            VmCard(
                vm = vm,
                isRunning = isCurrentRunning,
                isBooting = isCurrentBooting,
                onStart = { viewModel.startVm(vm) },
                onStop = { viewModel.stopVm() },
                onRestart = { viewModel.restartVm(hard = false) },
                onRepair = { viewModel.triggerRepairDialog(vm) },
                onBackup = { viewModel.triggerBackupDialog(vm) },
                onSettings = { viewModel.openSettings(vm) },
                onClone = { viewModel.cloneVm(vm) },
                onDelete = { viewModel.deleteVm(vm) }
            )
        }
    }
}

@Composable
fun HypervisorHeader(
    runningVm: VmInstance?,
    vmStatus: VmStatus,
    telemetryCpu: Float,
    onViewRunningVm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hypervisor_status_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(CyanPrimary.copy(alpha = 0.5f), DarkBorder)
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (vmStatus) {
                                    VmStatus.RUNNING -> EmeraldAccent
                                    VmStatus.BOOTING -> AmberWarning
                                    VmStatus.OFF -> TextMuted
                                    else -> CyanPrimary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HEADMASTER HYPERVISOR",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = DarkVoid,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
                ) {
                    Text(
                        text = "v3.4.0 GSI-GMS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (runningVm != null && (vmStatus == VmStatus.RUNNING || vmStatus == VmStatus.BOOTING)) {
                // Active Running VM Highlight
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewRunningVm() },
                    color = DarkSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldAccent.copy(0.4f), CyanPrimary.copy(0.2f))))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ACTIVE GUEST:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldAccent
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = runningVm.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Android ${runningVm.osVersion} • vCPU ${telemetryCpu.toInt()}% • 60 FPS",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = onViewRunningVm,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("open_active_vm_button")
                        ) {
                            Text("Open Screen", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HypervisorMetricItem(label = "Hypervisor State", value = "Idle / Ready", color = EmeraldAccent)
                    HypervisorMetricItem(label = "Host Virtualization", value = "ARM64 KVM/PRoot", color = CyanPrimary)
                    HypervisorMetricItem(label = "Max Guest API", value = "API 37 (Android 17)", color = AmberWarning)
                }
            }
        }
    }
}

@Composable
fun HypervisorMetricItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ToolChip(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
            .testTag(testTag),
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, accentColor.copy(0.3f))))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
fun VmCard(
    vm: VmInstance,
    isRunning: Boolean,
    isBooting: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onRepair: () -> Unit,
    onBackup: () -> Unit,
    onSettings: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vm_card_${vm.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) DarkSurfaceElevated else DarkSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isRunning) listOf(EmeraldAccent.copy(0.6f), CyanPrimary.copy(0.3f))
                else listOf(DarkBorder, DarkBorder)
            ),
            width = if (isRunning) 1.5.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vm.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isRunning) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = EmeraldAccent.copy(0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "ID: ${vm.id} • API ${vm.apiLevel}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                }

                // Android Version Badge
                Surface(
                    color = getVersionBadgeColor(vm.osVersion).copy(0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(getVersionBadgeColor(vm.osVersion), getVersionBadgeColor(vm.osVersion)))
                    )
                ) {
                    Text(
                        text = "Android ${vm.osVersion}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = getVersionBadgeColor(vm.osVersion),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Specs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceCard, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecBadge(icon = Icons.Default.Memory, text = "${vm.cpuCores} vCPU")
                SpecBadge(icon = Icons.Default.Storage, text = "${vm.ramMb} MB RAM")
                SpecBadge(icon = Icons.Default.Build, text = "${vm.resolutionWidth}x${vm.resolutionHeight}")
                SpecBadge(icon = Icons.Default.Security, text = "${vm.dpi} DPI")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capabilities & Plugins Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Root chip
                FeaturePill(
                    label = when (vm.rootMode) {
                        RootMode.MAGISK -> "Magisk v27"
                        RootMode.SUPERUSER -> "Superuser"
                        RootMode.NONE -> "No Root"
                    },
                    isActive = vm.rootMode != RootMode.NONE,
                    color = if (vm.rootMode != RootMode.NONE) AmberWarning else TextMuted
                )

                // Play Store / GMS chip
                FeaturePill(
                    label = if (vm.gmsEnabled) "GMS / Play" else "No GMS",
                    isActive = vm.gmsEnabled,
                    color = if (vm.gmsEnabled) CyanPrimary else TextMuted
                )

                // Xposed chip
                FeaturePill(
                    label = if (vm.xposedEnabled) "LSPosed" else "No Xposed",
                    isActive = vm.xposedEnabled,
                    color = if (vm.xposedEnabled) EmeraldAccent else TextMuted
                )

                // ABI chip
                FeaturePill(
                    label = if (vm.supportA32 && vm.supportA64) "A32+A64" else if (vm.supportA64) "ARM64" else "ARM32",
                    isActive = true,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Storage info
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Virtual Disk (/data)", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "${vm.storageUsedMb} MB / ${vm.maxStorageMb / 1024} GB",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (vm.storageUsedMb.toFloat() / vm.maxStorageMb.toFloat()).coerceIn(0.05f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = CyanPrimary,
                    trackColor = DarkSurfaceCard
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main start / restart / stop button
                if (isRunning) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onStart, // opens running screen
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_vm_button_${vm.id}")
                        ) {
                            Text("Open Screen", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onRestart,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("restart_vm_button_${vm.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restart", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = onStop,
                            modifier = Modifier.testTag("stop_vm_button_${vm.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Shut Down", tint = RedDanger)
                        }
                    }
                } else {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("start_vm_button_${vm.id}")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = DarkVoid, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start VM", color = DarkVoid, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Secondary tools
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = onRepair,
                        modifier = Modifier.testTag("repair_vm_button_${vm.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = "Repair VM", tint = CyanPrimary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onBackup,
                        modifier = Modifier.testTag("backup_vm_button_${vm.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = "Backup VM", tint = AmberWarning, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier.testTag("settings_vm_button_${vm.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onClone,
                        modifier = Modifier.testTag("clone_vm_button_${vm.id}")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Clone VM", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_vm_button_${vm.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete VM", tint = RedDanger, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SpecBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 10.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun FeaturePill(label: String, isActive: Boolean, color: Color) {
    Surface(
        color = if (isActive) color.copy(alpha = 0.12f) else DarkSurfaceElevated,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, if (isActive) color.copy(0.4f) else DarkBorder)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) color else TextMuted,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

fun getVersionBadgeColor(osVersion: String): Color {
    return when {
        osVersion.startsWith("17") -> Color(0xFF00E5FF)
        osVersion.startsWith("16") -> Color(0xFF80D8FF)
        osVersion.startsWith("15") -> Color(0xFF69F0AE)
        osVersion.startsWith("14") -> Color(0xFFFFD740)
        osVersion.startsWith("13") -> Color(0xFFFFAB40)
        osVersion.startsWith("12") -> Color(0xFFFF8A80)
        osVersion.startsWith("11") -> Color(0xFFEA80FC)
        osVersion.startsWith("10") -> Color(0xFFB388FF)
        osVersion.startsWith("9") -> Color(0xFF8C9EFF)
        osVersion.startsWith("7") -> Color(0xFF82B1FF)
        osVersion.startsWith("5") -> Color(0xFFA7FFEB)
        osVersion.contains("4.4") -> Color(0xFFFFE57F)
        else -> CyanPrimary
    }
}
