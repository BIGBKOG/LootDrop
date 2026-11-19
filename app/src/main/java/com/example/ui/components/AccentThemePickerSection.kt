package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDescription
import com.example.i18n.localizedLabel
import com.example.i18n.localizedShortName
import com.example.i18n.localizedTitle
import com.example.preferences.AccentCategory
import com.example.preferences.AccentTheme
import com.example.ui.GiveawaysViewModel
import com.example.ui.theme.TextMuted

@Composable
internal fun AccentThemePickerSection(
    accentTheme: AccentTheme,
    isCurrentDark: Boolean,
    viewModel: GiveawaysViewModel,
    context: Context
) {
    val s = LocalAppStrings.current
    val lang = s.language
    var selectedAccentCategory by remember { mutableStateOf(AccentCategory.ALL) }
    var accentViewMode by remember { mutableStateOf("grid") }

    SectionHeader(title = s.settings.appearanceAccentSection, icon = Icons.Default.Palette)
    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val activePrimary = accentTheme.primary(isCurrentDark)
        val activeSecondary = accentTheme.secondary(isCurrentDark)

        // Hero Spotlight Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(activePrimary.copy(alpha = 0.18f), activeSecondary.copy(alpha = 0.08f), MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(activePrimary.copy(alpha = 0.7f), activeSecondary.copy(alpha = 0.35f))),
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(activePrimary, activeSecondary)))
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(accentTheme.localizedTitle(s), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(activePrimary.copy(alpha = 0.2f))
                                    .border(0.5.dp, activePrimary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(s.settings.languageActiveBadge, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = activePrimary)
                            }
                        }
                        Text(accentTheme.localizedDescription(s), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(activePrimary)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "100% GRATIS"
                                AppLanguage.FRENCH -> "100% GRATUIT"
                                AppLanguage.ENGLISH -> "100% OFF"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0B0F19)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(activeSecondary.copy(alpha = 0.2f))
                            .border(0.5.dp, activeSecondary, RoundedCornerShape(5.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Reclamar Gratis"
                                AppLanguage.FRENCH -> "Réclamer"
                                AppLanguage.ENGLISH -> "Free Claim"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeSecondary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        when (lang) {
                            AppLanguage.SPANISH -> "Vista Previa"
                            AppLanguage.FRENCH -> "Aperçu"
                            AppLanguage.ENGLISH -> "Live Preview"
                        },
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }

        // Category Filter & View Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AccentCategory.values()) { category ->
                    val isSelected = category == selectedAccentCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (isSelected) activePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { selectedAccentCategory = category }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = category.localizedLabel(s),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf("grid" to Icons.Default.GridView, "carousel" to Icons.Default.ViewCarousel).forEach { (mode, icon) ->
                    val active = accentViewMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) activePrimary else Color.Transparent)
                            .clickable { accentViewMode = mode }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (mode == "grid") {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Vista en cuadrícula"
                                    AppLanguage.FRENCH -> "Vue grille"
                                    AppLanguage.ENGLISH -> "Grid View"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.SPANISH -> "Vista en carrusel"
                                    AppLanguage.FRENCH -> "Vue carrousel"
                                    AppLanguage.ENGLISH -> "Carousel View"
                                }
                            },
                            tint = if (active) Color(0xFF0B0F19) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        val displayedThemes = remember(selectedAccentCategory) {
            if (selectedAccentCategory == AccentCategory.ALL) AccentTheme.values().toList()
            else AccentTheme.values().filter { it.category == selectedAccentCategory }
        }

        val onSelectTheme: (AccentTheme, Boolean) -> Unit = { theme, unlocked ->
            if (unlocked) {
                viewModel.setAccentTheme(theme)
            } else {
                val msg = when (lang) {
                    AppLanguage.SPANISH -> "🔒 ¡Completa un hito en el Reto Backlog 3×3 para desbloquear ${theme.localizedTitle(s)}!"
                    AppLanguage.FRENCH -> "🔒 Terminez un palier du Défi Backlog 3×3 pour débloquer ${theme.localizedTitle(s)} !"
                    AppLanguage.ENGLISH -> "🔒 Complete a 3×3 Backlog Challenge milestone to unlock ${theme.localizedTitle(s)}!"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        if (accentViewMode == "grid") {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayedThemes.chunked(4).forEach { rowThemes ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        rowThemes.forEach { theme ->
                            val isSelected = theme == accentTheme
                            val isUnlocked = viewModel.preferences.isAccentThemeUnlocked(theme)
                            val primary = theme.primary(isCurrentDark)
                            val secondary = theme.secondary(isCurrentDark)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectTheme(theme, isUnlocked) }
                                    .padding(vertical = 4.dp)
                                    .testTag("accent_theme_${theme.name.lowercase()}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            if (isSelected) 2.5.dp else 1.dp,
                                            if (isSelected) primary else if (!isUnlocked) Color(0xFF64748B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    if (isUnlocked) listOf(primary, secondary) else listOf(primary.copy(alpha = 0.4f), secondary.copy(alpha = 0.4f))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = when (lang) {
                                                        AppLanguage.SPANISH -> "Seleccionado"
                                                        AppLanguage.FRENCH -> "Sélectionné"
                                                        AppLanguage.ENGLISH -> "Selected"
                                                    },
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        } else if (!isUnlocked) {
                                            Icon(
                                                Icons.Default.Lock,
                                                contentDescription = when (lang) {
                                                    AppLanguage.SPANISH -> "Bloqueado"
                                                    AppLanguage.FRENCH -> "Verrouillé"
                                                    AppLanguage.ENGLISH -> "Locked"
                                                },
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val short = theme.localizedShortName(s)
                                Text(
                                    text = if (!isUnlocked) "🔒 $short" else short,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primary else if (!isUnlocked) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        repeat(4 - rowThemes.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(displayedThemes) { theme ->
                    val isSelected = theme == accentTheme
                    val isUnlocked = viewModel.preferences.isAccentThemeUnlocked(theme)
                    val primary = theme.primary(isCurrentDark)
                    val secondary = theme.secondary(isCurrentDark)
                    Box(
                        modifier = Modifier
                            .width(155.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) primary else if (!isUnlocked) Color(0xFF64748B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectTheme(theme, isUnlocked) }
                            .padding(12.dp)
                            .testTag("accent_theme_${theme.name.lowercase()}")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier.size(26.dp).clip(CircleShape).background(Brush.linearGradient(listOf(primary, secondary))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!isUnlocked) Icon(
                                        Icons.Default.Lock,
                                        contentDescription = when (lang) {
                                            AppLanguage.SPANISH -> "Bloqueado"
                                            AppLanguage.FRENCH -> "Verrouillé"
                                            AppLanguage.ENGLISH -> "Locked"
                                        },
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                if (isSelected) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(primary).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(s.settings.languageActiveBadge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B0F19))
                                    }
                                } else if (!isUnlocked) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(Color(0xFF334155)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(
                                            when (lang) {
                                                AppLanguage.SPANISH -> "BLOQUEADO"
                                                AppLanguage.FRENCH -> "VERROUILLÉ"
                                                AppLanguage.ENGLISH -> "LOCKED"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = theme.localizedTitle(s),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) primary else if (!isUnlocked) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = if (!isUnlocked) {
                                    when (lang) {
                                        AppLanguage.SPANISH -> "Desbloquea en Reto Backlog"
                                        AppLanguage.FRENCH -> "Débloquer via Défi Backlog"
                                        AppLanguage.ENGLISH -> "Unlock via Backlog Challenge"
                                    }
                                } else theme.localizedDescription(s),
                                fontSize = 11.sp,
                                color = if (!isUnlocked) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
