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

private enum class AlertSubMenu(val shortName: String, val spanishName: String, val frenchName: String) {
    ALL("All", "Todo", "Tout"),
    STORES("Stores", "Tiendas", "Boutiques"),
    RULES("Rules", "Reglas", "Règles"),
    AUDIO("Sound & Timing", "Sonido y Horario", "Son & Horaires"),
    PREVIEW("Preview", "Vista", "Aperçu")
}

@Composable
fun AlertsSettingsCategoryContent(
    viewModel: GiveawaysViewModel,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
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
            when (lang) {
                AppLanguage.SPANISH -> "¡Permiso de notificaciones concedido!"
                AppLanguage.FRENCH -> "Autorisation de notification accordée !"
                AppLanguage.ENGLISH -> "Notification permission granted!"
            }
        } else {
            when (lang) {
                AppLanguage.SPANISH -> "Permiso denegado. Actívalo en los ajustes del sistema."
                AppLanguage.FRENCH -> "Autorisation refusée. Activez-la dans les paramètres du système."
                AppLanguage.ENGLISH -> "Permission denied. Enable in device settings."
            }
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val storeSpecs = listOf(
        StoreAlertSpec(
            when (lang) {
                AppLanguage.SPANISH -> "Ofertas y Drops PlayStation"
                AppLanguage.FRENCH -> "Offres et Drops PlayStation"
                AppLanguage.ENGLISH -> "PlayStation Deals & Drops"
            },
            when (lang) {
                AppLanguage.SPANISH -> "Juegos 100% gratis y botín para PS5 y PS4"
                AppLanguage.FRENCH -> "Jeux et butins 100 % gratuits pour PS5 et PS4"
                AppLanguage.ENGLISH -> "PS5 & PS4 100% discount free games & loot"
            },
            PlayStationBlue, isNotifyPlayStation
        ) { viewModel.setNotifyPlayStation(it) },
        StoreAlertSpec(
            when (lang) {
                AppLanguage.SPANISH -> "Ofertas y Drops Xbox"
                AppLanguage.FRENCH -> "Offres et Drops Xbox"
                AppLanguage.ENGLISH -> "Xbox Deals & Drops"
            },
            when (lang) {
                AppLanguage.SPANISH -> "Juegos y claves gratis de Series X|S y Xbox One"
                AppLanguage.FRENCH -> "Jeux et clés gratuits Series X|S et Xbox One"
                AppLanguage.ENGLISH -> "Series X|S & Xbox One free games & keys"
            },
            XboxGreen, isNotifyXbox
        ) { viewModel.setNotifyXbox(it) },
        StoreAlertSpec(
            "Nintendo Switch eShop",
            when (lang) {
                AppLanguage.SPANISH -> "Ofertas 100% gratis y drops de Switch"
                AppLanguage.FRENCH -> "Jeux et drops 100 % gratuits sur Switch"
                AppLanguage.ENGLISH -> "Switch 100% discount freebies & drops"
            },
            NintendoRed, isNotifyNintendo
        ) { viewModel.setNotifyNintendo(it) },
        StoreAlertSpec(
            when (lang) {
                AppLanguage.SPANISH -> "Promociones Gratis de Steam"
                AppLanguage.FRENCH -> "Promotions Gratuites Steam"
                AppLanguage.ENGLISH -> "Steam Free Promotions"
            },
            when (lang) {
                AppLanguage.SPANISH -> "Juegos gratis de Steam y claves 100% rebajadas"
                AppLanguage.FRENCH -> "Jeux gratuits Steam et clés à -100 %"
                AppLanguage.ENGLISH -> "Steam store free games & 100% off keys"
            },
            SteamBlue, isNotifySteam
        ) { viewModel.setNotifySteam(it) },
        StoreAlertSpec(
            "Epic Games Store",
            when (lang) {
                AppLanguage.SPANISH -> "Juegos gratis semanales y títulos misteriosos"
                AppLanguage.FRENCH -> "Jeux gratuits hebdomadaires et titres mystères"
                AppLanguage.ENGLISH -> "Weekly free games & mystery vault titles"
            },
            EpicGamesBlue, isNotifyEpic
        ) { viewModel.setNotifyEpic(it) },
        StoreAlertSpec(
            when (lang) {
                AppLanguage.SPANISH -> "GOG.com Sin DRM"
                AppLanguage.FRENCH -> "GOG.com Sans DRM"
                AppLanguage.ENGLISH -> "GOG.com DRM-Free"
            },
            when (lang) {
                AppLanguage.SPANISH -> "Juegos clásicos e indies sin DRM gratis"
                AppLanguage.FRENCH -> "Jeux classiques et indés sans DRM gratuits"
                AppLanguage.ENGLISH -> "DRM-free classic and indie giveaways"
            },
            GogPurple, isNotifyGog
        ) { viewModel.setNotifyGog(it) },
        StoreAlertSpec(
            "Google Play / Android",
            when (lang) {
                AppLanguage.SPANISH -> "Juegos móviles Android, apps y packs dentro del juego"
                AppLanguage.FRENCH -> "Jeux mobiles Android, apps et packs en jeu"
                AppLanguage.ENGLISH -> "Android mobile games, apps & in-game packs"
            },
            Color(0xFF3DDC84), isNotifyAndroid
        ) { viewModel.setNotifyAndroid(it) },
        StoreAlertSpec(
            "Apple App Store / iOS",
            when (lang) {
                AppLanguage.SPANISH -> "Juegos gratis y ofertas para iPhone y iPad"
                AppLanguage.FRENCH -> "Jeux gratuits et offres pour iPhone et iPad"
                AppLanguage.ENGLISH -> "iOS (iPhone & iPad) free games & giveaways"
            },
            Color(0xFF007AFF), isNotifyIos
        ) { viewModel.setNotifyIos(it) },
        StoreAlertSpec(
            when (lang) {
                AppLanguage.SPANISH -> "Otros Launchers de PC"
                AppLanguage.FRENCH -> "Autres Launchers PC"
                AppLanguage.ENGLISH -> "Other PC Launchers"
            },
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
                            text = when (lang) {
                                AppLanguage.SPANISH -> subMenu.spanishName
                                AppLanguage.FRENCH -> subMenu.frenchName
                                AppLanguage.ENGLISH -> subMenu.shortName
                            },
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
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Motor Inteligente de Alertas"
                                    AppLanguage.FRENCH -> "Moteur d'Alertes Intelligent"
                                    AppLanguage.ENGLISH -> "Smart Push Alerts Engine"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isNotificationsEnabled) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Vigilando $monitoredStoresCount tiendas • Alertas al instante"
                                    AppLanguage.FRENCH -> "Suivi de $monitoredStoresCount boutiques • Alertes instantanées"
                                    AppLanguage.ENGLISH -> "Actively monitoring $monitoredStoresCount stores • Instant alerts on drop"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Todas las notificaciones en segundo plano están en pausa"
                                    AppLanguage.FRENCH -> "Toutes les notifications en arrière-plan sont en pause"
                                    AppLanguage.ENGLISH -> "All background notifications & alarms are currently paused"
                                }
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
                            if (isSoundEnabled) when (lang) {
                                AppLanguage.SPANISH -> "Sonido SÍ"
                                AppLanguage.FRENCH -> "Son OUI"
                                AppLanguage.ENGLISH -> "Sound ON"
                            } else when (lang) {
                                AppLanguage.SPANISH -> "Silencio"
                                AppLanguage.FRENCH -> "Muet"
                                AppLanguage.ENGLISH -> "Silent"
                            },
                            isSoundEnabled, Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            if (isVibrateEnabled) when (lang) {
                                AppLanguage.SPANISH -> "Vibrar SÍ"
                                AppLanguage.FRENCH -> "Vibreur OUI"
                                AppLanguage.ENGLISH -> "Vibrate ON"
                            } else when (lang) {
                                AppLanguage.SPANISH -> "Vibrar NO"
                                AppLanguage.FRENCH -> "Vibreur NON"
                                AppLanguage.ENGLISH -> "Vibrate OFF"
                            },
                            isVibrateEnabled, Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            if (isKeepForeverOnly) when (lang) {
                                AppLanguage.SPANISH -> "Para Siempre"
                                AppLanguage.FRENCH -> "À Vie"
                                AppLanguage.ENGLISH -> "Keep-Only"
                            } else when (lang) {
                                AppLanguage.SPANISH -> "Todas"
                                AppLanguage.FRENCH -> "Toutes"
                                AppLanguage.ENGLISH -> "All Deals"
                            },
                            isKeepForeverOnly, Modifier.weight(1f)
                        )
                        NotificationStatusChip(
                            if (isRichArtEnabled) when (lang) {
                                AppLanguage.SPANISH -> "Portada"
                                AppLanguage.FRENCH -> "Jaquette"
                                AppLanguage.ENGLISH -> "Poster Art"
                            } else when (lang) {
                                AppLanguage.SPANISH -> "Compacta"
                                AppLanguage.FRENCH -> "Compacte"
                                AppLanguage.ENGLISH -> "Compact"
                            },
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
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Activa o silencia cada tienda por separado"
                                    AppLanguage.FRENCH -> "Activez ou coupez chaque boutique séparément"
                                    AppLanguage.ENGLISH -> "Toggle alerts per store independently"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { storeSpecs.forEach { it.onToggle(true) } },
                                enabled = isNotificationsEnabled
                            ) {
                                Text(
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Todas"
                                        AppLanguage.FRENCH -> "Tout activer"
                                        AppLanguage.ENGLISH -> "All On"
                                    },
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = { storeSpecs.forEach { it.onToggle(false) } },
                                enabled = isNotificationsEnabled
                            ) {
                                Text(
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Silenciar"
                                        AppLanguage.FRENCH -> "Tout couper"
                                        AppLanguage.ENGLISH -> "Mute All"
                                    },
                                    fontSize = 11.sp, color = TextMuted
                                )
                            }
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
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Valor Mínimo Original del Juego:"
                                AppLanguage.FRENCH -> "Valeur Minimale d'Origine du Jeu :"
                                AppLanguage.ENGLISH -> "Minimum Original Game Value:"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                when (lang) {
                                    AppLanguage.SPANISH -> listOf(0f to "Cualquiera ($0+)", 5f to "Valor $5+", 10f to "Valor $10+", 20f to "Valor $20+", 30f to "Valor $30+ (AAA)")
                                    AppLanguage.FRENCH -> listOf(0f to "Tous ($0+)", 5f to "Valeur $5+", 10f to "Valeur $10+", 20f to "Valeur $20+", 30f to "Valeur $30+ (AAA)")
                                    AppLanguage.ENGLISH -> listOf(0f to "Any ($0+)", 5f to "Worth $5+", 10f to "Worth $10+", 20f to "Worth $20+", 30f to "Worth $30+ (AAA)")
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
                        text = when (lang) {
                            AppLanguage.SPANISH -> "ENTREGA, AUDIO Y HORARIO SILENCIOSO"
                            AppLanguage.FRENCH -> "LIVRAISON, AUDIO & PLAGE SILENCIEUSE"
                            AppLanguage.ENGLISH -> "DELIVERY, AUDIO & QUIET SCHEDULE"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsSoundTitle,
                        subtitle = when (lang) {
                            AppLanguage.SPANISH -> "Reproduce un sonido al detectar nuevas ofertas gratis"
                            AppLanguage.FRENCH -> "Joue un son lorsqu'une nouvelle offre gratuite est détectée"
                            AppLanguage.ENGLISH -> "Play alert tone when new freebies are discovered"
                        },
                        checked = isSoundEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationSoundEnabled(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsVibrateTitle,
                        subtitle = when (lang) {
                            AppLanguage.SPANISH -> "Vibra al aparecer nuevas ofertas"
                            AppLanguage.FRENCH -> "Fait vibrer l'appareil lors de nouvelles offres"
                            AppLanguage.ENGLISH -> "Vibrate device when new deals drop"
                        },
                        checked = isVibrateEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationVibrateEnabled(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsRichArtTitle,
                        subtitle = when (lang) {
                            AppLanguage.SPANISH -> "Descarga y muestra portadas en alta resolución en las alertas"
                            AppLanguage.FRENCH -> "Télécharge et affiche des jaquettes HD dans les alertes"
                            AppLanguage.ENGLISH -> "Download and display high-res game posters inside alerts"
                        },
                        checked = isRichArtEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationRichArtwork(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsActionBtnsTitle,
                        subtitle = when (lang) {
                            AppLanguage.SPANISH -> "Incluye botones de 'Reclamar', '★ Guardar' y 'Compartir'"
                            AppLanguage.FRENCH -> "Inclut les boutons 'Réclamer', '★ Favoris' et 'Partager'"
                            AppLanguage.ENGLISH -> "Include 'Claim Free', '★ Bookmark', and 'Share' buttons in alerts"
                        },
                        checked = isActionsEnabled,
                        enabled = isNotificationsEnabled,
                        mutedColor = TextMuted,
                        onCheckedChange = { viewModel.setNotificationActionsEnabled(it) }
                    )
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)

                    SimpleSettingSwitchRow(
                        title = s.settings.alertsQuietHoursTitle,
                        subtitle = if (isQuietHours) {
                            when (lang) {
                                AppLanguage.SPANISH -> "Silenciando alertas entre $quietHoursStart:00 y $quietHoursEnd:00"
                                AppLanguage.FRENCH -> "Alertes en sourdine de ${quietHoursStart}h00 à ${quietHoursEnd}h00"
                                AppLanguage.ENGLISH -> "Muting alerts between $quietHoursStart:00 and $quietHoursEnd:00"
                            }
                        } else {
                            when (lang) {
                                AppLanguage.SPANISH -> "Las alertas se envían las 24 horas"
                                AppLanguage.FRENCH -> "Les alertes arrivent 24h/24"
                                AppLanguage.ENGLISH -> "Alerts deliver around the clock"
                            }
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
                            text = when (lang) {
                                AppLanguage.SPANISH -> "Horarios Silenciosos Predefinidos:"
                                AppLanguage.FRENCH -> "Plages Horaires Silencieuses Prédéfinies :"
                                AppLanguage.ENGLISH -> "Quiet Hours Schedule Presets:"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                when (lang) {
                                    AppLanguage.SPANISH -> listOf(
                                        Triple("Noche (23:00 - 8:00)", 23, 8),
                                        Triple("Madrugada (1:00 - 9:00)", 1, 9),
                                        Triple("Trabajo (9:00 - 17:00)", 9, 17)
                                    )
                                    AppLanguage.FRENCH -> listOf(
                                        Triple("Nuit (23h - 8h)", 23, 8),
                                        Triple("Nuit tardive (1h - 9h)", 1, 9),
                                        Triple("Travail (9h - 17h)", 9, 17)
                                    )
                                    AppLanguage.ENGLISH -> listOf(
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
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Frecuencia de Escaneo en Segundo Plano:"
                                    AppLanguage.FRENCH -> "Fréquence d'Analyse en Arrière-plan :"
                                    AppLanguage.ENGLISH -> "Background Scanner Frequency:"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isNotificationsEnabled) MaterialTheme.colorScheme.onSurface else TextMuted
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(
                                when (lang) {
                                    AppLanguage.SPANISH -> listOf(2 to "2h", 4 to "4h", 8 to "8h", 24 to "Diario", 0 to "Manual")
                                    AppLanguage.FRENCH -> listOf(2 to "2h", 4 to "4h", 8 to "8h", 24 to "Quotidien", 0 to "Manuel")
                                    AppLanguage.ENGLISH -> listOf(2 to "2h", 4 to "4h", 8 to "8h", 24 to "Daily", 0 to "Manual")
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
                        subtitle = when (lang) {
                            AppLanguage.SPANISH -> "Ejecutar escaneos en segundo plano solo con Wi-Fi"
                            AppLanguage.FRENCH -> "Exécuter les analyses en arrière-plan uniquement en Wi-Fi"
                            AppLanguage.ENGLISH -> "Only run background scans when connected to Wi-Fi"
                        },
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
                    text = when (lang) {
                        AppLanguage.SPANISH -> "ACCIONES Y PRUEBA DE NOTIFICACIONES"
                        AppLanguage.FRENCH -> "ACTIONS & TEST DE NOTIFICATIONS"
                        AppLanguage.ENGLISH -> "ACTIONS & NOTIFICATION TESTING"
                    },
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
                        when (lang) {
                            AppLanguage.SPANISH -> "  Abrir Bandeja e Historial de Alertas"
                            AppLanguage.FRENCH -> "  Ouvrir la Boîte de Réception & l'Historique"
                            AppLanguage.ENGLISH -> "  Open Alert Inbox & History"
                        },
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
                                    when (lang) {
                                        AppLanguage.SPANISH -> "¡Alerta de prueba enviada! Revisa la barra."
                                        AppLanguage.FRENCH -> "Alerte test envoyée ! Vérifiez vos notifications."
                                        AppLanguage.ENGLISH -> "Rich game alert test sent! Check shade."
                                    },
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
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "  Probar Juego"
                                AppLanguage.FRENCH -> "  Tester Jeu"
                                AppLanguage.ENGLISH -> "  Test Game Drop"
                            },
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (hasOrRequestNotificationPermission(context, permissionLauncher)) {
                                viewModel.sendEndingSoonTestNotification()
                                Toast.makeText(
                                    context,
                                    when (lang) {
                                        AppLanguage.SPANISH -> "¡Alerta urgente enviada! Revisa la barra."
                                        AppLanguage.FRENCH -> "Alerte urgente envoyée ! Vérifiez vos notifications."
                                        AppLanguage.ENGLISH -> "Expiring Soon alert test sent! Check shade."
                                    },
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
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "  Probar Urgente"
                                AppLanguage.FRENCH -> "  Tester Urgent"
                                AppLanguage.ENGLISH -> "  Test Expiring"
                            },
                            fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
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
                        when (lang) {
                            AppLanguage.SPANISH -> "💎  Vista Previa de Aviso Trofeo Mítico"
                            AppLanguage.FRENCH -> "💎  Aperçu du Toast Trophée Mythique"
                            AppLanguage.ENGLISH -> "💎  Preview Mythic Trophy Popup Toast"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
