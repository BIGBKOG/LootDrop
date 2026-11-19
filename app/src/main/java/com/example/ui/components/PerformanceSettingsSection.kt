package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDescription
import com.example.i18n.localizedLabel
import com.example.preferences.TargetFrameRate
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.util.DevicePerformanceHelper

@Composable
fun PreRenderingPerformanceSection(
    targetFrameRate: TargetFrameRate,
    detectedRefreshRate: Int,
    isLowEndOptimization: Boolean,
    onToggleLowEndOptimization: (Boolean) -> Unit,
    onSelectFrameRate: (TargetFrameRate) -> Unit
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val context = LocalContext.current
    val perfProfile = remember(context) { DevicePerformanceHelper.getProfile(context) }

    SectionHeader(title = s.settings.perfSectionTitle, icon = Icons.Default.Speed)

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Column {
                        Text(
                            when (lang) {
                                AppLanguage.SPANISH -> "Perfil de SoC y Memoria Detectado"
                                AppLanguage.FRENCH -> "Profil SoC & Mémoire Détecté"
                                AppLanguage.ENGLISH -> "Detected SoC & Memory Profile"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val summaryText = when (lang) {
                            AppLanguage.SPANISH -> buildString {
                                append("${perfProfile.processorCores} Núcleos • ${perfProfile.totalRamMb}MB RAM • ${perfProfile.memoryClassMb}MB Heap")
                                if (perfProfile.isLowRamDevice) append(" • Modo Poca RAM")
                                if (perfProfile.isPowerSaveMode) append(" • Ahorro de Batería")
                                if (perfProfile.isLowEndDevice) append(" • Optimizado Gama Baja") else append(" • Alto Rendimiento")
                            }
                            AppLanguage.FRENCH -> buildString {
                                append("${perfProfile.processorCores} Cœurs • ${perfProfile.totalRamMb} Mo RAM • ${perfProfile.memoryClassMb} Mo Heap")
                                if (perfProfile.isLowRamDevice) append(" • Faible RAM")
                                if (perfProfile.isPowerSaveMode) append(" • Économie d'Énergie")
                                if (perfProfile.isLowEndDevice) append(" • Optimisé Entrée de Gamme") else append(" • Haute Performance")
                            }
                            AppLanguage.ENGLISH -> perfProfile.telemetrySummary
                        }
                        Text(summaryText, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "${detectedRefreshRate}Hz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isLowEndOptimization) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                .border(
                    1.dp,
                    if (isLowEndOptimization) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Text(
                    when (lang) {
                        AppLanguage.SPANISH -> "Aceleración para Gama Baja"
                        AppLanguage.FRENCH -> "Accélération Appareils Modestes"
                        AppLanguage.ENGLISH -> "Low-End Device Acceleration"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (lang) {
                        AppLanguage.SPANISH -> "Reduce a la mitad la RAM de imágenes con texturas RGB_565 de 16 bits, pausa animaciones infinitas inactivas, omite el renderizado de pestañas ocultas y optimiza GPU."
                        AppLanguage.FRENCH -> "Divise par deux la RAM image via textures RGB_565 16 bits, met en pause les animations inactives, ignore les onglets masqués et optimise le GPU."
                        AppLanguage.ENGLISH -> "Halves image RAM via 16-bit RGB_565 textures, pauses idle infinite animation clocks, skips off-screen tab rendering, and optimizes GPU passes."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isLowEndOptimization) NeonGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLowEndOptimization) {
                                when (lang) {
                                    AppLanguage.SPANISH -> "⚡ ACELERACIÓN ACTIVA"
                                    AppLanguage.FRENCH -> "⚡ ACCÉLÉRATION ACTIVE"
                                    AppLanguage.ENGLISH -> "⚡ ACCELERATION ACTIVE"
                                }
                            } else {
                                when (lang) {
                                    AppLanguage.SPANISH -> "MODO ESTÁNDAR"
                                    AppLanguage.FRENCH -> "MODE STANDARD"
                                    AppLanguage.ENGLISH -> "STANDARD MODE"
                                }
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowEndOptimization) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "${perfProfile.processorCores} Núcleos • ${perfProfile.totalRamMb}MB RAM • ${perfProfile.memoryClassMb}MB Heap"
                            AppLanguage.FRENCH -> "${perfProfile.processorCores} Cœurs • ${perfProfile.totalRamMb} Mo RAM • ${perfProfile.memoryClassMb} Mo Heap"
                            AppLanguage.ENGLISH -> "${perfProfile.processorCores} Cores • ${perfProfile.totalRamMb}MB RAM • ${perfProfile.memoryClassMb}MB Heap"
                        },
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
            Switch(
                checked = isLowEndOptimization,
                onCheckedChange = onToggleLowEndOptimization,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("switch_low_end_optimization")
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = when (lang) {
                AppLanguage.SPANISH -> "Elige la tasa de refresco objetivo para la UI (60 FPS, 90 FPS, 120 FPS):"
                AppLanguage.FRENCH -> "Choisissez la fréquence d'images cible de l'interface (60 FPS, 90 FPS, 120 FPS) :"
                AppLanguage.ENGLISH -> "Select UI pre-rendering frame rate target (60 FPS, 90 FPS, 120 FPS):"
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TargetFrameRate.values().forEach { rate ->
            val isSelected = rate == targetFrameRate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectFrameRate(rate) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("fps_option_${rate.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(0.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.width(64.dp)
                    ) {
                        Text(
                            text = if (rate.fps > 0) "${rate.fps} FPS" else "AUTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    Column {
                        Text(
                            text = rate.localizedLabel(s),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = rate.localizedDescription(s), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = when (s.language) {
                            AppLanguage.SPANISH -> "Seleccionado"
                            AppLanguage.FRENCH -> "Sélectionné"
                            AppLanguage.ENGLISH -> "Selected"
                        },
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
