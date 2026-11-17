package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.NotificationHistoryEntity
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxGreen
import com.example.util.LocalLowEndOptimization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val NOTIF_LOCALE_ES: Locale = Locale.forLanguageTag("es")

private enum class AlertFilter(val label: String, val spanishLabel: String) {
    ALL("All Alerts", "Todas"),
    MAJOR_GAMES("🔥 Games", "🔥 Juegos"),
    ENDING_SOON("⏳ Ending Soon", "⏳ Por Vencer"),
    DLC("🎁 DLC & Loot", "🎁 DLC y Botín")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistorySheet(
    viewModel: GiveawaysViewModel,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val context = LocalContext.current
    val lowEnd = LocalLowEndOptimization.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val history by viewModel.notificationHistory.collectAsState()
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()
    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }
    val coroutineScope = rememberCoroutineScope()

    val filteredHistory = remember(history, selectedFilter) {
        when (selectedFilter) {
            AlertFilter.ALL -> history
            AlertFilter.MAJOR_GAMES -> history.filter { it.channelType == "MAJOR_GAME" }
            AlertFilter.ENDING_SOON -> history.filter { it.channelType == "ENDING_SOON" }
            AlertFilter.DLC -> history.filter { it.channelType == "DLC" }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GamingDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("notification_history_sheet")
        ) {
            // Top Bar
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
                            .background(NeonCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.settings.alertsHistoryTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isEs) "$unreadCount NUEVAS" else "$unreadCount NEW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonGreen
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isEs) "${history.size} notificaciones registradas" else "${history.size} total notifications recorded",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("notification_sheet_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (isEs) "Ajustes de Alertas" else "Alert Settings",
                            tint = TextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.common.close,
                            tint = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action strip (Mark all as read, Clear all)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(AlertFilter.entries) { filter ->
                        val isSelected = selectedFilter == filter
                        val chipModifier = if (lowEnd) {
                            Modifier
                        } else {
                            val chipScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "alert_filter_chip_scale"
                            )
                            Modifier.graphicsLayer {
                                scaleX = chipScale
                                scaleY = chipScale
                            }
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            modifier = chipModifier,
                            label = { Text(if (isEs) filter.spanishLabel else filter.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = GamingDarkSurfaceVariant,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = GamingCardBorder,
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                if (history.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (unreadCount > 0) {
                            IconButton(
                                onClick = { viewModel.markAllNotificationsAsRead() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = s.settings.alertsMarkAllRead,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.clearAllNotificationHistory()
                                Toast.makeText(
                                    context,
                                    if (isEs) "Historial de notificaciones borrado" else "Notification history cleared",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = s.settings.alertsClearHistory,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredHistory.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GamingDarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (history.isEmpty()) {
                                if (isEs) "Sin Alertas Todavía" else "No Alerts Yet"
                            } else {
                                if (isEs) "No hay alertas en este filtro" else "No matching alerts in this filter"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (history.isEmpty()) {
                                s.settings.alertsHistoryEmpty
                            } else {
                                if (isEs) "Prueba cambiar a 'Todas' para ver el historial completo de alertas."
                                else "Try switching to 'All Alerts' to see your complete notifications log."
                            },
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.sendTestNotification(isRichPreview = true)
                                Toast.makeText(context, s.settings.alertsTestToast, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEs) "Enviar Alerta de Prueba" else "Send Rich Test Alert", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { item ->
                        NotificationHistoryCard(
                            entity = item,
                            lowEnd = lowEnd,
                            modifier = if (lowEnd) {
                                Modifier
                            } else {
                                Modifier.animateItem(
                                    fadeInSpec = tween(240),
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    fadeOutSpec = tween(180)
                                )
                            },
                            onCardClick = {
                                viewModel.markNotificationAsRead(item.id)
                                viewModel.openItemById(item.giveawayId)
                                onDismiss()
                            },
                            onClaimClick = {
                                viewModel.markNotificationAsRead(item.id)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.openGiveawayUrl)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            onBookmarkClick = {
                                viewModel.markNotificationAsRead(item.id)
                                viewModel.toggleBookmarkById(item.giveawayId)
                                Toast.makeText(
                                    context,
                                    s.system.notifBookmarkSavedToastFormat.format(item.title),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onShareClick = {
                                coroutineScope.launch {
                                    val giveaway = viewModel.allGiveaways.value.find { it.id == item.giveawayId }
                                    val fallbackShare: () -> Unit = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                s.dialogs.shareGameMessageFormat.format(
                                                    item.title,
                                                    item.worth.ifBlank { "100% OFF" },
                                                    item.platforms,
                                                    item.openGiveawayUrl
                                                )
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, s.dialogs.shareChooserTitle))
                                    }
                                    if (giveaway != null) {
                                        try {
                                            com.example.util.ShareGraphicGenerator.shareGraphicCard(context, giveaway, includeTextLink = true)
                                        } catch (_: Exception) {
                                            fallbackShare()
                                        }
                                    } else {
                                        fallbackShare()
                                    }
                                }
                            },
                            onDismissNotification = {
                                viewModel.deleteNotification(item.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationHistoryCard(
    entity: NotificationHistoryEntity,
    lowEnd: Boolean = false,
    onCardClick: () -> Unit,
    onClaimClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onDismissNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val context = LocalContext.current
    val relativeTime = remember(entity.timestamp, isEs) { formatRelativeTimestamp(entity.timestamp, isEs) }

    val interactionSource = remember { MutableInteractionSource() }
    val scaleModifier = if (lowEnd) {
        Modifier
    } else {
        val isPressed by interactionSource.collectIsPressedAsState()
        val cardScale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "notif_card_scale"
        )
        Modifier.graphicsLayer {
            scaleX = cardScale
            scaleY = cardScale
        }
    }

    val platformColor = when {
        entity.platforms.contains("PlayStation", ignoreCase = true) -> PlayStationBlue
        entity.platforms.contains("Xbox", ignoreCase = true) -> XboxGreen
        entity.platforms.contains("Nintendo", ignoreCase = true) -> NintendoRed
        entity.platforms.contains("Steam", ignoreCase = true) -> SteamBlue
        entity.platforms.contains("Epic", ignoreCase = true) -> EpicGamesBlue
        else -> NeonGreen
    }

    val thumbUrl = entity.thumbnail.ifBlank { entity.image }
    val thumbRequest = remember(context, thumbUrl, lowEnd) {
        buildGiveawayImageRequest(
            context = context,
            url = thumbUrl,
            isLowEndOptimization = lowEnd,
            width = 160,
            height = 140
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(scaleModifier)
            .clip(RoundedCornerShape(14.dp))
            .background(if (entity.isRead) GamingDarkSurfaceVariant else GamingDarkSurfaceVariant.copy(alpha = 0.95f))
            .border(
                width = if (entity.isRead) 1.dp else 1.5.dp,
                color = if (entity.isRead) GamingCardBorder else NeonCyan.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCardClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = thumbRequest,
                    contentDescription = entity.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(width = 80.dp, height = 70.dp)
                )

                // Channel badge pill on thumbnail
                val channelTag = when (entity.channelType) {
                    "ENDING_SOON" -> if (isEs) "⏳ VENCE" else "⏳ ENDING"
                    "DLC" -> "🎁 DLC"
                    else -> if (isEs) "GRATIS" else "FREE"
                }
                val channelBg = when (entity.channelType) {
                    "ENDING_SOON" -> Color(0xFFFF5252)
                    "DLC" -> Color(0xFF9C27B0)
                    else -> NeonGreen
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(channelBg.copy(alpha = 0.9f), RoundedCornerShape(topEnd = 4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = channelTag,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(platformColor)
                        )
                        Text(
                            text = entity.platforms.take(24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = platformColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = relativeTime,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        if (!entity.isRead) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = entity.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEs) "Valor: " else "Value: ",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (entity.worth.isNotBlank() && entity.worth != "N/A") entity.worth else s.system.notif100Off,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onClaimClick,
                        modifier = Modifier
                            .height(28.dp)
                            .weight(1.2f),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isEs) "Reclamar" else "Claim Free", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier
                            .height(28.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, GamingCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isEs) "Guardar" else "Save", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = s.system.notifActionShare, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

private fun formatRelativeTimestamp(timestamp: Long, isEs: Boolean): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> if (isEs) "Ahora mismo" else "Just now"
        minutes < 60 -> if (isEs) "Hace ${minutes}m" else "${minutes}m ago"
        hours < 24 -> if (isEs) "Hace ${hours}h" else "${hours}h ago"
        days == 1L -> if (isEs) "Ayer" else "Yesterday"
        days < 7 -> if (isEs) "Hace ${days}d" else "${days}d ago"
        else -> {
            val locale = if (isEs) NOTIF_LOCALE_ES else Locale.getDefault()
            val sdf = SimpleDateFormat("MMM d", locale)
            sdf.format(Date(timestamp))
        }
    }
}
