package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
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
    val s = LocalAppStrings.current
    val lang = s.language
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
    val lastKeepAliveTimestamp by viewModel.lastSupabaseKeepAliveTimestamp.collectAsState()
    val lastKeepAliveStatus by viewModel.lastSupabaseKeepAliveStatus.collectAsState()
    val isPingingSupabase by viewModel.isPingingSupabase.collectAsState()

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
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Respaldo Nube Cloudflare R2"
                                AppLanguage.FRENCH -> "Sauvegarde Cloud Cloudflare R2"
                                AppLanguage.ENGLISH -> "Cloudflare R2 Cloud Backup"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoggedIn && isSessionActive) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Conectado • Cuenta: $username"
                                    AppLanguage.FRENCH -> "Connecté • Compte : $username"
                                    AppLanguage.ENGLISH -> "Connected • Account: $username"
                                }
                            } else if (isLoggedIn) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Cuenta: $username (Pulsa para iniciar sesión)"
                                    AppLanguage.FRENCH -> "Compte : $username (Appuyez pour connecter)"
                                    AppLanguage.ENGLISH -> "Account: $username (Tap to Sign In)"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Almacenamiento Cloudflare R2 (10 GB Gratis)"
                                    AppLanguage.FRENCH -> "Stockage Cloudflare R2 (10 Go gratuits)"
                                    AppLanguage.ENGLISH -> "Cloudflare R2 Object Storage (10 GB Free)"
                                }
                            },
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
                                isLoggedIn && isSessionActive -> when (lang) {
                                    AppLanguage.SPANISH -> "R2 Sinc."
                                    AppLanguage.FRENCH -> "R2 Synchro"
                                    AppLanguage.ENGLISH -> "R2 Synced"
                                }
                                isLoggedIn -> when (lang) {
                                    AppLanguage.SPANISH -> "Requiere Sesión"
                                    AppLanguage.FRENCH -> "Connexion requise"
                                    AppLanguage.ENGLISH -> "Sign In Needed"
                                }
                                isConfigured -> when (lang) {
                                    AppLanguage.SPANISH -> "Sin Conectar"
                                    AppLanguage.FRENCH -> "Non connecté"
                                    AppLanguage.ENGLISH -> "Login Needed"
                                }
                                else -> when (lang) {
                                    AppLanguage.SPANISH -> "Sin Config."
                                    AppLanguage.FRENCH -> "Non configuré"
                                    AppLanguage.ENGLISH -> "Unconfigured"
                                }
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
                text = when (lang) {
                    AppLanguage.SPANISH -> "Los respaldos se sincronizan con tu depósito privado de Cloudflare R2. Tus juegos guardados, biblioteca y perfil permanecen protegidos entre dispositivos."
                    AppLanguage.FRENCH -> "Les sauvegardes sont synchronisées sur votre bucket privé Cloudflare R2. Vos jeux enregistrés, bibliothèque et profil restent protégés entre appareils."
                    AppLanguage.ENGLISH -> "Backups sync directly to your private Cloudflare R2 bucket. Your saved giveaways, game library, and profile remain securely backed up across devices."
                },
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
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp),
                                        tint = Color(0xFFF87171)
                                    )
                                    Text(
                                        text = s.settings.cloudSignOutBtn,
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
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Destino de Almacenamiento:"
                                    AppLanguage.FRENCH -> "Destination de stockage :"
                                    AppLanguage.ENGLISH -> "Storage Destination:"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Depósito Cloudflare R2"
                                    AppLanguage.FRENCH -> "Bucket Cloudflare R2"
                                    AppLanguage.ENGLISH -> "Cloudflare R2 Bucket"
                                },
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
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Último Respaldo de Cuenta:"
                                    AppLanguage.FRENCH -> "Dernière sauvegarde :"
                                    AppLanguage.ENGLISH -> "Last Account Backup:"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (lastBackupTimestamp > 0L) {
                                    SimpleDateFormat("MMM d, yyyy • h:mm a", lang.locale).format(Date(lastBackupTimestamp))
                                } else {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Sin respaldo subido aún"
                                        AppLanguage.FRENCH -> "Aucune sauvegarde envoyée"
                                        AppLanguage.ENGLISH -> "No backup uploaded yet"
                                    }
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
                                        text = when (lang) {
                                            AppLanguage.SPANISH -> "⚠️ Sesión desconectada"
                                            AppLanguage.FRENCH -> "⚠️ Session déconnectée"
                                            AppLanguage.ENGLISH -> "⚠️ Session disconnected"
                                        },
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
                                            Text(s.settings.cloudSignInBtn, fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                                        }
                                        TextButton(
                                            onClick = { showSignOutConfirmDialog = true },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(s.settings.cloudSignOutBtn, fontSize = 11.sp, color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
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
                                    text = when (lang) {
                                        AppLanguage.SPANISH -> "Autenticación de Cuenta Requerida"
                                        AppLanguage.FRENCH -> "Authentification requise"
                                        AppLanguage.ENGLISH -> "Account Authentication Required"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF1F5F9)
                                )
                                Text(
                                    text = when (lang) {
                                        AppLanguage.SPANISH -> "Crea una cuenta o inicia sesión con tu contraseña para guardar y recuperar tu respaldo personal."
                                        AppLanguage.FRENCH -> "Créez un compte ou connectez-vous avec votre mot de passe pour sauvegarder et récupérer votre profil."
                                        AppLanguage.ENGLISH -> "Create an account or sign in with your password to anchor and retrieve your personal backup file."
                                    },
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
                                    Text(s.settings.cloudCreateAccountBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    Text(s.settings.cloudSignInBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    contentDescription = s.common.dismiss,
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
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Subiendo..."
                                AppLanguage.FRENCH -> "Envoi..."
                                AppLanguage.ENGLISH -> "Uploading..."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(s.settings.cloudBackupNowBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Restaurando..."
                                AppLanguage.FRENCH -> "Restauration..."
                                AppLanguage.ENGLISH -> "Restoring..."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(
                                when (lang) {
                                    AppLanguage.SPANISH -> "Restaurar"
                                    AppLanguage.FRENCH -> "Restaurer"
                                    AppLanguage.ENGLISH -> "Restore"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                            text = s.settings.cloudAutoSyncTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLoggedIn) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Auto-sincroniza respaldo a la cuenta '$username' cada día"
                                    AppLanguage.FRENCH -> "Synchro auto vers le compte « $username » chaque jour"
                                    AppLanguage.ENGLISH -> "Auto-syncs backup to account '$username' daily"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Requiere iniciar sesión en cuenta nube"
                                    AppLanguage.FRENCH -> "Connexion au compte cloud requise"
                                    AppLanguage.ENGLISH -> "Requires cloud account login"
                                }
                            },
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

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Supabase Keep-Alive Status & Manual Ping Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Latido Activo de Base Supabase"
                            AppLanguage.FRENCH -> "Maintien Actif Base Supabase"
                            AppLanguage.ENGLISH -> "Supabase DB Keep-Alive"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val subtitle = when {
                        lastKeepAliveTimestamp > 0L -> {
                            val formatted = SimpleDateFormat("MMM d • h:mm a", s.language.locale).format(Date(lastKeepAliveTimestamp))
                            val statusHint = if (lastKeepAliveStatus.isNotBlank()) " ($lastKeepAliveStatus)" else ""
                            when (lang) {
                                AppLanguage.SPANISH -> "Último ping: $formatted$statusHint"
                                AppLanguage.FRENCH -> "Dernier ping : $formatted$statusHint"
                                AppLanguage.ENGLISH -> "Last ping: $formatted$statusHint"
                            }
                        }
                        lastKeepAliveStatus.isNotBlank() -> lastKeepAliveStatus
                        else -> when (lang) {
                            AppLanguage.SPANISH -> "Auto-ping cada 12h + GitHub cada 2 días"
                            AppLanguage.FRENCH -> "Auto-ping toutes les 12h + GitHub tous les 2j"
                            AppLanguage.ENGLISH -> "Auto-pings every 12h + GitHub every 2 days"
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = if (lastKeepAliveTimestamp > 0L) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = { viewModel.pingSupabaseNow(context) },
                    enabled = !isPingingSupabase,
                    modifier = Modifier
                        .testTag("btn_supabase_keepalive_ping")
                        .size(width = 88.dp, height = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                ) {
                    if (isPingingSupabase) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Ping"
                                AppLanguage.FRENCH -> "Ping BD"
                                AppLanguage.ENGLISH -> "Ping DB"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }

    if (showCreateAccountDialog) {
        CreateCloudAccountDialog(
            viewModel = viewModel,
            context = context,
            username = username,
            isAuthenticating = isAuthenticating,
            onDismiss = { showCreateAccountDialog = false }
        )
    }

    if (showSignInDialog) {
        SignInCloudAccountDialog(
            viewModel = viewModel,
            context = context,
            username = username,
            isAuthenticating = isAuthenticating,
            onDismiss = { showSignInDialog = false },
            onSwitchToCreateAccount = {
                showSignInDialog = false
                showCreateAccountDialog = true
            }
        )
    }

    if (showRestoreConfirmDialog) {
        RestoreCloudConfirmDialog(
            username = username,
            onDismiss = { showRestoreConfirmDialog = false },
            onConfirm = {
                showRestoreConfirmDialog = false
                viewModel.restoreFromCloud(context)
            }
        )
    }

    if (showSignOutConfirmDialog) {
        SignOutCloudConfirmDialog(
            username = username,
            onDismiss = { showSignOutConfirmDialog = false },
            onConfirm = {
                showSignOutConfirmDialog = false
                viewModel.signOutCloudAccount(context)
            }
        )
    }
}
