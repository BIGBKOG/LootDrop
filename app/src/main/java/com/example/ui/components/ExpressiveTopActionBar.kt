package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.rememberExpressiveCornerRadius
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.util.LocalLowEndOptimization

/**
 * Compact floating pill bar placed in the top right corner.
 * Styled to match the bottom floating navigation bar (rounded container, dark surface, theme border, soft glow).
 * Houses Search, Filters, and Notification Alerts actions.
 */
@Composable
fun ExpressiveTopActionBar(
    isSearchExpanded: Boolean,
    searchQuery: String,
    onToggleSearch: () -> Unit,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    unreadNotificationCount: Int,
    onNotificationClick: () -> Unit,
    isAnimationsEnabled: Boolean = true,
    isLowEndOptimization: Boolean = false,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val effectiveLowEnd = isLowEndOptimization || LocalLowEndOptimization.current
    val barHeight = 44.dp
    val barShape = RoundedCornerShape(22.dp)

    val themePrimary = MaterialTheme.colorScheme.primary
    val chromeSurface = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)

    val outerBorderColor = if (effectiveLowEnd) {
        themePrimary.copy(alpha = 0.30f)
    } else {
        animateColorAsState(
            targetValue = themePrimary.copy(alpha = 0.30f),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "top_bar_outer_border"
        ).value
    }

    Surface(
        modifier = modifier
            .wrapContentWidth()
            .height(barHeight)
            .then(
                if (effectiveLowEnd) {
                    Modifier
                } else {
                    Modifier.shadow(
                        elevation = 10.dp,
                        shape = barShape,
                        spotColor = themePrimary.copy(alpha = 0.22f),
                        ambientColor = Color(0x33000000)
                    )
                }
            )
            .border(
                width = 1.dp,
                color = outerBorderColor,
                shape = barShape
            )
            .testTag("top_action_bar"),
        shape = barShape,
        color = chromeSurface,
        tonalElevation = if (effectiveLowEnd) 0.dp else 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Search Action Button
            TopCornerActionItem(
                icon = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                contentDescription = s.common.search,
                isActive = isSearchExpanded || searchQuery.isNotBlank(),
                activeColor = themePrimary,
                onClick = onToggleSearch,
                testTag = "toggle_search_button",
                hasDotIndicator = searchQuery.isNotBlank() && !isSearchExpanded,
                isLowEndOptimization = effectiveLowEnd
            )

            // Vertical divider line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(dividerColor)
            )

            // 2. Filters Action Button
            TopCornerActionItem(
                icon = Icons.Default.Tune,
                contentDescription = s.home.filterSortTitle,
                isActive = activeFilterCount > 0,
                activeColor = themePrimary,
                onClick = onFilterClick,
                testTag = "filter_menu_button",
                badgeCount = if (activeFilterCount > 0) activeFilterCount else null,
                isLowEndOptimization = effectiveLowEnd
            )

            // Vertical divider line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(dividerColor)
            )

            // 3. Notifications Action Button
            val (bellRotation, dotPulse) = rememberBellWiggle(
                enabled = isAnimationsEnabled && !effectiveLowEnd,
                hasAlerts = unreadNotificationCount > 0
            )

            TopCornerActionItem(
                icon = if (unreadNotificationCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                contentDescription = s.common.checkNotificationsCd,
                isActive = unreadNotificationCount > 0,
                activeColor = if (unreadNotificationCount > 0) NeonCyan else themePrimary,
                onClick = onNotificationClick,
                testTag = "notification_inbox_button",
                badgeCount = if (unreadNotificationCount > 0) unreadNotificationCount else null,
                isLowEndOptimization = effectiveLowEnd,
                iconRotation = bellRotation,
                badgeColor = NeonGreen
            )
        }
    }
}

@Composable
private fun TopCornerActionItem(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    hasDotIndicator: Boolean = false,
    isLowEndOptimization: Boolean = false,
    iconRotation: Float = 0f,
    badgeColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val targetContentColor = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    val pressScale: Float
    val morphedCorner: androidx.compose.ui.unit.Dp
    val contentColor: Color
    if (isLowEndOptimization) {
        pressScale = 1.0f
        morphedCorner = if (isActive) 14.dp else 18.dp
        contentColor = targetContentColor
    } else {
        val isPressed by interactionSource.collectIsPressedAsState()
        pressScale = animateFloatAsState(
            targetValue = if (isPressed) 0.86f else 1.0f,
            animationSpec = ExpressiveMotion.spatialFast(reducedMotion = false),
            label = "corner_item_press_$testTag"
        ).value
        morphedCorner = rememberExpressiveCornerRadius(
            isSelected = isActive,
            isPressed = isPressed,
            baseRadius = 18.dp,
            selectedRadius = 14.dp,
            pressedRadius = 11.dp,
            enabled = true
        )
        contentColor = animateColorAsState(
            targetValue = targetContentColor,
            animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = false),
            label = "corner_item_color_$testTag"
        ).value
    }

    val activeBackground = if (isActive) {
        Brush.horizontalGradient(
            listOf(
                activeColor.copy(alpha = 0.22f),
                activeColor.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Box(
        modifier = modifier
            .size(width = 42.dp, height = 38.dp)
            .then(
                if (!isLowEndOptimization && pressScale != 1f) {
                    Modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                } else Modifier
            )
            .clip(RoundedCornerShape(morphedCorner))
            .background(activeBackground)
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 1.dp,
                        color = activeColor.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(morphedCorner)
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box {
            val iconMod = if (iconRotation != 0f) {
                Modifier.size(20.dp).graphicsLayer { rotationZ = iconRotation }
            } else {
                Modifier.size(20.dp)
            }
            if (isLowEndOptimization) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = iconMod
                )
            } else {
                AnimatedContent(
                    targetState = icon,
                    transitionSpec = {
                        (fadeIn(ExpressiveMotion.effectsFast()) + scaleIn(initialScale = 0.7f, animationSpec = ExpressiveMotion.spatialFast()))
                            .togetherWith(fadeOut(ExpressiveMotion.effectsFast()) + scaleOut(targetScale = 0.7f))
                    },
                    label = "corner_icon_swap_$testTag"
                ) { currentIcon ->
                    Icon(
                        imageVector = currentIcon,
                        contentDescription = contentDescription,
                        tint = contentColor,
                        modifier = iconMod
                    )
                }
            }

            // Optional dot indicator (e.g. active search query)
            if (hasDotIndicator) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
            }

            // Optional numeric badge (e.g. unread count or filter count)
            if (badgeCount != null && badgeCount > 0) {
                val effectiveBadgeColor = badgeColor ?: activeColor
                val badgeTextStr = if (badgeCount > 99) "99+" else badgeCount.toString()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            translationX = 8.dp.toPx()
                            translationY = (-4).dp.toPx()
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(effectiveBadgeColor)
                        .padding(horizontal = 4.dp, vertical = 0.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLowEndOptimization) {
                        Text(
                            text = badgeTextStr,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (badgeColor != null) Color(0xFF0B0F19) else MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        AnimatedContent(
                            targetState = badgeTextStr,
                            transitionSpec = { ExpressiveMotion.popNumberTransform(false) },
                            label = "corner_badge_$testTag"
                        ) { badgeText ->
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (badgeColor != null) Color(0xFF0B0F19) else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberBellWiggle(
    enabled: Boolean,
    hasAlerts: Boolean
): Pair<Float, Float> {
    if (!enabled || !hasAlerts) return 0f to 1f

    val infiniteTransition = rememberInfiniteTransition(label = "bell_wiggle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell_rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    return rotation to pulse
}
