package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDescription
import com.example.i18n.localizedLabel
import com.example.i18n.localizedTitle
import com.example.preferences.BackgroundTheme
import com.example.preferences.CardDensity
import com.example.preferences.DpiScaleMode
import com.example.preferences.ThemeMode
import com.example.ui.GiveawaysViewModel
import com.example.ui.MainTab

@Composable
fun AppearanceSettingsCategoryContent(
    viewModel: GiveawaysViewModel
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val accentTheme by viewModel.accentTheme.collectAsState()
    val darkBgTheme by viewModel.darkBackgroundTheme.collectAsState()
    val lightBgTheme by viewModel.lightBackgroundTheme.collectAsState()
    val cardDensity by viewModel.cardDensity.collectAsState()
    val customFontPath by viewModel.customFontPath.collectAsState()
    val customFontName by viewModel.customFontName.collectAsState()
    val dpiScaleMode by viewModel.dpiScaleMode.collectAsState()
    val defaultTab by viewModel.defaultTab.collectAsState()

    val isCurrentDark = themeMode != ThemeMode.LIGHT

    SectionHeader(title = s.settings.appearanceThemeSection, icon = Icons.Default.BrightnessAuto)
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.values().forEach { mode ->
            val isSelected = mode == themeMode
            val icon = when (mode) {
                ThemeMode.DARK -> Icons.Default.DarkMode
                ThemeMode.LIGHT -> Icons.Default.LightMode
                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                    .border(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { viewModel.setThemeMode(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = mode.localizedLabel(s),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    AccentThemePickerSection(
        accentTheme = accentTheme,
        isCurrentDark = isCurrentDark,
        viewModel = viewModel,
        context = context
    )

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(
        title = if (themeMode == ThemeMode.LIGHT) s.settings.appearanceBgLightSection else s.settings.appearanceBgDarkSection,
        icon = Icons.Default.ColorLens
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsOptionCard {
        val availableThemes = BackgroundTheme.values().filter {
            if (themeMode == ThemeMode.LIGHT) !it.isDark else it.isDark
        }
        availableThemes.forEach { bg ->
            val isSelected = if (themeMode == ThemeMode.LIGHT) bg == lightBgTheme else bg == darkBgTheme
            SelectableOptionRow(
                title = bg.localizedTitle(s),
                subtitle = null,
                selected = isSelected,
                testTag = "bg_theme_${bg.name.lowercase()}",
                onClick = {
                    if (themeMode == ThemeMode.LIGHT) viewModel.setLightBackgroundTheme(bg)
                    else viewModel.setDarkBackgroundTheme(bg)
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg.backgroundColor)
                            .border(1.dp, bg.borderColor, RoundedCornerShape(6.dp))
                    )
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    CustomFontSettingsCard(
        customFontPath = customFontPath,
        customFontName = customFontName,
        viewModel = viewModel,
        context = context
    )

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(title = s.settings.appearanceDensitySection, icon = Icons.Default.ViewAgenda)
    Spacer(modifier = Modifier.height(8.dp))

    SettingsOptionCard {
        CardDensity.values().forEach { density ->
            SelectableOptionRow(
                title = density.localizedLabel(s),
                subtitle = density.localizedDescription(s),
                selected = density == cardDensity,
                testTag = "density_${density.name.lowercase()}",
                onClick = { viewModel.setCardDensity(density) }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(
        title = if (isEs) "ESCALA DE UI Y TAMAÑO EN PANTALLA" else "UI SCALE & SCREEN FOOTPRINT",
        icon = Icons.Default.Tune
    )
    Spacer(modifier = Modifier.height(8.dp))

    SettingsOptionCard {
        DpiScaleMode.values().forEach { mode ->
            SelectableOptionRow(
                title = mode.localizedLabel(s),
                subtitle = mode.localizedDescription(s),
                selected = mode == dpiScaleMode,
                testTag = "dpi_scale_${mode.name.lowercase()}",
                onClick = { viewModel.setDpiScaleMode(mode) }
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    SectionHeader(title = s.settings.appearanceStartTabSection, icon = Icons.Default.Home)
    Spacer(modifier = Modifier.height(8.dp))

    SettingsOptionCard {
        listOf(
            Triple(
                MainTab.HOME,
                Icons.Default.Home to (if (isEs) "Inicio (Recomendado)" else "Home (Recommended)"),
                if (isEs) "Panel con tu actividad Steam y los mejores juegos gratis" else "Dashboard with recent Steam activity & top free game picks"
            ),
            Triple(
                MainTab.DISCOVER,
                Icons.Default.Explore to s.common.tabDiscover,
                if (isEs) "Explora todas las ofertas gratis en PC, consolas y móvil" else "Browse all active PC, console & mobile free giveaways"
            ),
            Triple(
                MainTab.CALENDAR,
                Icons.Default.CalendarMonth to s.common.tabCalendar,
                if (isEs) "Radar de vencimiento y calendario de ofertas por terminar" else "Expiration radar & timeline for ending-soon deals"
            ),
            Triple(
                MainTab.CLAIMED,
                Icons.Default.CheckCircle to s.common.tabClaimed,
                if (isEs) "Tu bóveda personal de juegos y logros desbloqueados" else "Your saved game vault and unlocked achievements"
            ),
            Triple(
                MainTab.SETTINGS,
                Icons.Default.Settings to s.common.tabSettings,
                if (isEs) "Preferencias de la app, apariencia y configuración" else "App preferences, appearance, and configuration"
            )
        ).forEach { (tab, iconAndLabel, description) ->
            val (icon, label) = iconAndLabel
            val isSelected = tab == defaultTab
            SelectableOptionRow(
                title = label,
                subtitle = description,
                selected = isSelected,
                testTag = "default_tab_${tab.name.lowercase()}",
                onClick = { viewModel.setDefaultTab(tab) },
                leadingContent = { TabOptionIcon(icon = icon, selected = isSelected) }
            )
        }
    }
}

@Composable
private fun SettingsOptionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun SelectableOptionRow(
    title: String,
    subtitle: String?,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingContent?.invoke()
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = if (LocalAppStrings.current.language == com.example.i18n.AppLanguage.SPANISH) "Seleccionado" else "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TabOptionIcon(icon: ImageVector, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
