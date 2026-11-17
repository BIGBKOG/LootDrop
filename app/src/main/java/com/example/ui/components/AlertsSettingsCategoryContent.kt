package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.XboxGreen

private enum class AlertSubMenu(val shortName: String, val spanishName: String) {
    ALL("All", "Todo"),
    STORES("Stores", "Tiendas"),
    RULES("Rules", "Reglas"),
    AUDIO("Sound & Timing", "Sonido y Horario"),
    PREVIEW("Preview", "Vista")
}

@Composable
fun AlertsSettingsCategoryContent(
    viewModel: GiveawaysViewModel,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val context = LocalContext.current
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isNotifyPlayStation by viewModel.isNotifyPlayStation.collectAsState()
    val isNotifyXbox by viewModel.isNotifyXbox.collectAsState()
    val isNotifyNintendo by viewModel.isNotifyNintendo.collectAsState()
    val isNotifySteam by viewModel.isNotifySteam.collectAsState()
    val isNotifyEpic by viewModel.isNotifyEpic.collectAsState()
    val isNotifyGog by viewModel.isNotifyGog.collectAsState()
    val isNotifyAndroid by viewModel.isNotifyAndroid.collectAsState()
    val isNotifyIos by viewModel.isNotifyIos.collectAsState()
    val isNotifyOthers by viewModel.isNotifyOthers.collectAsState()
    val isGamesOnly by viewModel.isGamesOnly.collectAsState()
    val isKeepForeverOnly by viewModel.isKeepForeverOnly.collectAsState()
    val isNotifyEndingSoon by viewModel.isNotifyEndingSoon.collectAsState()
    val minWorthThreshold by viewModel.minWorthAlertThreshold.collectAsState()
    val isQuietHours by viewModel.isQuietHoursEnabled.collectAsState()
    val quietHoursStart by viewModel.quietHoursStartHour.collectAsState()
    val quietHoursEnd by viewModel.quietHoursEndHour.collectAsState()
    val isSoundEnabled by viewModel.isNotificationSoundEnabled.collectAsState()
    val isVibrateEnabled by viewModel.isNotificationVibrateEnabled.collectAsState()
    val isRichArtEnabled by viewModel.isNotificationRichArtwork.collectAsState()
    val isActionsEnabled by viewModel.isNotificationActionsEnabled.collectAsState()
    val syncIntervalHours by viewModel.backgroundSyncIntervalHours.collectAsState()
    val wifiOnly by viewModel.backgroundSyncWifiOnly.collectAsState()

    var activeSubMenu by remember { mutableStateOf(AlertSubMenu.ALL) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val msg = if (isGranted) {
            if (isEs) "¡Permiso de notificaciones concedido!" else "Notification permission granted!"
        } else {
            if (isEs) "Permiso denegado. Actívalo en los ajustes del sistema." else "Permission denied. Enable in device settings."
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val storeSpecs = listOf(
        StoreAlertSpec(
            if (isEs) "Ofertas y Drops PlayStation" else "PlayStation Deals & Drops",
            if (isEs) "Juegos 100% gratis y botín para PS5 y PS4" else "PS5 & PS4 100% discount free games & loot",
            PlayStationBlue, isNotifyPlayStation
        ) { viewModel.setNotifyPlayStation(it) },
        StoreAlertSpec(
            if (isEs) "Ofertas y Drops Xbox" else "Xbox Deals & Drops",
            if (isEs) "Juegos y claves gratis de Series X|S y Xbox One" else "Series X|S & Xbox One free games & keys",
            XboxGreen, isNotifyXbox
        ) { viewModel.setNotifyXbox(it) },
        StoreAlertSpec(
            "Nintendo Switch eShop",
            if (isEs) "Ofertas 100% gratis y drops de Switch" else "Switch 100% discount freebies & drops",
            NintendoRed, isNotifyNintendo
        ) { viewModel.setNotifyNintendo(it) },
        StoreAlertSpec(
            if (isEs) "Promociones Gratis de Steam" else "Steam Free Promotions",
            if (isEs) "Juegos gratis de Steam y claves 100% rebajadas" else "Steam store free games & 100% off keys",
            SteamBlue, isNotifySteam
        ) { viewModel.setNotifySteam(it) },
        StoreAlertSpec(
            "Epic Games Store",
            if (isEs) "Juegos gratis semanales y títulos misteriosos" else "Weekly free games & mystery vault titles",
            EpicGamesBlue, isNotifyEpic
        ) { viewModel.setNotifyEpic(it) },
        StoreAlertSpec(
            "GOG.com Sin DRM",
            if (isEs) "Juegos clásicos e indies sin DRM gratis" else "DRM-free classic and indie giveaways",
            GogPurple, isNotifyGog
        ) { viewModel.setNotifyGog(it) },
        StoreAlertSpec(
            "Google Play / Android",
            if (isEs) "Juegos móviles Android, apps y packs dentro del juego" else "Android mobile games, apps & in-game packs",
            Color(0xFF3DDC84), isNotifyAndroid
        ) { viewModel.setNotifyAndroid(it) },
        StoreAlertSpec(
            "Apple App Store / iOS",
            if (isEs) "Juegos gratis y ofertas para iPhone y iPad" else "iOS (iPhone & iPad) free games & giveaways",
            Color(0xFF007AFF), isNotifyIos
        ) { viewModel.setNotifyIos(it) },
        StoreAlertSpec(
            if (isEs) "Otros Launchers de PC" else "Other PC Launchers",
            "Ubisoft, itch.io, EA App, Prime Gaming",
            NeonGreen, isNotifyOthers
        ) { viewModel.setNotifyOthers(it) }
    )
    val monitoredStoresCount = storeSpecs.count { it.checked }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AlertSubMenu.values()) { subMenu ->
                val isSelected = subMenu == activeSubMenu
                Surface(
                    onClick = { activeSubMenu = subMenu },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (isEs) subMenu.spanishName else subMenu.shortName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Master Switch Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isNotificationsEnabled) NeonGreen else Color.Gray)
                            )
                            Text(
                                text = if (isEs) "Motor Inteligente de Alertas" else "Smart Push Alerts Engine",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isNotificationsEnabled) {
                                if (isEs) "Vigilando $monitoredStoresCount tiendas • Alertas al instante"
                                else "Actively monitoring $monitoredStoresCount stores • Instant alerts on drop"
                            } else {
                                if (isEs) "Todas las notificaciones en segundo plano están en pausa"
                                else "All background notifications & alarms are currently paused"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { checked ->
                            viewModel.setNotificationsEnabled(checked)
                            if (checked) hasOrRequestNotificationPermission(context, permissionLauncher)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.background,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("settings_master_notifications_switch")
                    )
                }

                if (isNotificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotificationStatusChip(
                            if (isSoundEnabled) (if (isEs) "Sonido SÍ" else "Sound ON") else (if (isEs) "Silencio" else "Silent"),
                            isSoundEnabled, Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            if (isVibrateEnabled) (if (isEs) "Vibrar SÍ" else "Vibrate ON") else (if (isEs) "Vibrar NO" else "Vibrate OFF"),
                            isVibrateEnabled, Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            if (isKeepForeverOnly) (if (isEs) "Para Siempre" else "Keep-Only") else (if (isEs) "Todas" else "All Deals"),
                            isKeepForeverOnly, Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            if (isRichArtEnabled) (if (isEs) "Portada" else "Poster Art") else (if (isEs) "Compacta" else "Compact"),
                            isRichArtEnabled, Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.PREVIEW) {
            LiveNotificationPreviewCard(isNotificationsEnabled, isRichArtEnabled, isActionsEnabled)
        }

        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.STORES) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = s.settings.alertsPlatformsSection,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isEs) "Activa o silencia cada tienda por separado" else "Toggle alerts per store independently",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { storeSpecs.forEach { it.onToggle(true) } },
                                enabled = isNotificationsEnabled
                            ) { Text(if (isEs) "Todas" else "All On", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            TextButton(
                                onClick = { storeSpecs.forEach { it.onToggle(false) } },
                                enabled = isNotificationsEnabled
                            ) { Text(if (isEs) "Silenciar" else "Mute All", fontSize = 11.sp, color = TextMuted) }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)

                    storeSpecs.forEach { spec ->
                        SettingToggleRow(
                            title = spec.title,
                            subtitle = spec.subtitle,
                            color = spec.color,
                            checked = spec.checked,
                            enabled = isNotificationsEnabled,
                            onCheckedChange = spec.onToggle,
                            useCircleDot = true,
                            thumbColor = MaterialTheme.colorScheme.background
                        )
                    }
                }
            }
        }

        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.RULES) {
            val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = s.settings.alertsRulesSection,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsKeepForeverTitle,
                        subtitle = s.settings.alertsKeepForeverSub,
                        checked = isKeepForeverOnly,
                        enabled = isNotificationsEnabled,
                        titleFontWeight = FontWeight.Bold,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setKeepForeverOnly(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsGamesOnlyTitle,
                        subtitle = s.settings.alertsGamesOnlySub,
                        checked = isGamesOnly,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setGamesOnly(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsEndingSoonTitle,
                        subtitle = s.settings.alertsEndingSoonSub,
                        checked = isNotifyEndingSoon,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        trackColor = Color(0xFFFF5252),
                        leadingContent = {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(15.dp))
                        },
                        onCheckedChange = { viewModel.setNotifyEndingSoon(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isEs) "Valor Mínimo Original del Juego:" else "Minimum Original Game Value:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                if (isEs) {
                                    listOf(0f to "Cualquiera ($0+)", 5f to "Valor $5+", 10f to "Valor $10+", 20f to "Valor $20+", 30f to "Valor $30+ (AAA)")
                                } else {
                                    listOf(0f to "Any ($0+)", 5f to "Worth $5+", 10f to "Worth $10+", 20f to "Worth $20+", 30f to "Worth $30+ (AAA)")
                                }
                            ) { (threshold, label) ->
                                FilterChip(
                                    selected = minWorthThreshold == threshold,
                                    enabled = isNotificationsEnabled,
                                    onClick = { viewModel.setMinWorthAlertThreshold(threshold) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeSubMenu == AlertSubMenu.ALL || activeSubMenu == AlertSubMenu.AUDIO) {
            val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEs) "ENTREGA, AUDIO Y HORARIO SILENCIOSO" else "DELIVERY, AUDIO & QUIET SCHEDULE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsSoundTitle,
                        subtitle = if (isEs) "Reproduce un sonido al detectar nuevas ofertas gratis" else "Play alert tone when new freebies are discovered",
                        checked = isSoundEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationSoundEnabled(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsVibrateTitle,
                        subtitle = if (isEs) "Vibra al aparecer nuevas ofertas" else "Vibrate device when new deals drop",
                        checked = isVibrateEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationVibrateEnabled(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsRichArtTitle,
                        subtitle = if (isEs) "Descarga y muestra portadas en alta resolución en las alertas" else "Download and display high-res game posters inside alerts",
                        checked = isRichArtEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationRichArtwork(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsActionBtnsTitle,
                        subtitle = if (isEs) "Incluye botones de 'Reclamar', '★ Guardar' y 'Compartir'" else "Include 'Claim Free', '★ Bookmark', and 'Share' buttons in alerts",
                        checked = isActionsEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationActionsEnabled(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsQuietHoursTitle,
                        subtitle = if (isQuietHours) {
                            if (isEs) "Silenciando alertas entre $quietHoursStart:00 y $quietHoursEnd:00"
                            else "Muting alerts between $quietHoursStart:00 and $quietHoursEnd:00"
                        } else {
                            if (isEs) "Las alertas se envían las 24 horas" else "Alerts deliver around the clock"
                        },
                        checked = isQuietHours,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        leadingContent = {
                            Icon(Icons.Default.Nightlight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        },
                        onCheckedChange = { viewModel.setQuietHoursEnabled(it) }
                    )

                    if (isQuietHours) {
                        Text(
                            text = if (isEs) "Horarios Silenciosos Predefinidos:" else "Quiet Hours Schedule Presets:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                if (isEs) {
                                    listOf(
                                        Triple("Noche (23:00 - 8:00)", 23, 8),
                                        Triple("Madrugada (1:00 - 9:00)", 1, 9),
                                        Triple("Trabajo (9:00 - 17:00)", 9, 17)
                                    )
                                } else {
                                    listOf(
                                        Triple("Night (11 PM - 8 AM)", 23, 8),
                                        Triple("Late Night (1 AM - 9 AM)", 1, 9),
                                        Triple("Work (9 AM - 5 PM)", 9, 17)
                                    )
                                }
                            ) { (label, start, end) ->
                                FilterChip(
                                    selected = quietHoursStart == start && quietHoursEnd == end,
                                    onClick = { viewModel.setQuietHoursHours(start, end) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                            Text(
                                text = if (isEs) "Frecuencia de Escaneo en Segundo Plano:" else "Background Scanner Frequency:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                if (isEs) {
                                    listOf(2 to "2h", 4 to "4h", 8 to "8h", 24 to "Diario", 0 to "Manual")
                                } else {
                                    listOf(2 to "2h", 4 to "4h", 8 to "8h", 24 to "Daily", 0 to "Manual")
                                }
                            ) { (hours, label) ->
                                FilterChip(
                                    selected = syncIntervalHours == hours,
                                    enabled = isNotificationsEnabled,
                                    onClick = { viewModel.setBackgroundSyncInterval(hours) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsWifiOnlyTitle,
                        subtitle = if (isEs) "Ejecutar escaneos en segundo plano solo con Wi-Fi" else "Only run background scans when connected to Wi-Fi",
                        checked = wifiOnly,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        leadingContent = {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        },
                        onCheckedChange = { viewModel.setBackgroundSyncWifiOnly(it) }
                    )
                }
            }
        }

        // Action Center (Inbox & Test Push)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isEs) "ACCIONES Y PRUEBA DE NOTIFICACIONES" else "ACTIONS & NOTIFICATION TESTING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { viewModel.setNotificationHistorySheetOpen(true) },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                        if (isEs) "  Abrir Bandeja e Historial de Alertas" else "  Open Alert Inbox & History",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hasOrRequestNotificationPermission(context, permissionLauncher)) {
                                viewModel.sendTestNotification(isRichPreview = isRichArtEnabled)
                                Toast.makeText(
                                    context,
                                    if (isEs) "¡Alerta de prueba enviada! Revisa la barra." else "Rich game alert test sent! Check shade.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("settings_test_alert_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(if (isEs) "  Probar Juego" else "  Test Game Drop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (hasOrRequestNotificationPermission(context, permissionLauncher)) {
                                viewModel.sendEndingSoonTestNotification()
                                Toast.makeText(
                                    context,
                                    if (isEs) "¡Alerta urgente enviada! Revisa la barra." else "Expiring Soon alert test sent! Check shade.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(44.dp).testTag("settings_test_ending_soon_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF5252)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(if (isEs) "  Probar Urgente" else "  Test Expiring", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.previewMythicUnlockToast() },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("settings_test_mythic_toast_button"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE040FB)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE040FB))
                ) {
                    Text(
                        if (isEs) "💎  Vista Previa de Aviso Trofeo Mítico" else "💎  Preview Mythic Trophy Popup Toast",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
