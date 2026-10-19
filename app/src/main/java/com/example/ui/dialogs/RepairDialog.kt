package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VmStatus
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkVoid
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.HeadmasterViewModel

@Composable
fun RepairDialog(viewModel: HeadmasterViewModel) {
    val activeVm by viewModel.activeVmForAction.collectAsState()
    val report by viewModel.lastRepairReport.collectAsState()
    val status by viewModel.vmStatus.collectAsState()

    if (activeVm == null) return

    val isRepairing = status == VmStatus.REPAIRING

    AlertDialog(
        onDismissRequest = {
            if (!isRepairing) viewModel.closeRepairDialog()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Repair Virtual Machine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "VM: ${activeVm?.name} (${activeVm?.id})",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyanPrimary
                )

                if (report == null) {
                    Text(
                        text = "This operation executes virtual e2fsck partition check, resolves orphan inodes, wipes corrupted dalvik-cache, and restores SELinux security contexts on userdata.img.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    if (isRepairing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Scanning & repairing virtual blocks...", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                } else {
                    // Repair Completed Report
                    Surface(
                        color = EmeraldAccent.copy(0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Filesystem Integrity Restored (100% Clean)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                        }
                    }

                    Text(
                        text = "Inodes Checked: ${report?.inodesChecked} • Bad blocks resolved: ${report?.repairedSectors}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF03060B))
                    ) {
                        LazyColumn(modifier = Modifier.height(130.dp).padding(8.dp)) {
                            items(report?.logs ?: emptyList()) { logLine ->
                                Text(
                                    text = logLine,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldAccent
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (report == null && !isRepairing) {
                Button(
                    onClick = { viewModel.executeRepair() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    modifier = Modifier.testTag("execute_repair_button")
                ) {
                    Text("Start Repair", color = DarkVoid, fontWeight = FontWeight.Bold)
                }
            } else if (report != null) {
                Button(
                    onClick = { viewModel.closeRepairDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Done", color = DarkVoid, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isRepairing && report == null) {
                TextButton(onClick = { viewModel.closeRepairDialog() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
