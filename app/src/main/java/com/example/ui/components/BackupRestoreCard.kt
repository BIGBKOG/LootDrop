package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRestoreCard(
    viewModel: GiveawaysViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiStats by viewModel.uiStats.collectAsState()
    val isBackingUpOrRestoring by viewModel.isBackingUpOrRestoring.collectAsState()
    val backupRestoreStatus by viewModel.backupRestoreStatus.collectAsState()

    // Auto-Backup State
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState()
    val autoBackupFolderDisplayName by viewModel.autoBackupFolderDisplayName.collectAsState()
    val autoBackupIntervalDays by viewModel.autoBackupIntervalDays.collectAsState()
    val lastAutoBackupStatus by viewModel.lastAutoBackupStatus.collectAsState()
    val isAutoBackingUp by viewModel.isAutoBackingUp.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    // Storage Access Framework Launchers (Zero permissions required, works on all Android versions)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.restoreBackup(context, uri)
        }
    }

    val openFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: Exception) {
                // Best effort permission persistence
            }
            viewModel.setAutoBackupFolder(uri, context)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("backup_restore_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Icon & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Backup & Restore Games",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Export, sync or transfer library across devices",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(
                        text = "SAF • JSON",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Current Library Snapshot Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🎮", fontSize = 12.sp)
                        Text(
                            text = "Vault: ${uiStats.claimedCount} claimed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚡", fontSize = 12.sp)
                        Text(
                            text = "Steam: ${uiStats.steamTotalCount} games",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                    }
                }
            }

            Text(
                text = "Export your claimed games, play statuses (Unplayed/Playing/Beaten), and Steam library to a single JSON file. Works with Google Drive, local storage, or SD cards.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // Status feedback message if any
            AnimatedVisibility(
                visible = backupRestoreStatus != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                backupRestoreStatus?.let { status ->
                    val isSuccess = status.startsWith("✅") || status.startsWith("🎉")
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSuccess) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFF7F1D1D).copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, if (isSuccess) NeonGreen.copy(alpha = 0.6f) else Color(0xFFEF4444))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSuccess) NeonGreen else Color(0xFFFCA5A5),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissBackupRestoreStatus() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Dual Action Buttons: Manual Export & Restore
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export / Backup Button (Primary)
                Button(
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                        exportLauncher.launch("lootdrop_backup_$timestamp.json")
                    },
                    enabled = !isBackingUpOrRestoring,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("backup_games_button")
                ) {
                    if (isBackingUpOrRestoring) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Export File",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Import / Restore Button (Secondary)
                OutlinedButton(
                    onClick = { showRestoreConfirmDialog = true },
                    enabled = !isBackingUpOrRestoring,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("restore_games_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Restore Games",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

            // --- SECTION: Automatic Cloud & Drive Backup ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_backup_section"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Auto-Backup Master Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAutoBackupEnabled) NeonCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAutoBackupEnabled) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (isAutoBackupEnabled) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Auto-Backup to Cloud / Drive",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Keeps backup current in your Google Drive or cloud folder",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isAutoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackupEnabled(it, context) },
                        modifier = Modifier.testTag("switch_auto_backup_enabled"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Expandable Settings Area when Auto-Backup is Enabled
                AnimatedVisibility(
                    visible = isAutoBackupEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Destination Folder Picker Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (autoBackupFolderDisplayName.isNotBlank()) NeonCyan.copy(alpha = 0.5f) else Color(0xFFF59E0B)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (autoBackupFolderDisplayName.isNotBlank()) NeonCyan else Color(0xFFF59E0B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "BACKUP DESTINATION",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    if (autoBackupFolderDisplayName.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = NeonGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (autoBackupFolderDisplayName.isNotBlank()) {
                                    Text(
                                        text = autoBackupFolderDisplayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Sync target: lootdrop_backup_latest.json",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                } else {
                                    Text(
                                        text = "No cloud folder selected yet. Tap below to choose a Google Drive folder or device directory.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFF59E0B),
                                        lineHeight = 15.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { openFolderLauncher.launch(null) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("choose_backup_folder_button"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (autoBackupFolderDisplayName.isBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (autoBackupFolderDisplayName.isBlank()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Text(
                                            text = if (autoBackupFolderDisplayName.isBlank()) "Choose Google Drive / Cloud Folder" else "Change Folder",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (autoBackupFolderDisplayName.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { viewModel.clearAutoBackupFolder(context) },
                                            modifier = Modifier.height(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }

                        // Auto-Backup Frequency Chips ("Every couple days or something")
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Auto-Backup Interval",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val frequencies = listOf(
                                    1 to "Daily",
                                    2 to "Every 2 Days",
                                    3 to "Every 3 Days",
                                    7 to "Weekly"
                                )

                                for ((days, label) in frequencies) {
                                    val isSelected = autoBackupIntervalDays == days
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setAutoBackupIntervalDays(days, context) },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            selectedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("frequency_chip_$days")
                                    )
                                }
                            }
                        }

                        // Last Run Info & Always Current status
                        if (lastAutoBackupStatus.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = lastAutoBackupStatus,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Immediate Sync to Folder Button
                        OutlinedButton(
                            onClick = { viewModel.triggerManualAutoBackup(context) },
                            enabled = autoBackupFolderDisplayName.isNotBlank() && !isAutoBackingUp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("trigger_auto_backup_now_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (autoBackupFolderDisplayName.isNotBlank()) NeonCyan else MaterialTheme.colorScheme.outline
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonCyan
                            )
                        ) {
                            if (isAutoBackingUp) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Writing to Cloud Folder...", fontSize = 12.sp)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "Back Up to Folder Now (Keep Current)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Restore Games from File?")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select a previously exported LootDrop JSON backup file to restore your claimed Vault games, play statuses, and Steam library.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Existing claimed games are safely preserved\n• Works with files from Google Drive, cloud, or device\n• Instant one-tap import",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Select File & Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
