package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.HeadmasterViewModel

@Composable
fun BackupDialog(viewModel: HeadmasterViewModel) {
    val activeVm by viewModel.activeVmForAction.collectAsState()
    var note by remember { mutableStateOf("Full snapshot before experiments") }

    if (activeVm == null) return

    AlertDialog(
        onDismissRequest = { viewModel.closeBackupDialog() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Create VM Snapshot Backup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column {
                Text(text = "Target: ${activeVm?.name} (Android ${activeVm?.osVersion})", fontSize = 12.sp, color = CyanPrimary)
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = "Saves userdata.img, virtual machine hardware allocations, and installed guest packages into a compressed snapshot archive.", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.size(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Backup Note / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("backup_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.createBackup(note) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                modifier = Modifier.testTag("confirm_backup_button")
            ) {
                Text("Create Backup", color = DarkVoid, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeBackupDialog() }) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
