package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudBackupCard(
    viewModel: GiveawaysViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isConfigured = viewModel.isCloudBackupConfigured
    val isLoggedIn by viewModel.isCloudAccountLoggedIn.collectAsState()
    val isSessionActive by viewModel.isCloudSessionActiveFlow.collectAsState()
    val username by viewModel.cloudAccountUsername.collectAsState()
    val isAuthenticating by viewModel.isCloudAuthenticating.collectAsState()
    val isBackingUp by viewModel.isCloudBackingUp.collectAsState()
    val isRestoring by viewModel.isCloudRestoring.collectAsState()
    val cloudStatus by viewModel.cloudBackupStatus.collectAsState()
    val lastBackupTimestamp by viewModel.lastCloudBackupTimestamp.collectAsState()
    val isCloudAutoBackupEnabled by viewModel.isCloudAutoBackupEnabled.collectAsState()

    var showCreateAccountDialog by remember { mutableStateOf(false) }
    var showSignInDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cloud_backup_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isLoggedIn && isSessionActive) NeonCyan.copy(alpha = 0.5f)
            else if (isLoggedIn) Color(0xFFF59E0B).copy(alpha = 0.5f)
            else if (isConfigured) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Cloud Icon + Title + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isLoggedIn && isSessionActive) NeonCyan.copy(alpha = 0.15f)
                                else if (isLoggedIn) Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLoggedIn && isSessionActive) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isLoggedIn && isSessionActive) NeonCyan else if (isLoggedIn) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Cloudflare R2 Cloud Backup",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoggedIn && isSessionActive) "Connected • Account: $username" else if (isLoggedIn) "Account: $username (Tap to Sign In)" else "Cloudflare R2 Object Storage (10 GB Free)",
                            fontSize = 11.sp,
                            color = if (isLoggedIn && isSessionActive) NeonCyan else if (isLoggedIn) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isLoggedIn) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isLoggedIn && isSessionActive -> Color(0xFF064E3B).copy(alpha = 0.5f)
                        isLoggedIn -> Color(0xFF78350F).copy(alpha = 0.5f)
                        isConfigured -> Color(0xFF1E3A8A).copy(alpha = 0.4f)
                        else -> Color(0xFF78350F).copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isLoggedIn && isSessionActive -> NeonGreen.copy(alpha = 0.7f)
                            isLoggedIn -> Color(0xFFF59E0B)
                            isConfigured -> Color(0xFF60A5FA)
                            else -> Color(0xFFF59E0B)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isLoggedIn && isSessionActive -> NeonGreen
                                        isLoggedIn -> Color(0xFFF59E0B)
                                        isConfigured -> Color(0xFF60A5FA)
                                        else -> Color(0xFFF59E0B)
                                    }
                                )
                        )
                        Text(
                            text = when {
                                isLoggedIn && isSessionActive -> "R2 Synced"
                                isLoggedIn -> "Sign In Needed"
                                isConfigured -> "Login Needed"
                                else -> "Unconfigured"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isLoggedIn && isSessionActive -> NeonGreen
                                isLoggedIn -> Color(0xFFFDE68A)
                                isConfigured -> Color(0xFF93C5FD)
                                else -> Color(0xFFFDE68A)
                            }
                        )
                    }
                }
            }

            // Descriptive Subtitle
            Text(
                text = "Backups sync directly to your private Cloudflare R2 bucket. Your saved giveaways, game library, and profile remain securely backed up across devices.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            // IF LOGGED IN: Account Profile & Cloud Details
            if (isLoggedIn) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = username,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val accountEmail = viewModel.cloudAccountEmail
                                    val subtitleText = if (accountEmail.isNotBlank()) {
                                        accountEmail
                                    } else {
                                        "lootdrop_backup_${username.lowercase().trim()}.json"
                                    }
                                    Text(
                                        text = subtitleText,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { showSignOutConfirmDialog = true },
                                modifier = Modifier.testTag("btn_sign_out_cloud"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF87171)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFF87171)
                                    )
                                    Text(
                                        text = "Sign Out",
                                        fontSize = 11.sp,
                                        color = Color(0xFFF87171),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Storage Destination:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Cloudflare R2 Bucket",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonCyan
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Last Account Backup:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (lastBackupTimestamp > 0L) {
                                    SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).format(Date(lastBackupTimestamp))
                                } else {
                                    "No backup uploaded yet"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (lastBackupTimestamp > 0L) MaterialTheme.colorScheme.onSurface else TextSecondary
                            )
                        }

                        if (!isSessionActive) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF78350F).copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "⚠️ Session disconnected",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE68A),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { showSignInDialog = true },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Sign In", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                                        }
                                        TextButton(
                                            onClick = { showSignOutConfirmDialog = true },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Sign Out", fontSize = 11.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // NOT LOGGED IN: Account Creation / Sign-in Prompt
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Account Authentication Required",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9)
                                )
                                Text(
                                    text = "Create an account or sign in with your password to anchor and retrieve your personal backup file.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCreateAccountDialog = true },
                                enabled = isConfigured && !isAuthenticating,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_open_create_account"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Create Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { showSignInDialog = true },
                                enabled = isConfigured && !isAuthenticating,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("btn_open_sign_in"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF38BDF8)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Cloud Status Feedback Message
            AnimatedVisibility(
                visible = cloudStatus != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                cloudStatus?.let { status ->
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
                                onClick = { viewModel.dismissCloudBackupStatus() },
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

            // Action Buttons: Backup to Cloud & Restore from Cloud
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Backup Button
                Button(
                    onClick = {
                        if (!isLoggedIn || !isSessionActive) {
                            showSignInDialog = true
                        } else {
                            viewModel.backupToCloud(context)
                        }
                    },
                    enabled = isConfigured && !isBackingUp && !isRestoring,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_backup_to_cloud"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uploading...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Backup to Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Restore Button
                OutlinedButton(
                    onClick = {
                        if (!isLoggedIn || !isSessionActive) {
                            showSignInDialog = true
                        } else {
                            showRestoreConfirmDialog = true
                        }
                    },
                    enabled = isConfigured && !isBackingUp && !isRestoring,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_restore_from_cloud"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restoring...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Auto-Cloud Sync Switch
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCloudAutoBackupEnabled && isLoggedIn) NeonGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (isCloudAutoBackupEnabled && isLoggedIn) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Automatic Cloud Sync",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoggedIn) "Auto-syncs backup to account '$username' daily" else "Requires cloud account login",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isCloudAutoBackupEnabled && isLoggedIn,
                    onCheckedChange = {
                        if (!isLoggedIn) {
                            showSignInDialog = true
                        } else {
                            viewModel.setCloudAutoBackupEnabled(it)
                        }
                    },
                    enabled = isConfigured,
                    modifier = Modifier.testTag("switch_cloud_auto_backup"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }

    // --- DIALOG: Create Cloud Account ---
    if (showCreateAccountDialog) {
        val currentProfileName = viewModel.userProfile.collectAsState().value.name
        var inputUser by remember { mutableStateOf(username.ifBlank { currentProfileName.ifBlank { "" } }) }
        var inputEmail by remember { mutableStateOf("") }
        var inputPass by remember { mutableStateOf("") }
        var inputConfirmPass by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isAuthenticating) showCreateAccountDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Create Cloud Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Set up your gamer account and password (min 6 characters). Your display name will match your profile name and be reserved so nobody else can use it.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputUser,
                        onValueChange = { inputUser = it; errorMessage = null },
                        label = { Text("Gamer Tag / Profile Name") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_account_user"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it; errorMessage = null },
                        label = { Text("Cloud Account Email") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_account_email"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inputPass,
                        onValueChange = { inputPass = it; errorMessage = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_account_pass"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inputConfirmPass,
                        onValueChange = { inputConfirmPass = it; errorMessage = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_create_account_confirm_pass"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = inputUser.trim()
                        val e = inputEmail.trim()
                        val p = inputPass
                        if (u.length < 3) {
                            errorMessage = "Account name must be at least 3 characters."
                            return@Button
                        }
                        if (e.isBlank() || !e.contains("@") || !e.contains(".")) {
                            errorMessage = "Please enter a valid email address (e.g. name@example.com)."
                            return@Button
                        }
                        if (p.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }
                        if (p != inputConfirmPass) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        viewModel.createCloudAccount(u, p, context, email = e) { success, msg ->
                            if (success) {
                                showCreateAccountDialog = false
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_submit_create_account")
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Create & Connect", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateAccountDialog = false },
                    enabled = !isAuthenticating
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- DIALOG: Sign In to Cloud Account ---
    if (showSignInDialog) {
        var inputUser by remember { mutableStateOf(username) }
        var inputEmail by remember { mutableStateOf("") }
        var inputPass by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isAuthenticating) showSignInDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Sign In to Cloud Backup", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter your account and password to connect your personal backup file.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputUser,
                        onValueChange = { inputUser = it; errorMessage = null },
                        label = { Text("Account Name or Email") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signin_user"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it; errorMessage = null },
                        label = { Text("Cloud Account Email") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signin_email"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = inputPass,
                        onValueChange = { inputPass = it; errorMessage = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signin_pass"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            fontSize = 11.sp,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showSignInDialog = false
                                showCreateAccountDialog = true
                            }
                        ) {
                            Text("Don't have an account? Create one", fontSize = 11.sp, color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = inputUser.trim()
                        val e = inputEmail.trim()
                        val p = inputPass
                        if (u.isBlank() || p.isBlank()) {
                            errorMessage = "Please enter your username and password."
                            return@Button
                        }
                        viewModel.signInCloudAccount(u, p, context, email = e) { success, msg ->
                            if (success) {
                                showSignInDialog = false
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_submit_sign_in")
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignInDialog = false },
                    enabled = !isAuthenticating
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- DIALOG: Restore Confirmation ---
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Restore Account Backup?")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This will locate and download the backup specifically linked to account '$username'.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• Account: $username\n• Preserves local data while merging cloud saves\n• Restores claimed games, play statuses, and profile",
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
                        viewModel.restoreFromCloud(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_confirm_account_restore")
                ) {
                    Text("Restore from Account", fontWeight = FontWeight.Bold)
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

    // --- DIALOG: Sign Out Confirmation ---
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = "Sign out of account '$username'? Your local saved giveaways and device data will stay intact, but cloud sync and backups will be paused until you sign back in.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        viewModel.signOutCloudAccount(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_sign_out_dialog_btn")
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
