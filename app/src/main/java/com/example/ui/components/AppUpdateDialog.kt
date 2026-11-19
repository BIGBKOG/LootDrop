package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppUpdateInfo
import com.example.data.model.UpdateDownloadState
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppUpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit,
    onInstallApk: () -> Unit,
    onViewReleases: (url: String) -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val themePrimary = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("app_update_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
            border = BorderStroke(1.2.dp, themePrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(themePrimary.copy(alpha = 0.2f))
                                .border(1.dp, themePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (downloadState) {
                                    is UpdateDownloadState.ReadyToInstall -> Icons.Default.CheckCircle
                                    is UpdateDownloadState.Downloading -> Icons.Default.CloudDownload
                                    else -> Icons.Default.NewReleases
                                },
                                contentDescription = null,
                                tint = when (downloadState) {
                                    is UpdateDownloadState.ReadyToInstall -> NeonGreen
                                    else -> themePrimary
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (downloadState) {
                                            is UpdateDownloadState.ReadyToInstall -> NeonGreen.copy(alpha = 0.15f)
                                            is UpdateDownloadState.Downloading -> themePrimary.copy(alpha = 0.15f)
                                            else -> NeonGreen.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (downloadState) {
                                        is UpdateDownloadState.Downloading -> when (lang) {
                                            AppLanguage.SPANISH -> "DESCARGANDO EN LA APP..."
                                            AppLanguage.FRENCH -> "TÉLÉCHARGEMENT..."
                                            AppLanguage.ENGLISH -> "DOWNLOADING IN-APP..."
                                        }
                                        is UpdateDownloadState.ReadyToInstall -> when (lang) {
                                            AppLanguage.SPANISH -> "LISTO PARA INSTALAR"
                                            AppLanguage.FRENCH -> "PRÊT À INSTALLER"
                                            AppLanguage.ENGLISH -> "READY TO INSTALL"
                                        }
                                        is UpdateDownloadState.Error -> when (lang) {
                                            AppLanguage.SPANISH -> "ERROR DE DESCARGA"
                                            AppLanguage.FRENCH -> "ÉCHEC DU TÉLÉCHARGEMENT"
                                            AppLanguage.ENGLISH -> "DOWNLOAD FAILED"
                                        }
                                        else -> if (updateInfo.hasUpdate) s.dialogs.updateAvailableTitle.uppercase() else when (lang) {
                                            AppLanguage.SPANISH -> "VERSIÓN INSTALADA"
                                            AppLanguage.FRENCH -> "VERSION INSTALLÉE"
                                            AppLanguage.ENGLISH -> "RELEASE INSTALLED"
                                        }
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (downloadState) {
                                        is UpdateDownloadState.ReadyToInstall -> NeonGreen
                                        is UpdateDownloadState.Error -> Color(0xFFFF5252)
                                        else -> themePrimary
                                    },
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = "LootDrop ${updateInfo.latestVersion}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_update_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.common.close,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Version Transition Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1A2030),
                    border = BorderStroke(1.dp, Color(0xFF26334D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "ACTUAL"
                                    AppLanguage.FRENCH -> "ACTUELLE"
                                    AppLanguage.ENGLISH -> "CURRENT"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(text = "v${updateInfo.currentVersion}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = themePrimary,
                            modifier = Modifier.size(18.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "ÚLTIMA EN GITHUB"
                                    AppLanguage.FRENCH -> "DERNIÈRE GITHUB"
                                    AppLanguage.ENGLISH -> "LATEST GITHUB"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = updateInfo.latestVersion,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Release Title & Notes
                if (updateInfo.releaseTitle.isNotBlank()) {
                    Text(
                        text = updateInfo.releaseTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (updateInfo.releaseNotes.isNotBlank()) {
                    Text(
                        text = s.dialogs.updateWhatsNew,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMuted,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F121B))
                            .border(1.dp, Color(0xFF1E2638), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = updateInfo.releaseNotes,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // IN-APP DOWNLOAD PROGRESS HUD (When Downloading)
                when (downloadState) {
                    is UpdateDownloadState.Downloading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF171E2D))
                                .border(1.dp, themePrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("update_download_progress_card")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = themePrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.SPANISH -> "Descargando APK en la app..."
                                                AppLanguage.FRENCH -> "Téléchargement de l'APK..."
                                                AppLanguage.ENGLISH -> "Downloading APK in app..."
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = if (downloadState.totalBytes > 0L) {
                                            "${(downloadState.progress * 100).toInt()}%"
                                        } else {
                                            "..."
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = themePrimary
                                    )
                                }

                                if (downloadState.totalBytes > 0L) {
                                    LinearProgressIndicator(
                                        progress = { downloadState.progress.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = themePrimary,
                                        trackColor = Color(0xFF26334D)
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = themePrimary,
                                        trackColor = Color(0xFF26334D)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val downloadedMb = downloadState.bytesDownloaded / 1_048_576.0
                                    val totalMb = downloadState.totalBytes / 1_048_576.0
                                    Text(
                                        text = if (downloadState.totalBytes > 0) {
                                            "${"%.1f".format(downloadedMb)} MB / ${"%.1f".format(totalMb)} MB"
                                        } else {
                                            "${"%.1f".format(downloadedMb)} MB"
                                        },
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.SPANISH -> "Se abrirá el instalador al terminar"
                                            AppLanguage.FRENCH -> "L'installateur s'ouvrira à la fin"
                                            AppLanguage.ENGLISH -> "Will prompt installer when finished"
                                        },
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonGreen.copy(alpha = 0.1f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("update_ready_to_install_card")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = s.dialogs.updateReadyToInstall,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.SPANISH -> "Pulsa el botón inferior para iniciar el instalador"
                                            AppLanguage.FRENCH -> "Appuyez ci-dessous pour lancer l'installateur"
                                            AppLanguage.ENGLISH -> "Tap button below to launch the package installer"
                                        },
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    is UpdateDownloadState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2D1619))
                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.SPANISH -> "Error de descarga en la app"
                                            AppLanguage.FRENCH -> "Erreur de téléchargement"
                                            AppLanguage.ENGLISH -> "In-App Download Error"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF5252)
                                    )
                                    Text(
                                        text = downloadState.message,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    else -> {}
                }

                // Primary Action Button
                when (downloadState) {
                    is UpdateDownloadState.Downloading -> {
                        Button(
                            onClick = { /* In progress */ },
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("downloading_in_progress_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF26334D))
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Descargando y preparando instalador..."
                                    AppLanguage.FRENCH -> "Téléchargement et préparation..."
                                    AppLanguage.ENGLISH -> "Downloading & Preparing Installer..."
                                },
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Button(
                            onClick = onInstallApk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("install_update_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.dialogs.updateInstallNow,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    else -> {
                        Button(
                            onClick = onStartDownload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("download_update_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (updateInfo.hasUpdate) NeonGreen else themePrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    downloadState is UpdateDownloadState.Error -> when (lang) {
                                        AppLanguage.SPANISH -> "Reintentar Descarga e Instalación"
                                        AppLanguage.FRENCH -> "Réessayer l'installation"
                                        AppLanguage.ENGLISH -> "Retry Update & Install"
                                    }
                                    !updateInfo.hasUpdate -> when (lang) {
                                        AppLanguage.SPANISH -> "Actualizar y Reinstalar (${updateInfo.latestVersion})"
                                        AppLanguage.FRENCH -> "Réinstaller (${updateInfo.latestVersion})"
                                        AppLanguage.ENGLISH -> "Update & Reinstall (${updateInfo.latestVersion})"
                                    }
                                    else -> when (lang) {
                                        AppLanguage.SPANISH -> "Descargar e Instalar (${updateInfo.latestVersion})"
                                        AppLanguage.FRENCH -> "Télécharger et installer (${updateInfo.latestVersion})"
                                        AppLanguage.ENGLISH -> "Update & Install (${updateInfo.latestVersion})"
                                    }
                                },
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onViewReleases(updateInfo.releaseUrl) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("view_github_release_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF26334D)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Ver Notas"
                                AppLanguage.FRENCH -> "Voir les notes"
                                AppLanguage.ENGLISH -> "View Notes"
                            },
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("dismiss_update_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2B3347)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Text(s.dialogs.updateLater, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
