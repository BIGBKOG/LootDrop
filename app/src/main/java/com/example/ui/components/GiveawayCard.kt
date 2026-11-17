package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.GiveawayItem
import com.example.data.model.PlayStatus
import com.example.preferences.CardDensity
import com.example.ui.density.LocalScreenDensityInfo
import com.example.ui.theme.NeonGreen
import kotlin.math.abs

@Composable
fun GiveawayCard(
    item: GiveawayItem,
    cardDensity: CardDensity = CardDensity.RICH_CARDS,
    showOriginalPrice: Boolean = true,
    showExpiration: Boolean = true,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    onToggleBookmark: () -> Unit = {},
    onToggleClaimed: () -> Unit,
    onPlayStatusChange: ((PlayStatus) -> Unit)? = null,
    isAnimationsEnabled: Boolean = true,
    isLowEndOptimization: Boolean = false,
    beamAngle: Float = 0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val screenDensityInfo = LocalScreenDensityInfo.current
    val launcherColor = Color(item.launcherCategory.badgeColorHex)
    val cardAngleOffset = remember(item.id) {
        val h = item.id.hashCode()
        ((if (h == Int.MIN_VALUE) 0 else abs(h)) % 360).toFloat()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animateUi = isAnimationsEnabled && !isLowEndOptimization

    val cardScale = if (animateUi) {
        val anim by animateFloatAsState(if (isPressed) 0.985f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "card_press_scale")
        anim
    } else if (isPressed) 0.985f else 1f

    val (pulseGlow, pulseScale) = rememberCardPulse(enabled = item.isNew && animateUi)

    val claimedScale = if (animateUi) {
        val anim by animateFloatAsState(if (item.isClaimed) 1.15f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow), label = "claimed_scale")
        anim
    } else if (item.isClaimed) 1.15f else 1f

    val claimInteractionSource = remember { MutableInteractionSource() }
    val isClaimPressed by claimInteractionSource.collectIsPressedAsState()
    val claimButtonScale = if (animateUi) {
        val anim by animateFloatAsState(if (isClaimPressed) 0.94f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "claim_button_press_scale")
        anim
    } else if (isClaimPressed) 0.94f else 1f

    val themePrimary = MaterialTheme.colorScheme.primary
    val calmRestingBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    val isHighlightedCard = item.isWishlisted || item.isClaimed || item.isNew || item.isBookmarked || isPressed

    val expressiveBorderColor = when {
        item.isWishlisted -> Color(0xFFFF2D87)
        item.isClaimed -> NeonGreen
        item.isNew -> Color(0xFFFF9100)
        item.isBookmarked || isPressed -> themePrimary
        else -> calmRestingBorder
    }

    val cardBorderColor = if (animateUi) {
        val anim by animateColorAsState(expressiveBorderColor, spring(stiffness = Spring.StiffnessMediumLow), label = "card_border_color")
        anim
    } else expressiveBorderColor

    val cardBorderWidth = if (isHighlightedCard) 1.8.dp else 1.dp
    val showBorderBeam = isHighlightedCard && animateUi

    when (cardDensity) {
        CardDensity.MINIMAL_LIST -> MinimalGiveawayCard(
            item = item, context = context, launcherColor = launcherColor, cardScale = cardScale,
            cardBorderWidth = cardBorderWidth, cardBorderColor = cardBorderColor, beamAngle = beamAngle,
            cardAngleOffset = cardAngleOffset, showBorderBeam = showBorderBeam, claimedScale = claimedScale,
            claimButtonScale = claimButtonScale, interactionSource = interactionSource,
            claimInteractionSource = claimInteractionSource, showOriginalPrice = showOriginalPrice,
            isLowEndOptimization = isLowEndOptimization, onClick = onClick, onClaimClick = onClaimClick,
            onToggleClaimed = onToggleClaimed, onPlayStatusChange = onPlayStatusChange, modifier = modifier
        )
        CardDensity.COMPACT_CARDS -> CompactGiveawayCard(
            item = item, context = context, screenDensityInfo = screenDensityInfo, launcherColor = launcherColor,
            cardScale = cardScale, cardBorderWidth = cardBorderWidth, cardBorderColor = cardBorderColor,
            beamAngle = beamAngle, cardAngleOffset = cardAngleOffset, showBorderBeam = showBorderBeam,
            claimButtonScale = claimButtonScale, interactionSource = interactionSource,
            claimInteractionSource = claimInteractionSource, showOriginalPrice = showOriginalPrice,
            isLowEndOptimization = isLowEndOptimization, onClick = onClick, onClaimClick = onClaimClick,
            onPlayStatusChange = onPlayStatusChange, modifier = modifier
        )
        CardDensity.RICH_CARDS -> RichGiveawayCard(
            item = item, context = context, screenDensityInfo = screenDensityInfo, launcherColor = launcherColor,
            themePrimary = themePrimary, cardScale = cardScale, cardBorderWidth = cardBorderWidth,
            cardBorderColor = cardBorderColor, beamAngle = beamAngle, cardAngleOffset = cardAngleOffset,
            showBorderBeam = showBorderBeam, pulseGlow = pulseGlow, pulseScale = pulseScale,
            claimedScale = claimedScale, claimButtonScale = claimButtonScale, interactionSource = interactionSource,
            claimInteractionSource = claimInteractionSource, showOriginalPrice = showOriginalPrice,
            showExpiration = showExpiration, isAnimationsEnabled = isAnimationsEnabled,
            isLowEndOptimization = isLowEndOptimization, onClick = onClick, onClaimClick = onClaimClick,
            onToggleBookmark = onToggleBookmark, onToggleClaimed = onToggleClaimed,
            onPlayStatusChange = onPlayStatusChange, modifier = modifier
        )
    }
}

@Composable
private fun rememberCardPulse(enabled: Boolean): Pair<Float, Float> {
    if (!enabled) return 1.0f to 1.0f
    val infiniteTransition = rememberInfiniteTransition(label = "card_pulse")
    val glow by infiniteTransition.animateFloat(0.65f, 1.0f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse_glow")
    val scale by infiniteTransition.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse_scale")
    return glow to scale
}
