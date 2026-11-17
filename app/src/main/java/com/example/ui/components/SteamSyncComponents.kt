package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonGreen
import com.example.util.LocalLowEndOptimization

@Composable
internal fun SteamTrackerBanner(
    isSyncing: Boolean,
    steamUserId: String,
    onOpenDialog: () -> Unit,
    onQuickSync: () -> Unit
) {
    val s = LocalAppStrings.current
    val lowEnd = LocalLowEndOptimization.current
    val spinAngle = if (isSyncing && !lowEnd) {
        val infiniteTransition = rememberInfiniteTransition(label = "steam_spin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
            label = "spin_angle"
        ).value
    } else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).clickable(onClick = onOpenDialog),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF66C0F4).copy(alpha = 0.14f))
                    .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = "Steam", tint = Color(0xFF66C0F4), modifier = Modifier.size(20.dp))
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(s.library.steamTrackerTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF66C0F4).copy(alpha = 0.16f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (steamUserId.isNotBlank()) s.library.steamLinkedBadge else s.library.steamApiBadge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF66C0F4)
                        )
                    }
                }
                Text(
                    text = if (steamUserId.isNotBlank()) {
                        s.library.steamLinkedSubtitleFormat.format(steamUserId)
                    } else {
                        s.library.steamUnlinkedSubtitle
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = { if (steamUserId.isNotBlank()) onQuickSync() else onOpenDialog() },
                enabled = !isSyncing,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .testTag("steam_quick_sync_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = s.library.steamSyncCd,
                    tint = if (isSyncing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp).then(if (isSyncing && !lowEnd) Modifier.rotate(spinAngle) else Modifier)
                )
            }

            IconButton(
                onClick = onOpenDialog,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .testTag("steam_settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = s.library.steamSettingsCd, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SteamSyncDialog(
    currentUserId: String,
    currentApiKey: String,
    isSyncing: Boolean,
    statusMessage: String?,
    onDismiss: () -> Unit,
    onSync: (userId: String, apiKey: String) -> Unit
) {
    val s = LocalAppStrings.current
    var userIdInput by remember(currentUserId) { mutableStateOf(currentUserId) }
    var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }
    var showAdvancedKey by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF66C0F4),
        unfocusedBorderColor = Color(0xFF2A475E),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF0E141B),
        unfocusedContainerColor = Color(0xFF0E141B)
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, Color(0xFF66C0F4).copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
            color = Color(0xFF171A21),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B2838))
                                .border(1.dp, Color(0xFF66C0F4), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF66C0F4), modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(s.library.steamTrackerTitle, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(s.library.steamDialogSubtitle, fontSize = 12.sp, color = Color(0xFF8F98A0))
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = s.common.close, tint = Color(0xFF8F98A0))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1B2838))
                        .border(1.dp, Color(0xFF2A475E), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(s.library.steamHowCategorizeTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66C0F4))
                        Text(
                            text = s.library.steamHowCategorizeBody,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFC7D5E0)
                        )
                    }
                }

                OutlinedTextField(
                    value = userIdInput,
                    onValueChange = { userIdInput = it },
                    label = { Text(s.library.steamIdLabel, color = Color(0xFF8F98A0)) },
                    placeholder = { Text(s.library.steamIdPlaceholder, color = Color(0xFF667382)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF66C0F4)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("steam_user_id_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF131B23))
                        .border(1.dp, Color(0xFF2A475E).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(s.library.steamHowToFindTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66C0F4))
                        Text(
                            text = s.library.steamHowToFindBody,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFFC7D5E0)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showAdvancedKey = !showAdvancedKey },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showAdvancedKey) s.library.steamHideCustomKey else s.library.steamShowCustomKey,
                        fontSize = 11.sp,
                        color = Color(0xFF66C0F4),
                        fontWeight = FontWeight.Medium
                    )
                }

                AnimatedVisibility(visible = showAdvancedKey) {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text(s.library.steamCustomKeyLabel, color = Color(0xFF8F98A0)) },
                        placeholder = { Text(s.library.steamCustomKeyPlaceholder, color = Color(0xFF667382)) },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF66C0F4)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("steam_custom_api_key_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors
                    )
                }

                if (statusMessage != null) {
                    val isError = statusMessage.contains("failed", ignoreCase = true) ||
                        statusMessage.contains("error", ignoreCase = true) ||
                        statusMessage.contains("private", ignoreCase = true) ||
                        statusMessage.contains("could not", ignoreCase = true) ||
                        statusMessage.contains("fall", ignoreCase = true) ||
                        statusMessage.contains("privad", ignoreCase = true) ||
                        statusMessage.contains("no se pudo", ignoreCase = true)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isError) Color(0xFF3B181E) else Color(0xFF0F382A),
                        border = BorderStroke(1.dp, if (isError) Color(0xFFE53935) else NeonGreen)
                    ) {
                        Text(
                            text = statusMessage,
                            fontSize = 11.sp,
                            color = if (isError) Color(0xFFFFB4AB) else NeonGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 15.sp
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC7D5E0)),
                        border = BorderStroke(1.dp, Color(0xFF2A475E))
                    ) {
                        Text(s.common.close)
                    }

                    Button(
                        onClick = { onSync(userIdInput, apiKeyInput) },
                        enabled = !isSyncing && userIdInput.isNotBlank(),
                        modifier = Modifier.weight(1.4f).testTag("steam_sync_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF66C0F4),
                            contentColor = Color(0xFF0B0F19),
                            disabledContainerColor = Color(0xFF2A475E),
                            disabledContentColor = Color(0xFF8F98A0)
                        )
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF0B0F19))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.library.steamSyncingBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.library.steamSyncBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
