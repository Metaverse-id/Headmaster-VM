package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.dialogs.BackupDialog
import com.example.ui.dialogs.RepairDialog
import com.example.ui.screens.BackupManagerScreen
import com.example.ui.screens.RomBuilderScreen
import com.example.ui.screens.RomImportScreen
import com.example.ui.screens.VmDashboardScreen
import com.example.ui.screens.VmRunningScreen
import com.example.ui.screens.VmSettingsScreen
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.HeadmasterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HeadmasterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HeadmasterApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HeadmasterApp(viewModel: HeadmasterViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showRepairDialog by viewModel.showRepairDialog.collectAsState()
    val showBackupDialog by viewModel.showBackupDialog.collectAsState()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkVoid)
            .windowInsetsPadding(
                if (currentScreen == AppScreen.RUNNING_VM) WindowInsets.statusBars
                else WindowInsets.safeDrawing
            ),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DarkVoid
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> VmDashboardScreen(viewModel = viewModel)
                    AppScreen.RUNNING_VM -> VmRunningScreen(viewModel = viewModel)
                    AppScreen.IMPORT_ROM -> RomImportScreen(viewModel = viewModel)
                    AppScreen.ROM_BUILDER -> RomBuilderScreen(viewModel = viewModel)
                    AppScreen.BACKUP_MANAGER -> BackupManagerScreen(viewModel = viewModel)
                    AppScreen.VM_SETTINGS -> VmSettingsScreen(viewModel = viewModel)
                }
            }

            if (showRepairDialog) {
                RepairDialog(viewModel = viewModel)
            }

            if (showBackupDialog) {
                BackupDialog(viewModel = viewModel)
            }
        }
    }
}
