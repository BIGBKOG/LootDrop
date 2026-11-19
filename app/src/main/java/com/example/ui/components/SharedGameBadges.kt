package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import coil.size.Precision
import com.example.data.model.GiveawayItem
import com.example.data.model.GameHardwareProfile
import com.example.data.model.LauncherType
import com.example.data.model.PlayStatus
import com.example.data.model.SessionStyle
import com.example.data.model.SteamDeckStatus
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedLabel
import com.example.ui.theme.NeonGreen

internal fun buildGiveawayImageRequest(
    context: Context,
    url: String,
    isLowEndOptimization: Boolean,
    width: Int,
    height: Int
): ImageRequest = ImageRequest.Builder(context)
    .data(url)
    .crossfade(!isLowEndOptimization)
    .bitmapConfig(if (isLowEndOptimization) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
    .allowRgb565(true)
    .precision(if (isLowEndOptimization) Precision.INEXACT else Precision.AUTOMATIC)
    .size(width, height)
    .build()

@Composable
internal fun WishlistBadgePill(text: String? = null) {
    val s = LocalAppStrings.current
    val resolvedText = text ?: "❤️ ${s.home.badgeWishlisted}"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFF2D87).copy(alpha = 0.25f))
            .border(1.dp, Color(0xFFFF2D87), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(resolvedText, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF2D87))
    }
}

@Composable
internal fun SteamDeckStatusPill(
    status: SteamDeckStatus,
    fontSizeSp: Int = 10,
    horizontalPad: Dp = 6.dp,
    verticalPad: Dp = 2.5.dp,
    bold: Boolean = false
) {
    val s = LocalAppStrings.current
    val bgColor = when (status) {
        SteamDeckStatus.VERIFIED -> Color(0x2210B981)
        SteamDeckStatus.PLAYABLE -> Color(0x22F59E0B)
        SteamDeckStatus.UNSUPPORTED -> Color(0x22EF4444)
        SteamDeckStatus.UNKNOWN -> Color(0x2264748B)
    }
    val strokeColor = when (status) {
        SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
        SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
        SteamDeckStatus.UNSUPPORTED -> Color(0xFFEF4444)
        SteamDeckStatus.UNKNOWN -> Color(0xFF64748B)
    }
    val textColor = when (status) {
        SteamDeckStatus.VERIFIED -> Color(0xFF10B981)
        SteamDeckStatus.PLAYABLE -> Color(0xFFF59E0B)
        SteamDeckStatus.UNSUPPORTED -> Color(0xFFEF4444)
        SteamDeckStatus.UNKNOWN -> Color(0xFF94A3B8)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, strokeColor, RoundedCornerShape(6.dp))
            .padding(horizontal = horizontalPad, vertical = verticalPad)
    ) {
        Text(
            text = if (s.language != AppLanguage.ENGLISH) status.localizedLabel(s) else status.pillLabel,
            fontSize = fontSizeSp.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
internal fun HardwareReadinessPillsRow(hardware: GameHardwareProfile) {
    val s = LocalAppStrings.current
    val lang = s.language
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SteamDeckStatusPill(status = hardware.deckStatus)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x2266C0F4))
                .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.5.dp)
        ) {
            Text(hardware.controllerSupport.localizedLabel(s), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF66C0F4))
        }
        if (hardware.sessionStyle == SessionStyle.QUICK_SESSION) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x22F59E0B))
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
            ) {
                Text("⚡ ${SessionStyle.QUICK_SESSION.localizedLabel(s)}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF59E0B))
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x228B5CF6))
                .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.5.dp)
        ) {
            val manualSuffix = if (hardware.isManualOverride) {
                when (lang) {
                    AppLanguage.SPANISH -> " (manual)"
                    AppLanguage.FRENCH -> " (perso)"
                    AppLanguage.ENGLISH -> " (custom)"
                }
            } else ""
            Text(
                text = "💾 ${hardware.formattedSize}$manualSuffix",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA78BFA)
            )
        }
    }
}

@Composable
internal fun ExtraPlatformPills(
    item: GiveawayItem,
    horizontalPad: Dp = 7.dp,
    verticalPad: Dp = 4.dp,
    fontSizeSp: Int = 10,
    withBorder: Boolean = true
) {
    @Composable
    fun pill(label: String, bg: Color, fg: Color, borderAlpha: Float = 0.4f) {
        val base = Modifier.clip(RoundedCornerShape(8.dp)).background(bg)
        val mod = if (withBorder) base.border(1.dp, fg.copy(alpha = borderAlpha), RoundedCornerShape(8.dp)) else base
        Box(modifier = mod.padding(horizontal = horizontalPad, vertical = verticalPad)) {
            Text(label, fontSize = fontSizeSp.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
    if (item.isPlayStation && item.launcherCategory != LauncherType.PLAYSTATION) pill("PlayStation", Color(0xFF0070D1), Color.White)
    if (item.isXbox && item.launcherCategory != LauncherType.XBOX) pill("Xbox", Color(0xFF107C10), Color.White)
    if (item.isAndroid && item.launcherCategory != LauncherType.ANDROID) pill("Android", Color(0xFF3DDC84), Color(0xFF0B0F19), 0.25f)
    if (item.isIos && item.launcherCategory != LauncherType.IOS) pill("iOS", Color(0xFF007AFF), Color.White)
}

@Composable
fun PlayStatusBadge(
    status: PlayStatus,
    onStatusSelected: ((PlayStatus) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    var expanded by remember { mutableStateOf(false) }
    val statusColor = Color(status.badgeColorHex)
    val statusIcon = when (status) {
        PlayStatus.NOT_STARTED -> "⏳"
        PlayStatus.PLAYING -> "🎮"
        PlayStatus.BEATEN -> "🏆"
        PlayStatus.DROPPED -> "⏸️"
    }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .then(if (onStatusSelected != null) Modifier.clickable { expanded = true } else Modifier)
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(statusIcon, fontSize = 10.sp)
            Text(status.localizedLabel(s), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
            if (onStatusSelected != null) Text("▾", fontSize = 10.sp, color = statusColor.copy(alpha = 0.8f))
        }

        if (onStatusSelected != null) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                PlayStatus.entries.forEach { option ->
                    val isCurrent = option == status
                    val optColor = Color(option.badgeColorHex)
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = when (option) {
                                        PlayStatus.NOT_STARTED -> "⏳"
                                        PlayStatus.PLAYING -> "🎮"
                                        PlayStatus.BEATEN -> "🏆"
                                        PlayStatus.DROPPED -> "⏸️"
                                    },
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = option.localizedLabel(s),
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) optColor else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = { expanded = false; onStatusSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun SteamTelemetryBadge(item: GiveawayItem, modifier: Modifier = Modifier) {
    if (!item.hasSteamTelemetry) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF66C0F4).copy(alpha = 0.12f))
            .border(1.dp, Color(0xFF66C0F4).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text("🎮 Steam", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF66C0F4))
        Text("•", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("⏱ ${item.steamPlaytimeHours}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        if (item.steamAchievementsTotal > 0) {
            Text("•", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "🏆 ${item.steamAchievementsUnlocked}/${item.steamAchievementsTotal} (${item.steamAchievementsPercentage}%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.steamAchievementsPercentage == 100) NeonGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
