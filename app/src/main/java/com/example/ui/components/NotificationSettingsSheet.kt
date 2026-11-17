package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.notifications.NotificationHelper
import com.example.notifications.NotificationScheduler
import com.example.preferences.AppPreferences
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsSheet(
    notificationHelper: NotificationHelper,
    onDismiss: () -> Unit,
    onOpenInbox: (() -> Unit)? = null
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var isNotificationsEnabled by remember { mutableStateOf(notificationHelper.isNotificationsEnabled) }
    var isNotifyPlayStation by remember { mutableStateOf(notificationHelper.isNotifyPlayStation) }
    var isNotifyXbox by remember { mutableStateOf(notificationHelper.isNotifyXbox) }
    var isNotifyNintendo by remember { mutableStateOf(notificationHelper.isNotifyNintendo) }
    var isNotifySteam by remember { mutableStateOf(notificationHelper.isNotifySteam) }
    var isNotifyEpic by remember { mutableStateOf(notificationHelper.isNotifyEpic) }
    var isNotifyGog by remember { mutableStateOf(notificationHelper.isNotifyGog) }
    var isNotifyAndroid by remember { mutableStateOf(notificationHelper.isNotifyAndroid) }
    var isNotifyIos by remember { mutableStateOf(notificationHelper.isNotifyIos) }
    var isNotifyOthers by remember { mutableStateOf(notificationHelper.isNotifyOthers) }
    var isGamesOnly by remember { mutableStateOf(notificationHelper.isGamesOnly) }
    var isKeepForeverOnly by remember { mutableStateOf(notificationHelper.isKeepForeverOnly) }
    var isSoundEnabled by remember { mutableStateOf(notificationHelper.isNotificationSoundEnabled) }
    var isVibrateEnabled by remember { mutableStateOf(notificationHelper.isNotificationVibrateEnabled) }
    var isRichArtEnabled by remember { mutableStateOf(notificationHelper.isNotificationRichArtwork) }
    var isActionsEnabled by remember { mutableStateOf(notificationHelper.isNotificationActionsEnabled) }
    var isNotifyEndingSoon by remember { mutableStateOf(notificationHelper.isNotifyEndingSoon) }
    var minWorthThreshold by remember { mutableStateOf(notificationHelper.minWorthAlertThreshold) }
    var isQuietHours by remember { mutableStateOf(notificationHelper.isQuietHoursEnabled) }
    var syncIntervalHours by remember { mutableStateOf(appPrefs.backgroundSyncIntervalHours) }
    var wifiOnly by remember { mutableStateOf(appPrefs.backgroundSyncWifiOnly) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val msg = if (isGranted) {
            if (isEs) "¡Permiso de notificaciones concedido!" else "Notification permission granted!"
        } else {
            if (isEs) "Permiso denegado. Actívalo en los ajustes del sistema." else "Notification permission denied. Enable in system settings."
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val storeSpecs = listOf(
        StoreAlertSpec("PlayStation Store (PS5 / PS4)", if (isEs) "Ofertas y rebajas al 100% en PS Store" else "PS Store giveaways & 100% discount promotions", PlayStationBlue, isNotifyPlayStation) {
            isNotifyPlayStation = it; notificationHelper.isNotifyPlayStation = it
        },
        StoreAlertSpec("Xbox Series X|S & Xbox One", if (isEs) "Promociones gratis en tienda Xbox" else "Xbox Marketplace free game promotions", XboxGreen, isNotifyXbox) {
            isNotifyXbox = it; notificationHelper.isNotifyXbox = it
        },
        StoreAlertSpec("Nintendo Switch eShop", if (isEs) "Juegos gratis al 100% y drops en Switch" else "Nintendo Switch 100% discount freebies & drops", NintendoRed, isNotifyNintendo) {
            isNotifyNintendo = it; notificationHelper.isNotifyNintendo = it
        },
        StoreAlertSpec("Steam", if (isEs) "Promociones gratis en Steam y 100% descuento" else "Steam store free promotions & 100% off deals", SteamBlue, isNotifySteam) {
            isNotifySteam = it; notificationHelper.isNotifySteam = it
        },
        StoreAlertSpec("Epic Games Store", if (isEs) "Juegos gratis semanales y títulos misteriosos" else "Weekly free games vault & mystery deals", EpicGamesBlue, isNotifyEpic) {
            isNotifyEpic = it; notificationHelper.isNotifyEpic = it
        },
        StoreAlertSpec("GOG.com", if (isEs) "Juegos clásicos e indies sin DRM en PC" else "DRM-free PC classic and indie game drops", GogPurple, isNotifyGog) {
            isNotifyGog = it; notificationHelper.isNotifyGog = it
        },
        StoreAlertSpec("Google Play / Android", if (isEs) "Juegos móviles, apps y paquetes gratis" else "Android mobile games, apps & in-game pack promotions", Color(0xFF3DDC84), isNotifyAndroid) {
            isNotifyAndroid = it; notificationHelper.isNotifyAndroid = it
        },
        StoreAlertSpec("Apple App Store / iOS", if (isEs) "Títulos gratis y promos para iPhone y iPad" else "iOS (iPhone & iPad) free titles & giveaway promos", Color(0xFF007AFF), isNotifyIos) {
            isNotifyIos = it; notificationHelper.isNotifyIos = it
        },
        StoreAlertSpec(if (isEs) "Otros Launchers de PC" else "Other PC Launchers", "Ubisoft, itch.io, EA App, Prime Gaming", NeonGreen, isNotifyOthers) {
            isNotifyOthers = it; notificationHelper.isNotifyOthers = it
        }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GamingDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(20.dp)
                .verticalScroll(scrollState)
                .testTag("notification_settings_sheet")
        ) {
            // Header
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
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(if (isEs) "Centro de Notificaciones y Alertas" else "Notification Hub & Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (isEs) "Alertas inteligentes, portadas y canales" else "Smart push alerts, rich game artwork & channels", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = s.common.close, tint = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Switch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isEs) "Activar Alertas Inteligentes" else "Enable Smart Push Alerts", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (isEs) "Recibe alertas con portada cuando salgan juegos gratis" else "Receive rich alerts with game artwork when free deals drop", fontSize = 12.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { checked ->
                            isNotificationsEnabled = checked
                            notificationHelper.isNotificationsEnabled = checked
                            if (checked) {
                                NotificationScheduler.schedulePeriodicSync(context)
                                hasOrRequestNotificationPermission(context, permissionLauncher)
                            } else {
                                NotificationScheduler.cancelPeriodicSync(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GamingDarkBackground,
                            checkedTrackColor = NeonCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = Color(0xFF1E283C)
                        ),
                        modifier = Modifier.testTag("master_notification_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Channels Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = if (isEs) "CANALES DE NOTIFICACIÓN ACTIVOS" else "ACTIVE NOTIFICATION CHANNELS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChannelStatusBadge(if (isEs) "🔥 Juegos" else "🔥 Games", NeonCyan, Modifier.weight(1f))
                        ChannelStatusBadge(if (isEs) "⏳ Por Vencer" else "⏳ Ending Soon", Color(0xFFFF5252), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChannelStatusBadge(if (isEs) "🎁 DLC y Botín" else "🎁 DLC & Loot", Color(0xFFB388FF), Modifier.weight(1f))
                        ChannelStatusBadge(if (isEs) "🔔 Sistema" else "🔔 System Sync", NeonGreen, Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(s.settings.alertsPlatformsSection, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Column {
                    storeSpecs.forEachIndexed { idx, spec ->
                        SettingToggleRow(
                            title = spec.title,
                            subtitle = spec.subtitle,
                            color = spec.color,
                            checked = spec.checked,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = spec.onToggle,
                            titleColor = TextPrimary,
                            subtitleColor = TextSecondary,
                            thumbColor = GamingDarkBackground,
                            trackColor = NeonCyan
                        )
                        if (idx < storeSpecs.lastIndex) {
                            HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(if (isEs) "AJUSTE FINO DE ALERTAS" else "SMART ALERT TUNING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SimpleSettingSwitchRow(
                        title = s.settings.alertsEndingSoonTitle,
                        subtitle = s.settings.alertsEndingSoonSub,
                        checked = isNotifyEndingSoon,
                        enabled = isNotificationsEnabled,
                        titleFontWeight = FontWeight.Bold,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = Color(0xFFFF5252),
                        leadingContent = {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        },
                        onCheckedChange = { isNotifyEndingSoon = it; notificationHelper.isNotifyEndingSoon = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsGamesOnlyTitle,
                        subtitle = s.settings.alertsGamesOnlySub,
                        checked = isGamesOnly,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        onCheckedChange = { isGamesOnly = it; notificationHelper.isGamesOnly = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsKeepForeverTitle,
                        subtitle = s.settings.alertsKeepForeverSub,
                        checked = isKeepForeverOnly,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        onCheckedChange = { isKeepForeverOnly = it; notificationHelper.isKeepForeverOnly = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsSoundTitle,
                        subtitle = if (isEs) "Reproduce tono cuando llegan ofertas nuevas" else "Play audio chime when new giveaways drop",
                        checked = isSoundEnabled,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        onCheckedChange = { isSoundEnabled = it; notificationHelper.isNotificationSoundEnabled = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsVibrateTitle,
                        subtitle = if (isEs) "Vibra cuando llega un juego gratis nuevo" else "Vibrate on new free game arrival",
                        checked = isVibrateEnabled,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        onCheckedChange = { isVibrateEnabled = it; notificationHelper.isNotificationVibrateEnabled = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsRichArtTitle,
                        subtitle = if (isEs) "Muestra portadas en alta calidad en la notificación" else "Display BigPicture artwork inside notifications",
                        checked = isRichArtEnabled,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        onCheckedChange = { isRichArtEnabled = it; notificationHelper.isNotificationRichArtwork = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsActionBtnsTitle,
                        subtitle = if (isEs) "Incluye botones de 'Reclamar', '★ Guardar' y 'Compartir'" else "Include 'Claim Free', '★ Bookmark', and 'Share' buttons in alerts",
                        checked = isActionsEnabled,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        onCheckedChange = { isActionsEnabled = it; notificationHelper.isNotificationActionsEnabled = it }
                    )
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    Column {
                        Text(
                            text = if (isEs) "Valor Mínimo Original del Juego:" else "Minimum Original Game Value:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) TextPrimary else TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                if (isEs) {
                                    listOf(0f to "Cualquiera", 5f to "Valor $5+", 10f to "Valor $10+", 20f to "Valor $20+", 30f to "Valor $30+ (AAA)")
                                } else {
                                    listOf(0f to "Any Value", 5f to "Worth $5+", 10f to "Worth $10+", 20f to "Worth $20+", 30f to "Worth $30+ (AAA)")
                                }
                            ) { (threshold, label) ->
                                FilterChip(
                                    selected = minWorthThreshold == threshold,
                                    enabled = isNotificationsEnabled,
                                    onClick = {
                                        minWorthThreshold = threshold
                                        notificationHelper.minWorthAlertThreshold = threshold
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonGreen,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF141E2F),
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = if (isEs) "Horas Silenciosas (23:00 - 8:00)" else "Quiet Hours (11 PM - 8 AM)",
                        subtitle = if (isEs) "Silencia sonidos y alertas mientras duermes" else "Silence alert sounds & notifications while you sleep",
                        checked = isQuietHours,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        leadingContent = {
                            Icon(Icons.Default.Nightlight, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        },
                        onCheckedChange = { isQuietHours = it; notificationHelper.isQuietHoursEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(s.settings.alertsBgSyncSection, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GamingDarkSurfaceVariant)
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEs) "Frecuencia Automática de Revisión:" else "Automatic Check Frequency:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) TextPrimary else TextMuted
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            if (isEs) {
                                listOf(2 to "2 Horas", 4 to "4 Horas", 8 to "8 Horas", 24 to "Diario", 0 to "Manual")
                            } else {
                                listOf(2 to "2 Hours", 4 to "4 Hours", 8 to "8 Hours", 24 to "Daily", 0 to "Manual")
                            }
                        ) { (hours, label) ->
                            FilterChip(
                                selected = syncIntervalHours == hours,
                                enabled = isNotificationsEnabled,
                                onClick = {
                                    syncIntervalHours = hours
                                    appPrefs.backgroundSyncIntervalHours = hours
                                    NotificationScheduler.schedulePeriodicSync(context)
                                    Toast.makeText(
                                        context,
                                        if (isEs) "Sincronización ajustada a $label" else "Background sync set to $label",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF141E2F),
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = GamingCardBorder, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsWifiOnlyTitle,
                        subtitle = if (isEs) "Comprobar en segundo plano solo con Wi-Fi" else "Only check in background when connected to Wi-Fi",
                        checked = wifiOnly,
                        enabled = isNotificationsEnabled,
                        titleColor = TextPrimary,
                        subtitleColor = TextSecondary,
                        mutedColor = TextMuted,
                        thumbColor = GamingDarkBackground,
                        trackColor = NeonCyan,
                        leadingContent = {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        },
                        onCheckedChange = { wifiOnly = it; appPrefs.backgroundSyncWifiOnly = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (onOpenInbox != null) {
                Button(
                    onClick = { onDismiss(); onOpenInbox() },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_alert_inbox_from_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        if (isEs) "  Abrir Bandeja e Historial" else "  Open Alert Inbox & History",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        if (hasOrRequestNotificationPermission(context, permissionLauncher)) {
                            notificationHelper.sendTestNotification(isRichPreview = true)
                            Toast.makeText(
                                context,
                                if (isEs) "¡Alerta rica enviada! Revisa la barra." else "Rich BigPicture notification sent! Check drawer.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("send_test_notification_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(if (isEs) "  Alerta Juego" else "  Rich Game Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        if (hasOrRequestNotificationPermission(context, permissionLauncher)) {
                            notificationHelper.sendTestEndingSoonNotification()
                            Toast.makeText(
                                context,
                                if (isEs) "¡Alerta urgente enviada! Revisa la barra." else "Ending Soon notification sent! Check drawer.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("send_test_ending_soon_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(if (isEs) "  Alerta Urgente" else "  Ending Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ChannelStatusBadge(name: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF101726))
            .border(0.8.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}
