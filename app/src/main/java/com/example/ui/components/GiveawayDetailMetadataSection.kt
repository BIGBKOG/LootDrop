package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GiveawayItem
import com.example.data.model.LauncherOption
import com.example.data.model.SessionStyle
import com.example.data.model.StorageTier
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDisplayName
import com.example.i18n.localizedLabel
import com.example.util.OfficialOstInfo
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
internal fun DetailHardwareReadinessSection(
    item: GiveawayItem,
    onEditStorageClick: () -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val hardware = item.hardwareProfile
    Text(s.dialogs.detailHardwareReadinessTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1522))
            .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.SportsEsports, null, tint = Color(0xFF66C0F4), modifier = Modifier.size(16.dp))
                    Text("Steam Deck", fontSize = 12.sp, color = TextMuted)
                }
                SteamDeckStatusPill(status = hardware.deckStatus, fontSizeSp = 11, horizontalPad = 8.dp, verticalPad = 3.dp, bold = true)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s.library.hardwareControllerTitle, fontSize = 12.sp, color = TextMuted)
                Text(hardware.controllerSupport.localizedLabel(s), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF66C0F4))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s.library.hardwareSessionTitle, fontSize = 12.sp, color = TextMuted)
                Text(
                    text = hardware.sessionStyle.localizedLabel(s),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hardware.sessionStyle == SessionStyle.QUICK_SESSION) Color(0xFFF59E0B) else TextPrimary
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.SdStorage, null, tint = Color(0xFFA78BFA), modifier = Modifier.size(16.dp))
                    Text(s.library.hardwareStorageTitle, fontSize = 12.sp, color = TextMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        val tierLabel = when (lang) {
                            AppLanguage.SPANISH -> when (hardware.storageTier) {
                                StorageTier.FEATHERWEIGHT -> "Ligero"
                                StorageTier.COMPACT -> "Compacto"
                                StorageTier.MEDIUM -> "Medio"
                                StorageTier.HEAVYWEIGHT -> "Pesado"
                            }
                            AppLanguage.FRENCH -> when (hardware.storageTier) {
                                StorageTier.FEATHERWEIGHT -> "Léger"
                                StorageTier.COMPACT -> "Compact"
                                StorageTier.MEDIUM -> "Moyen"
                                StorageTier.HEAVYWEIGHT -> "Lourd"
                            }
                            AppLanguage.ENGLISH -> hardware.storageTier.label
                        }
                        Text("${hardware.formattedSize} ($tierLabel)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                        Text(
                            text = if (hardware.isManualOverride) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Tamaño manual"
                                    AppLanguage.FRENCH -> "Taille manuelle"
                                    AppLanguage.ENGLISH -> "Manual override"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Estimado catálogo"
                                    AppLanguage.FRENCH -> "Estimation catalogue"
                                    AppLanguage.ENGLISH -> "Catalog estimate"
                                }
                            },
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = onEditStorageClick,
                        modifier = Modifier.height(30.dp).testTag("detail_edit_storage_btn"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = s.library.hardwareSetCustomSize, tint = Color(0xFFA78BFA), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Editar"
                                AppLanguage.FRENCH -> "Modifier"
                                AppLanguage.ENGLISH -> "Edit"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFA78BFA)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailMetadataBox(
    item: GiveawayItem,
    isOstLoading: Boolean,
    ostInfo: OfficialOstInfo?
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1522))
            .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Plataformas"
                        AppLanguage.FRENCH -> "Plateformes"
                        AppLanguage.ENGLISH -> "Platforms"
                    },
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(item.platforms, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
            if (item.categories.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(s.dialogs.detailCategoriesLabel, fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = item.categories.joinToString(", ") { it.localizedDisplayName(s) },
                        fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 210.dp)
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetacriticScorePill(score = item.metacriticScore)
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Nota Metacritic"
                            AppLanguage.FRENCH -> "Score Metacritic"
                            AppLanguage.ENGLISH -> "Metacritic Score"
                        },
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = "${item.metacriticScore}/100 • ${item.metacriticInfo.tier.localizedLabel(s)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.metacriticInfo.tier.badgeColor
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(s.dialogs.detailEndsLabel, fontSize = 12.sp, color = TextMuted)
                Text(item.endDate, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NeonGreen)
            }
            Row(
                modifier = Modifier.fillMaxWidth().testTag("detail_ost_row"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Banda Sonora (OST)"
                        AppLanguage.FRENCH -> "Bande originale (OST)"
                        AppLanguage.ENGLISH -> "Official OST"
                    },
                    fontSize = 12.sp,
                    color = TextMuted
                )
                val ostLabel = when {
                    isOstLoading -> when (lang) {
                        AppLanguage.SPANISH -> "Consultando bases..."
                        AppLanguage.FRENCH -> "Recherche en cours..."
                        AppLanguage.ENGLISH -> "Checking databases..."
                    }
                    ostInfo != null -> if (!ostInfo.discogsUrl.isNullOrBlank()) "Spotify, YT & Discogs" else "YouTube Music & Spotify"
                    else -> when (lang) {
                        AppLanguage.SPANISH -> "Sin OST"
                        AppLanguage.FRENCH -> "Aucune OST"
                        AppLanguage.ENGLISH -> "No OST"
                    }
                }
                Text(
                    text = ostLabel, fontSize = 12.sp,
                    fontWeight = if (ostInfo != null) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (ostInfo != null) NeonCyan else Color(0xFF94A3B8)
                )
            }
            if (item.users > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Reclamado por"
                            AppLanguage.FRENCH -> "Réclamé par"
                            AppLanguage.ENGLISH -> "Claimed by"
                        },
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "${item.users} jugadores"
                            AppLanguage.FRENCH -> "${item.users} joueurs"
                            AppLanguage.ENGLISH -> "${item.users} gamers"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
internal fun DetailStorageFootprintDialog(
    item: GiveawayItem,
    storageInputText: String,
    onInputChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Float?) -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SdStorage, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(s.library.hardwareSetCustomSize, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(item.title, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Lleva el control exacto del tamaño de instalación en tu Steam Deck, ROG Ally o PC. Introduce gigabytes (GB) o restablece al estimado (${item.hardwareProfile.estimatedSizeGb} GB)."
                        AppLanguage.FRENCH -> "Suivez la taille exacte d'installation sur votre Steam Deck, ROG Ally ou PC. Saisissez les gigaoctets (Go) ou réinitialisez l'estimation (${item.hardwareProfile.estimatedSizeGb} Go)."
                        AppLanguage.ENGLISH -> "Track the exact install size on your Steam Deck, ROG Ally, or PC drive. Enter manual gigabytes (GB) or reset to restore estimated size (${item.hardwareProfile.estimatedSizeGb} GB)."
                    },
                    fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp
                )
                OutlinedTextField(
                    value = storageInputText,
                    onValueChange = onInputChange,
                    label = { Text(s.library.hardwareEnterGbLabel) },
                    placeholder = {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "ej. 15.5"
                                AppLanguage.FRENCH -> "ex. 15.5"
                                AppLanguage.ENGLISH -> "e.g. 15.5"
                            }
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    if (item.manualInstallSizeGb != null) {
                        TextButton(onClick = { onSave(null) }) { Text(s.library.hardwareResetAuto, color = Color(0xFFEF4444)) }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) { Text(s.common.cancel) }
                    Button(
                        onClick = { onSave(storageInputText.trim().toFloatOrNull()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text(s.common.save) }
                }
            }
        }
    }
}

@Composable
internal fun DetailLauncherCardRow(launcherOption: LauncherOption, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    val lang = s.language
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.965f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "detail_launcher_scale")
    val optColor = Color(launcherOption.badgeColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, optColor.copy(alpha = if (isPressed) 0.85f else 0.5f), RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .testTag("detail_launcher_${launcherOption.name.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = GamingDarkSurfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(optColor))
                Column {
                    Text(launcherOption.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(launcherOption.subtitle, fontSize = 11.sp, color = TextSecondary)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(optColor.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Reclamar"
                            AppLanguage.FRENCH -> "Réclamer"
                            AppLanguage.ENGLISH -> "Claim"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = optColor
                    )
                }
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = optColor, modifier = Modifier.size(14.dp))
            }
        }
    }
}
