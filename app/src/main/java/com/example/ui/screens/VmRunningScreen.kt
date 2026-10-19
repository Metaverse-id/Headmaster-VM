package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GuestApp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun VmRunningScreen(viewModel: HeadmasterViewModel) {
    val currentVm by viewModel.currentRunningVm.collectAsState()
    val vmStatus by viewModel.vmStatus.collectAsState()
    val bootLogs by viewModel.bootLogs.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val guestApps by viewModel.guestApps.collectAsState()
    val activeGuestApp by viewModel.activeGuestApp.collectAsState()

    var showAssistantMenu by remember { mutableStateOf(false) }
    var showHudOverlay by remember { mutableStateOf(true) }
    var showInstallApkDialog by remember { mutableStateOf(false) }
    var showSharedFilesDialog by remember { mutableStateOf(false) }
    var showScreenshotToast by remember { mutableStateOf(false) }

    // Floating ball offset
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(160f) }

    // Hardware Back button handling
    BackHandler {
        if (activeGuestApp != null) {
            viewModel.closeActiveGuestApp()
        } else {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Virtual Device Top Status Bar
            VmTopStatusBar(
                currentVm = currentVm,
                vmStatus = vmStatus,
                onBackToDashboard = { viewModel.navigateTo(AppScreen.DASHBOARD) }
            )

            // Main Viewport: Booting animation OR Running Guest OS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (vmStatus) {
                    VmStatus.BOOTING -> {
                        VmBootingView(currentVm = currentVm, bootLogs = bootLogs)
                    }
                    VmStatus.RUNNING -> {
                        if (activeGuestApp != null) {
                            GuestAppView(
                                app = activeGuestApp!!,
                                vm = currentVm,
                                viewModel = viewModel,
                                onClose = { viewModel.closeActiveGuestApp() }
                            )
                        } else {
                            GuestLauncherView(
                                vm = currentVm,
                                guestApps = guestApps,
                                onLaunchApp = { app -> viewModel.launchGuestApp(app) },
                                onInstallApk = { showInstallApkDialog = true }
                            )
                        }
                    }
                    VmStatus.OFF, VmStatus.SHUTTING_DOWN -> {
                        VmPoweredOffView(
                            currentVm = currentVm,
                            onStartVm = {
                                if (currentVm != null) viewModel.startVm(currentVm!!)
                            },
                            onReturnDashboard = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                        )
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CyanPrimary)
                        }
                    }
                }

                // HUD Overlay (FPS, CPU, RAM)
                if (vmStatus == VmStatus.RUNNING && showHudOverlay) {
                    PerformanceHud(telemetry = telemetry, modifier = Modifier.align(Alignment.TopEnd))
                }

                // Screenshot notification toast
                if (showScreenshotToast) {
                    ScreenshotToast(
                        onDismiss = { showScreenshotToast = false },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 60.dp)
                    )
                }
            }

            // Virtual Navigation Bar (Back, Home, Recents)
            if (vmStatus == VmStatus.RUNNING) {
                VirtualNavigationBar(
                    onBack = {
                        if (activeGuestApp != null) viewModel.closeActiveGuestApp()
                    },
                    onHome = {
                        viewModel.closeActiveGuestApp()
                    },
                    onRecents = {
                        // Toggle assistant menu
                        showAssistantMenu = true
                    }
                )
            }
        }

        // Floating Assistant Ball (draggable)
        if (vmStatus == VmStatus.RUNNING) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .padding(16.dp)
                    .align(Alignment.CenterEnd)
            ) {
                FloatingActionButton(
                    onClick = { showAssistantMenu = true },
                    containerColor = CyanPrimary,
                    contentColor = DarkVoid,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("floating_vm_tool_ball")
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "VM Menu", modifier = Modifier.size(22.dp))
                }
            }
        }

        // Assistant Quick Controls Dialog
        if (showAssistantMenu && currentVm != null) {
            VmAssistantMenuDialog(
                vm = currentVm!!,
                showHud = showHudOverlay,
                onToggleHud = { showHudOverlay = !showHudOverlay },
                onRestart = {
                    showAssistantMenu = false
                    viewModel.restartVm(hard = false)
                },
                onHardReset = {
                    showAssistantMenu = false
                    viewModel.restartVm(hard = true)
                },
                onShutDown = {
                    showAssistantMenu = false
                    viewModel.stopVm()
                },
                onScreenshot = {
                    showAssistantMenu = false
                    showScreenshotToast = true
                },
                onOpenSharedFiles = {
                    showAssistantMenu = false
                    showSharedFilesDialog = true
                },
                onInstallApk = {
                    showAssistantMenu = false
                    showInstallApkDialog = true
                },
                onToggleRoot = { mode ->
                    viewModel.saveVmSettings(currentVm!!.copy(rootMode = mode))
                },
                onToggleGms = {
                    viewModel.saveVmSettings(currentVm!!.copy(gmsEnabled = !currentVm!!.gmsEnabled))
                },
                onToggleXposed = {
                    viewModel.saveVmSettings(currentVm!!.copy(xposedEnabled = !currentVm!!.xposedEnabled))
                },
                onDismiss = { showAssistantMenu = false }
            )
        }

        // Install APK Dialog
        if (showInstallApkDialog) {
            InstallApkDialog(
                onInstall = { name, pkg ->
                    viewModel.installApkToGuest(name, pkg)
                    showInstallApkDialog = false
                },
                onDismiss = { showInstallApkDialog = false }
            )
        }

        // Shared Files Dialog
        if (showSharedFilesDialog) {
            SharedFilesDialog(
                viewModel = viewModel,
                onDismiss = { showSharedFilesDialog = false }
            )
        }
    }
}

@Composable
fun VmTopStatusBar(
    currentVm: VmInstance?,
    vmStatus: VmStatus,
    onBackToDashboard: () -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("vm_back_to_dashboard")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = currentVm?.name ?: "Headmaster VM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Android ${currentVm?.osVersion ?: "14"} • ${vmStatus.name}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (vmStatus == VmStatus.RUNNING) EmeraldAccent else AmberWarning
                    )
                }
            }

            // Virtual System Indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Wifi, contentDescription = "Wi-Fi", tint = EmeraldAccent, modifier = Modifier.size(14.dp))
                Text(
                    text = "5G",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanPrimary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "100%",
                    fontSize = 10.sp,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun VmBootingView(currentVm: VmInstance?, bootLogs: List<String>) {
    val listState = rememberLazyListState()

    LaunchedEffect(bootLogs.size) {
        if (bootLogs.isNotEmpty()) {
            listState.animateScrollToItem(bootLogs.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // High-Tech Cyber Boot Indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(2.dp, Brush.sweepGradient(listOf(CyanPrimary, EmeraldAccent, AmberWarning, CyanPrimary)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BOOTING ANDROID ${currentVm?.osVersion ?: ""}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = CyanPrimary
            )

            Text(
                text = "Initializing Kernel 6.1, Zygote & System Services...",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Boot log terminal window
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030508)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, CyanPrimary.copy(0.3f))))
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    items(bootLogs) { line ->
                        Text(
                            text = line,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = when {
                                line.contains("ERROR") -> RedDanger
                                line.contains("WARN") -> AmberWarning
                                line.contains("HEADMASTER") || line.contains("running") -> EmeraldAccent
                                line.contains("SERVICE") || line.contains("INIT") -> CyanPrimary
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                color = CyanPrimary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        }
    }
}

@Composable
fun GuestLauncherView(
    vm: VmInstance?,
    guestApps: List<GuestApp>,
    onLaunchApp: (GuestApp) -> Unit,
    onInstallApk: () -> Unit
) {
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val currentDate = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1426),
                        Color(0xFF080D18),
                        Color(0xFF04060A)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Guest Clock & Date Widget (Adapts to OS version vibe)
            Text(
                text = currentTime,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = currentDate,
                fontSize = 13.sp,
                color = CyanPrimary,
                fontWeight = FontWeight.Medium
            )

            // Search Bar Widget
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val browser = guestApps.find { it.iconCategory == "browser" }
                        if (browser != null) onLaunchApp(browser)
                    },
                color = DarkSurfaceElevated.copy(alpha = 0.8f),
                shape = RoundedCornerShape(24.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Search Google / apps in VM...", fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guest Apps Grid
            Text(
                text = "GUEST APPLICATIONS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(guestApps) { app ->
                    GuestAppListItem(app = app, onClick = { onLaunchApp(app) })
                }

                item {
                    // Add / Install APK button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInstallApk() }
                            .testTag("guest_install_apk_button"),
                        color = DarkSurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldAccent.copy(0.4f), EmeraldAccent.copy(0.2f))))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Install APK into Guest", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                                Text(text = "Sideload any application into this VM", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuestAppListItem(app: GuestApp, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("guest_app_${app.packageName}"),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(getAppIconBg(app.iconCategory)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAppIconVector(app.iconCategory),
                        contentDescription = null,
                        tint = getAppIconTint(app.iconCategory),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "${app.packageName} • v${app.versionName}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                }
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Open",
                tint = CyanPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun GuestAppView(
    app: GuestApp,
    vm: VmInstance?,
    viewModel: HeadmasterViewModel,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
    ) {
        // App Title Bar
        Surface(
            color = DarkSurfaceElevated,
            modifier = Modifier.fillMaxWidth(),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).testTag("close_guest_app_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = app.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Surface(
                    color = DarkVoid,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "PID: 1042 • VM",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Dedicated interactive screens for apps
        when (app.iconCategory) {
            "terminal" -> GuestTerminalScreen(viewModel = viewModel)
            "magisk" -> GuestMagiskScreen(vm = vm, viewModel = viewModel)
            "su" -> GuestSuperuserScreen(vm = vm, viewModel = viewModel)
            "play" -> GuestPlayStoreScreen(viewModel = viewModel)
            "settings" -> GuestSettingsScreen(vm = vm)
            "files" -> GuestFilesScreen(viewModel = viewModel)
            "browser" -> GuestBrowserScreen()
            else -> GuestGenericAppScreen(app = app)
        }
    }
}

@Composable
fun GuestTerminalScreen(viewModel: HeadmasterViewModel) {
    val history by viewModel.terminalHistory.collectAsState()
    val isRoot by viewModel.isTerminalRoot.collectAsState()
    var inputCmd by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(history.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03060B))
            .padding(12.dp)
    ) {
        // Quick autocomplete chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("uname -a", "getprop", "su", "id", "df -h", "free -m", "ps", "top").forEach { cmd ->
                Surface(
                    modifier = Modifier.clickable {
                        viewModel.executeTerminalCommand(cmd)
                    },
                    color = DarkSurfaceCard,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = cmd,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // History Output
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                Text(
                    text = "Headmaster Linux Shell 6.1.75 #1 PREEMPT\nType 'help' for available commands.\n",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = EmeraldAccent
                )
            }
            items(history) { entry ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "${if (entry.isRoot) "root@headmaster:~# " else "system@headmaster:~$ "}${entry.command}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isRoot) AmberWarning else CyanPrimary
                    )
                    if (entry.output.isNotEmpty()) {
                        Text(
                            text = entry.output,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isRoot) "# " else "$ ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isRoot) AmberWarning else CyanPrimary
            )

            OutlinedTextField(
                value = inputCmd,
                onValueChange = { inputCmd = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_command_input"),
                placeholder = { Text("Enter command...", fontSize = 12.sp, color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputCmd.isNotBlank()) {
                        viewModel.executeTerminalCommand(inputCmd)
                        inputCmd = ""
                    }
                },
                modifier = Modifier.testTag("terminal_send_button")
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Run", tint = CyanPrimary)
            }
        }
    }
}

@Composable
fun GuestMagiskScreen(vm: VmInstance?, viewModel: HeadmasterViewModel) {
    var zygiskOn by remember { mutableStateOf(true) }
    var denyListOn by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AmberWarning.copy(0.5f), DarkBorder)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Magisk v27.0 (27000)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Installed: Official Headmaster Daemon", fontSize = 11.sp, color = EmeraldAccent)
                            Text(text = "Ramdisk: Yes • SAR: Yes • A/B: Yes", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                        }
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }

        item {
            Text(text = "MAGISK SETTINGS & MODULES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }

        item {
            MagiskToggleRow(
                title = "Zygisk",
                subtitle = "Run Magisk in zygote process",
                checked = zygiskOn,
                onCheckedChange = { zygiskOn = it }
            )
        }

        item {
            MagiskToggleRow(
                title = "Enforce DenyList",
                subtitle = "Hide root from selected bank/security apps",
                checked = denyListOn,
                onCheckedChange = { denyListOn = it }
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Installed Modules (from magisk_uri)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Systemless Hosts v1.0", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• Headmaster-VirtIO-Audio-Bridge v2.1", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• LSPosed Zygisk Release v1.9.2", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun MagiskToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning, checkedTrackColor = AmberWarning.copy(0.4f))
            )
        }
    }
}

@Composable
fun GuestSuperuserScreen(vm: VmInstance?, viewModel: HeadmasterViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Superuser Access", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Binary: /system/xbin/su (version 10.3)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyanPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "SU daemon is active and granting requested permissions.", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        item {
            Text(text = "ALLOWED ROOT APPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "✓ jackpal.androidterm (Terminal Emulator) - UID 1000", fontSize = 12.sp, color = EmeraldAccent)
                    Text(text = "✓ com.android.documentsui (File Manager) - UID 1000", fontSize = 12.sp, color = EmeraldAccent)
                    Text(text = "✓ headmaster.sys.bridge - UID 0", fontSize = 12.sp, color = EmeraldAccent)
                }
            }
        }
    }
}

@Composable
fun GuestPlayStoreScreen(viewModel: HeadmasterViewModel) {
    val storeApps = listOf(
        Pair("Geekbench 6", "Benchmark CPU & Virtual GPU"),
        Pair("Termux", "Terminal environment and packages"),
        Pair("WhatsApp Messenger", "Fast & simple private messaging"),
        Pair("Chrome Browser", "Fast, secure web browser"),
        Pair("PUBG Mobile", "Battle royale tactical multiplayer")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shop, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Google Play Store (GMS GSI)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        items(storeApps) { (appName, desc) ->
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = appName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = desc, fontSize = 11.sp, color = TextMuted)
                    }
                    Button(
                        onClick = {
                            viewModel.installApkToGuest(appName, "com.${appName.lowercase().replace(" ", "")}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Install", color = DarkVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GuestSettingsScreen(vm: VmInstance?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "ABOUT VIRTUAL PHONE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
        }

        item {
            SettingsItemRow(label = "Device Name", value = vm?.name ?: "Headmaster Virtual Phone")
        }
        item {
            SettingsItemRow(label = "Android Version", value = "${vm?.osVersion ?: "14"} (API ${vm?.apiLevel ?: 34})")
        }
        item {
            SettingsItemRow(label = "Kernel Version", value = "6.1.75-headmaster-vmid")
        }
        item {
            SettingsItemRow(label = "Build Number", value = "GSI-GMS-${vm?.osVersion}-OFFICIAL-2026")
        }
        item {
            SettingsItemRow(label = "Processor Emulation", value = "${vm?.cpuCores ?: 4} Cores (${if (vm?.supportA64 == true) "ARM64-v8a" else "ARM32-v7a"})")
        }
        item {
            SettingsItemRow(label = "Allocated Physical RAM", value = "${vm?.ramMb ?: 4096} MB LPDDR5X")
        }
        item {
            SettingsItemRow(label = "Virtual Screen", value = "${vm?.resolutionWidth}x${vm?.resolutionHeight} @ ${vm?.dpi} DPI")
        }
    }
}

@Composable
fun SettingsItemRow(label: String, value: String) {
    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun GuestFilesScreen(viewModel: HeadmasterViewModel) {
    val folders = listOf("/system", "/vendor", "/data", "/sdcard", "/storage/emulated/0/HeadmasterShared")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(text = "VIRTUAL STORAGE EXPLORER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
        items(folders) { path ->
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = path, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun GuestBrowserScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Headmaster Web Sandbox", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = "Virtual network connection: 10.0.2.15/24 (NAT)", fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text(
                text = "https://www.google.com/search?q=headmaster+android+vm",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = CyanPrimary,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun GuestGenericAppScreen(app: GuestApp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = app.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = app.packageName, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
        }
    }
}

@Composable
fun VirtualNavigationBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back (Triangle)
            IconButton(onClick = onBack, modifier = Modifier.testTag("vm_nav_back")) {
                Text(text = "◁", fontSize = 20.sp, color = TextSecondary)
            }

            // Home (Circle)
            IconButton(onClick = onHome, modifier = Modifier.testTag("vm_nav_home")) {
                Text(text = "○", fontSize = 22.sp, color = TextSecondary)
            }

            // Recents (Square)
            IconButton(onClick = onRecents, modifier = Modifier.testTag("vm_nav_recents")) {
                Text(text = "□", fontSize = 20.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun PerformanceHud(telemetry: com.example.runtime.VmTelemetry, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(12.dp)
            .testTag("performance_hud_overlay"),
        color = Color(0xCC050A14),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanPrimary.copy(0.4f), EmeraldAccent.copy(0.4f))))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "${telemetry.fps} FPS • ${telemetry.latencyMs}ms",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = EmeraldAccent
            )
            Text(
                text = "CPU: ${telemetry.cpuUsagePercent.toInt()}% • RAM: ${telemetry.ramUsedMb}/${telemetry.ramTotalMb}MB",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
            )
            Text(
                text = "VirtIO GLES 3.2",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = CyanPrimary
            )
        }
    }
}

@Composable
fun ScreenshotToast(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }
    Surface(
        modifier = modifier,
        color = DarkSurfaceCard,
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldAccent, CyanPrimary)))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Screenshot captured to /sdcard/Headmaster/Screenshots/", fontSize = 11.sp, color = TextPrimary)
        }
    }
}

@Composable
fun VmAssistantMenuDialog(
    vm: VmInstance,
    showHud: Boolean,
    onToggleHud: () -> Unit,
    onRestart: () -> Unit,
    onHardReset: () -> Unit,
    onShutDown: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenSharedFiles: () -> Unit,
    onInstallApk: () -> Unit,
    onToggleRoot: (RootMode) -> Unit,
    onToggleGms: () -> Unit,
    onToggleXposed: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "VM Hypervisor Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Restart", color = DarkVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onShutDown,
                        colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Shut Down", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onScreenshot,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Screenshot", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenSharedFiles,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Files Bridge", fontSize = 11.sp)
                    }
                }

                HorizontalDivider(color = DarkBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Show Performance HUD", fontSize = 12.sp, color = TextPrimary)
                    Switch(
                        checked = showHud,
                        onCheckedChange = { onToggleHud() },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Google Play Services (GMS)", fontSize = 12.sp, color = TextPrimary)
                    Switch(
                        checked = vm.gmsEnabled,
                        onCheckedChange = { onToggleGms() },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Xposed / LSPosed Framework", fontSize = 12.sp, color = TextPrimary)
                    Switch(
                        checked = vm.xposedEnabled,
                        onCheckedChange = { onToggleXposed() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldAccent)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Root: ${vm.rootMode.name}", fontSize = 12.sp, color = AmberWarning)
                    TextButton(onClick = {
                        val nextMode = when (vm.rootMode) {
                            RootMode.NONE -> RootMode.MAGISK
                            RootMode.MAGISK -> RootMode.SUPERUSER
                            RootMode.SUPERUSER -> RootMode.NONE
                        }
                        onToggleRoot(nextMode)
                    }) {
                        Text(text = "Switch Mode", fontSize = 11.sp, color = AmberWarning)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = CyanPrimary)
            }
        },
        containerColor = DarkSurfaceElevated
    )
}

@Composable
fun InstallApkDialog(
    onInstall: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var appName by remember { mutableStateOf("Termux CLI") }
    var pkgName by remember { mutableStateOf("com.termux") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Install APK into VM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Select an APK package to inject into the guest system:", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pkgName,
                    onValueChange = { pkgName = it },
                    label = { Text("Package Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onInstall(appName, pkgName) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Install", color = DarkVoid, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = DarkSurfaceElevated
    )
}

@Composable
fun SharedFilesDialog(viewModel: HeadmasterViewModel, onDismiss: () -> Unit) {
    val files by viewModel.sharedFiles.collectAsState()
    var newFileName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Host <-> Guest File Transfer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Shared Directory: /sdcard/HeadmasterShared/", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyanPrimary)

                LazyColumn(modifier = Modifier.height(140.dp)) {
                    items(files) { f ->
                        Text(text = "📄 $f", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = { newFileName = it },
                        placeholder = { Text("file_name.zip", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newFileName.isNotBlank()) {
                                viewModel.addSharedFile(newFileName)
                                newFileName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                    ) {
                        Text("Add", color = DarkVoid, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = CyanPrimary) }
        },
        containerColor = DarkSurfaceElevated
    )
}

@Composable
fun VmPoweredOffView(
    currentVm: VmInstance?,
    onStartVm: () -> Unit,
    onReturnDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Virtual Machine is Powered Off", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = currentVm?.name ?: "", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onStartVm,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Start VM", color = DarkVoid, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onReturnDashboard) {
                Text("Return to Dashboard", color = TextPrimary)
            }
        }
    }
}

fun getAppIconBg(cat: String): Color {
    return when (cat) {
        "play" -> Color(0xFF00E5FF).copy(0.15f)
        "magisk" -> Color(0xFFFFB300).copy(0.15f)
        "su" -> Color(0xFFFF5252).copy(0.15f)
        "terminal" -> Color(0xFF00E676).copy(0.15f)
        "settings" -> Color(0xFF80D8FF).copy(0.15f)
        "files" -> Color(0xFFFFD740).copy(0.15f)
        "browser" -> Color(0xFF69F0AE).copy(0.15f)
        else -> Color(0xFF94A3B8).copy(0.15f)
    }
}

fun getAppIconTint(cat: String): Color {
    return when (cat) {
        "play" -> CyanPrimary
        "magisk" -> AmberWarning
        "su" -> RedDanger
        "terminal" -> EmeraldAccent
        "settings" -> CyanPrimary
        "files" -> AmberWarning
        "browser" -> EmeraldAccent
        else -> TextPrimary
    }
}

fun getAppIconVector(cat: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (cat) {
        "play" -> Icons.Default.Shop
        "magisk" -> Icons.Default.Security
        "su" -> Icons.Default.Security
        "terminal" -> Icons.Default.Terminal
        "settings" -> Icons.Default.Settings
        "files" -> Icons.Default.Folder
        "browser" -> Icons.Default.Language
        else -> Icons.Default.Android
    }
}
