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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.GiveawaysViewModel

@Composable
fun StorageSettingsCategoryContent(
    viewModel: GiveawaysViewModel,
    onRequestResyncDialog: () -> Unit,
    onRequestClearClaimedDialog: () -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val storageMetrics by viewModel.storageMetrics.collectAsState()
    val isDataSaverMode by viewModel.isDataSaverMode.collectAsState()
    val isAutoPruneExpired by viewModel.isAutoPruneExpired.collectAsState()

    // SECTION: Storage & Speed Optimization Suite
    SectionHeader(
        title = when (lang) {
            AppLanguage.SPANISH -> "ACELERADOR DE ALMACENAMIENTO Y RENDIMIENTO"
            AppLanguage.FRENCH -> "ACCÉLÉRATEUR DE STOCKAGE & PERFORMANCES"
            AppLanguage.ENGLISH -> "STORAGE & PERFORMANCE ACCELERATOR"
        },
        icon = Icons.Default.Speed
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("storage_accelerator_card"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Live Storage Metrics Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = s.settings.storageTotalFootprintLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = storageMetrics.formattedTotal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "OPTIMIZADO"
                        AppLanguage.FRENCH -> "OPTIMISÉ"
                        AppLanguage.ENGLISH -> "OPTIMIZED"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }

        // Storage Breakdown Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image Cache Chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "IMÁGENES"
                            AppLanguage.FRENCH -> "IMAGES"
                            AppLanguage.ENGLISH -> "IMAGES"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = storageMetrics.formattedImageCache,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // HTTP Network Cache Chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "RED"
                            AppLanguage.FRENCH -> "RÉSEAU"
                            AppLanguage.ENGLISH -> "NETWORK"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = storageMetrics.formattedHttpCache,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // SQLite Database Chip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "BASE DE DATOS"
                            AppLanguage.FRENCH -> "BASE DE DONNÉES"
                            AppLanguage.ENGLISH -> "DATABASE"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = storageMetrics.formattedDatabase,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Primary 1-Tap Deep Clean & VACUUM Action
        Button(
            onClick = { viewModel.performDeepClean() },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("deep_clean_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = s.settings.storageDeepCleanBtn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Granular Cache Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.clearImageCache() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("clear_image_cache_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Limpiar Imágenes"
                            AppLanguage.FRENCH -> "Vider Images"
                            AppLanguage.ENGLISH -> "Clear Images"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.optimizeDatabase() },
                modifier = Modifier
                    .weight(1f)
                    .testTag("compact_database_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "Compactar BD"
                            AppLanguage.FRENCH -> "Compacter BD"
                            AppLanguage.ENGLISH -> "Compact DB"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        // Data Saver & Low Memory Mode Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.settings.storageDataSaverTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.settings.storageDataSaverSub,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isDataSaverMode,
                onCheckedChange = { viewModel.setDataSaverMode(it) },
                modifier = Modifier.testTag("switch_data_saver_mode"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        // Auto-Prune Expired Freebies Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.settings.storageAutoPruneTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.settings.storageAutoPruneSub,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isAutoPruneExpired,
                onCheckedChange = { viewModel.setAutoPruneExpired(it) },
                modifier = Modifier.testTag("switch_auto_prune_expired"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION: Cloud Backup & Sync (Cloudflare R2 Object Storage)
    SectionHeader(
        title = when (lang) {
            AppLanguage.SPANISH -> "RESPALDO EN LA NUBE (CLOUDFLARE R2)"
            AppLanguage.FRENCH -> "SAUVEGARDE CLOUD (CLOUDFLARE R2)"
            AppLanguage.ENGLISH -> "CLOUD BACKUP (CLOUDFLARE R2)"
        },
        icon = Icons.Default.Cloud
    )

    Spacer(modifier = Modifier.height(8.dp))

    CloudBackupCard(viewModel = viewModel)

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION: Local File Backup & Restore
    SectionHeader(
        title = s.settings.backupLocalTitle,
        icon = Icons.Default.Sync
    )

    Spacer(modifier = Modifier.height(8.dp))

    BackupRestoreCard(viewModel = viewModel)

    Spacer(modifier = Modifier.height(20.dp))

    // SECTION: Library & Data Reset
    SectionHeader(
        title = when (lang) {
            AppLanguage.SPANISH -> "GESTIÓN DE DATOS Y BIBLIOTECA"
            AppLanguage.FRENCH -> "GESTION DES DONNÉES & BIBLIOTHÈQUE"
            AppLanguage.ENGLISH -> "DATA & LIBRARY MANAGEMENT"
        },
        icon = Icons.Default.Delete
    )

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onRequestResyncDialog,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Forzar Re-sincronización desde APIs de Tiendas"
                        AppLanguage.FRENCH -> "Forcer la Resynchronisation des API Boutiques"
                        AppLanguage.ENGLISH -> "Force Re-sync All Freebies from Store APIs"
                    },
                    fontSize = 13.sp
                )
            }
        }

        OutlinedButton(
            onClick = onRequestClearClaimedDialog,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = when (lang) {
                    AppLanguage.SPANISH -> "Limpiar Juegos Reclamados 'En Bóveda'"
                    AppLanguage.FRENCH -> "Vider les Jeux Réclamés 'Dans le Coffre'"
                    AppLanguage.ENGLISH -> "Clear 'In Library' Claimed Games"
                },
                fontSize = 13.sp
            )
        }
    }
}
