package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import com.example.data.model.UpdateDownloadState
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.NeonGreen
import java.util.Locale

@Composable
internal fun GithubUpdatesSection(
    appUpdateInfo: AppUpdateInfo?,
    isCheckingUpdates: Boolean,
    updateCheckStatus: String?,
    updateDownloadState: UpdateDownloadState,
    viewModel: GiveawaysViewModel,
    context: Context
) {
    val s = LocalAppStrings.current
    val lang = s.language
    SectionHeader(title = s.settings.githubSectionTitle, icon = Icons.Default.NewReleases)

    Spacer(modifier = Modifier.height(8.dp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("github_releases_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.settings.githubCurrentVersionFormat.format(BuildConfig.VERSION_NAME),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Origen: github.com/BIGBKOG/LootDrop"
                            AppLanguage.FRENCH -> "Source : github.com/BIGBKOG/LootDrop"
                            AppLanguage.ENGLISH -> "Upstream: github.com/BIGBKOG/LootDrop"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (appUpdateInfo?.hasUpdate == true) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.2f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "NUEVA: ${appUpdateInfo.latestVersion}"
                                AppLanguage.FRENCH -> "NOUVELLE : ${appUpdateInfo.latestVersion}"
                                AppLanguage.ENGLISH -> "NEW: ${appUpdateInfo.latestVersion}"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                    }
                } else if (appUpdateInfo != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "ÚLTIMA: ${appUpdateInfo.latestVersion}"
                                AppLanguage.FRENCH -> "DERNIÈRE : ${appUpdateInfo.latestVersion}"
                                AppLanguage.ENGLISH -> "LATEST: ${appUpdateInfo.latestVersion}"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (updateCheckStatus != null) {
                Text(
                    text = updateCheckStatus,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (appUpdateInfo?.hasUpdate == true) NeonGreen else MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.checkGithubReleases(isManual = true) },
                    enabled = !isCheckingUpdates,
                    modifier = Modifier.weight(1f).testTag("check_updates_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.settings.githubCheckingBtn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.settings.githubCheckNowBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                OutlinedButton(
                    onClick = {
                        val target = appUpdateInfo?.releaseUrl?.ifBlank { "https://github.com/BIGBKOG/LootDrop/releases" }
                            ?: "https://github.com/BIGBKOG/LootDrop/releases"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(browserIntent)
                    },
                    modifier = Modifier.weight(1f).testTag("view_github_repo_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Lanzamientos"
                            AppLanguage.FRENCH -> "Versions"
                            AppLanguage.ENGLISH -> "Releases"
                        },
                        fontSize = 11.sp
                    )
                }
            }

            when (updateDownloadState) {
                is UpdateDownloadState.Downloading -> {
                    UpdateDownloadingCard(updateDownloadState)
                }
                is UpdateDownloadState.ReadyToInstall -> {
                    Button(
                        onClick = { viewModel.installDownloadedApk(context) },
                        modifier = Modifier.fillMaxWidth().testTag("install_ready_update_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Instalar Actualización (${updateDownloadState.versionTag})"
                                AppLanguage.FRENCH -> "Installer la mise à jour (${updateDownloadState.versionTag})"
                                AppLanguage.ENGLISH -> "Install Downloaded Update (${updateDownloadState.versionTag})"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
                is UpdateDownloadState.Error -> {
                    UpdateErrorCard(
                        message = updateDownloadState.message,
                        onRetry = { appUpdateInfo?.let { viewModel.startInAppUpdateDownload(it, context) } },
                        onDismiss = { viewModel.resetUpdateDownloadState() }
                    )
                }
                else -> {
                    val info = appUpdateInfo
                    if (info?.hasUpdate == true) {
                        UpdateActionRow(
                            buttonText = when (lang) {
                                AppLanguage.SPANISH -> "Actualizar e Instalar (${info.latestVersion})"
                                AppLanguage.FRENCH -> "Mettre à jour (${info.latestVersion})"
                                AppLanguage.ENGLISH -> "Update & Install (${info.latestVersion})"
                            },
                            notesLabel = when (lang) {
                                AppLanguage.SPANISH -> "Notas"
                                AppLanguage.FRENCH -> "Notes"
                                AppLanguage.ENGLISH -> "Notes"
                            },
                            testTag = "view_update_dialog_button",
                            containerColor = NeonGreen,
                            contentColor = Color.Black,
                            onDownload = { viewModel.startInAppUpdateDownload(info, context) },
                            onNotes = { viewModel.setUpdateDialogVisible(true) }
                        )
                    } else if (info != null && info.isApkAvailable) {
                        UpdateActionRow(
                            buttonText = when (lang) {
                                AppLanguage.SPANISH -> "Actualizar y Reinstalar (${info.latestVersion})"
                                AppLanguage.FRENCH -> "Réinstaller (${info.latestVersion})"
                                AppLanguage.ENGLISH -> "Update & Reinstall (${info.latestVersion})"
                            },
                            notesLabel = when (lang) {
                                AppLanguage.SPANISH -> "Notas"
                                AppLanguage.FRENCH -> "Notes"
                                AppLanguage.ENGLISH -> "Notes"
                            },
                            testTag = "redownload_update_dialog_button",
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            onDownload = { viewModel.startInAppUpdateDownload(info, context) },
                            onNotes = { viewModel.setUpdateDialogVisible(true) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateDownloadingCard(downloadState: UpdateDownloadState.Downloading) {
    val lang = LocalAppStrings.current.language
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("update_download_progress_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Descargando APK..."
                        AppLanguage.FRENCH -> "Téléchargement de l'APK..."
                        AppLanguage.ENGLISH -> "Downloading APK..."
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            val pct = when {
                downloadState.totalBytes > 0L && downloadState.progress > 0f -> "${(downloadState.progress * 100).toInt()}%"
                downloadState.bytesDownloaded > 0L -> when (lang) {
                    AppLanguage.SPANISH -> "Descargando..."
                    AppLanguage.FRENCH -> "Téléchargement..."
                    AppLanguage.ENGLISH -> "Downloading..."
                }
                else -> when (lang) {
                    AppLanguage.SPANISH -> "Conectando..."
                    AppLanguage.FRENCH -> "Connexion..."
                    AppLanguage.ENGLISH -> "Connecting..."
                }
            }
            Text(pct, fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }

        val downloadedMb = String.format(Locale.US, "%.1f", downloadState.bytesDownloaded / (1024f * 1024f))
        if (downloadState.totalBytes > 0L) {
            LinearProgressIndicator(
                progress = { downloadState.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = NeonGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            val totalMb = String.format(Locale.US, "%.1f", downloadState.totalBytes / (1024f * 1024f))
            Text("$downloadedMb MB / $totalMb MB", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = NeonGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = if (downloadState.bytesDownloaded > 0L) {
                    when (lang) {
                        AppLanguage.SPANISH -> "$downloadedMb MB descargados"
                        AppLanguage.FRENCH -> "$downloadedMb Mo téléchargés"
                        AppLanguage.ENGLISH -> "$downloadedMb MB downloaded"
                    }
                } else {
                    when (lang) {
                        AppLanguage.SPANISH -> "Conectando con GitHub..."
                        AppLanguage.FRENCH -> "Connexion à GitHub..."
                        AppLanguage.ENGLISH -> "Connecting to GitHub..."
                    }
                },
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UpdateErrorCard(message: String, onRetry: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalAppStrings.current
    val lang = s.language
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2D1619))
            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp)
            .testTag("update_download_error_card"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
            Text(
                when (lang) {
                    AppLanguage.SPANISH -> "Error de descarga: $message"
                    AppLanguage.FRENCH -> "Erreur de téléchargement : $message"
                    AppLanguage.ENGLISH -> "Download error: $message"
                },
                fontSize = 11.sp,
                color = Color(0xFFFF8A80)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f).height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
            ) {
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Reintentar Descarga"
                        AppLanguage.FRENCH -> "Réessayer"
                        AppLanguage.ENGLISH -> "Retry Download"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.height(34.dp), shape = RoundedCornerShape(8.dp)) {
                Text(s.common.dismiss, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun UpdateActionRow(
    buttonText: String,
    notesLabel: String,
    testTag: String,
    containerColor: Color,
    contentColor: Color,
    onDownload: () -> Unit,
    onNotes: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onDownload,
            modifier = Modifier.weight(1f).testTag(testTag),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = containerColor)
        ) {
            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
        OutlinedButton(
            onClick = onNotes,
            modifier = Modifier.testTag("view_update_notes_dialog_button"),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(notesLabel, fontSize = 11.sp)
        }
    }
}
