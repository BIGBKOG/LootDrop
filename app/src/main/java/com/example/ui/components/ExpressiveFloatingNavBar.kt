package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalDensity
import com.example.ui.theme.ExpressiveMotion
import kotlin.math.abs
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.example.ui.MainTab
import com.example.util.LocalLowEndOptimization

@Composable
fun ExpressiveFloatingNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    claimedCount: Int = 0,
    expiringSoonCount: Int = 0,
    isLowEndOptimization: Boolean = false,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val effectiveLowEnd = isLowEndOptimization || LocalLowEndOptimization.current
    val navBarWidth = 372.dp
    val navBarHeight = 64.dp
    val tabCount = 5

    val targetIndex = when (selectedTab) {
        MainTab.HOME -> 0
        MainTab.DISCOVER -> 1
        MainTab.CALENDAR -> 2
        MainTab.CLAIMED -> 3
        MainTab.SETTINGS -> 4
    }

    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val chromeSurface = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f)

    val animatedIndex = if (effectiveLowEnd) {
        targetIndex.toFloat()
    } else {
        animateFloatAsState(
            targetValue = targetIndex.toFloat(),
            animationSpec = ExpressiveMotion.spatialDefault(reducedMotion = false),
            label = "sliding_nav_indicator_index"
        ).value
    }
    // Playful M3 Expressive indicator stretch while gliding between tabs
    val glideDistance = if (effectiveLowEnd) 0f else abs(targetIndex.toFloat() - animatedIndex).coerceIn(0f, 1f)
    val pillCornerRadius = if (effectiveLowEnd) {
        24.dp
    } else {
        animateDpAsState(
            targetValue = if (glideDistance > 0.08f) 18.dp else 24.dp,
            animationSpec = ExpressiveMotion.spatialFast(reducedMotion = false),
            label = "nav_pill_corner"
        ).value
    }

    val activePillBrush = remember(themePrimary, themeSecondary) {
        Brush.horizontalGradient(
            listOf(
                themePrimary.copy(alpha = 0.24f),
                themeSecondary.copy(alpha = 0.16f)
            )
        )
    }

    val activePillBorderColor = if (effectiveLowEnd) {
        themePrimary.copy(alpha = 0.60f)
    } else {
        animateColorAsState(
            targetValue = themePrimary.copy(alpha = 0.60f),
            animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = false),
            label = "nav_pill_border_color"
        ).value
    }

    val outerBorderColor = if (effectiveLowEnd) {
        themePrimary.copy(alpha = 0.30f)
    } else {
        animateColorAsState(
            targetValue = themePrimary.copy(alpha = 0.30f),
            animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = false),
            label = "nav_outer_border_color"
        ).value
    }

    Surface(
        modifier = modifier
            .width(navBarWidth)
            .height(navBarHeight)
            .then(
                if (effectiveLowEnd) {
                    Modifier
                } else {
                    Modifier.shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(36.dp),
                        spotColor = themePrimary.copy(alpha = 0.22f),
                        ambientColor = Color(0x33000000)
                    )
                }
            )
            .border(
                width = 1.dp,
                color = outerBorderColor,
                shape = RoundedCornerShape(36.dp)
            )
            .testTag("bottom_nav_bar"),
        shape = RoundedCornerShape(36.dp),
        color = chromeSurface,
        tonalElevation = if (effectiveLowEnd) 0.dp else 4.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val totalWidth = maxWidth
            val singleTabWidth = totalWidth / tabCount

            Box(
                modifier = Modifier
                    .offset(x = singleTabWidth * animatedIndex)
                    .width(singleTabWidth)
                    .fillMaxHeight()
                    .then(
                        if (!effectiveLowEnd && glideDistance > 0.01f) {
                            Modifier.graphicsLayer {
                                scaleX = 1f + glideDistance * 0.14f
                                scaleY = 1f - glideDistance * 0.06f
                            }
                        } else Modifier
                    )
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(pillCornerRadius))
                    .background(activePillBrush)
                    .border(
                        width = 1.2.dp,
                        color = activePillBorderColor,
                        shape = RoundedCornerShape(pillCornerRadius)
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveNavTab(
                    tab = MainTab.HOME,
                    title = s.common.tabHome,
                    icon = Icons.Default.Home,
                    isSelected = selectedTab == MainTab.HOME,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.HOME) },
                    testTag = "nav_tab_home",
                    isLowEndOptimization = effectiveLowEnd,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.DISCOVER,
                    title = s.common.tabDiscover,
                    icon = Icons.Default.Explore,
                    isSelected = selectedTab == MainTab.DISCOVER,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.DISCOVER) },
                    testTag = "nav_tab_discover",
                    isLowEndOptimization = effectiveLowEnd,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.CALENDAR,
                    title = s.common.tabCalendar,
                    icon = Icons.Default.CalendarMonth,
                    isSelected = selectedTab == MainTab.CALENDAR,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.CALENDAR) },
                    testTag = "nav_tab_calendar",
                    badgeCount = if (expiringSoonCount > 0) expiringSoonCount else null,
                    isLowEndOptimization = effectiveLowEnd,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.CLAIMED,
                    title = s.common.tabClaimed,
                    icon = Icons.Default.CheckCircle,
                    isSelected = selectedTab == MainTab.CLAIMED,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.CLAIMED) },
                    testTag = "nav_tab_claimed",
                    badgeCount = if (claimedCount > 0) claimedCount else null,
                    isLowEndOptimization = effectiveLowEnd,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.SETTINGS,
                    title = s.common.tabSettings,
                    icon = Icons.Default.Settings,
                    isSelected = selectedTab == MainTab.SETTINGS,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.SETTINGS) },
                    testTag = "nav_tab_settings",
                    isLowEndOptimization = effectiveLowEnd,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ExpressiveNavTab(
    tab: MainTab,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    isLowEndOptimization: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val targetContentColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    val pressScale: Float
    val iconScale: Float
    val iconLiftPx: Float
    val contentColor: Color
    if (isLowEndOptimization) {
        pressScale = 1.0f
        iconScale = if (isSelected) 1.08f else 1.0f
        iconLiftPx = 0f
        contentColor = targetContentColor
    } else {
        val isPressed by interactionSource.collectIsPressedAsState()
        pressScale = animateFloatAsState(
            targetValue = if (isPressed) 0.89f else 1.0f,
            animationSpec = ExpressiveMotion.spatialFast(reducedMotion = false),
            label = "nav_tab_press_${tab.name}"
        ).value
        iconScale = animateFloatAsState(
            targetValue = if (isSelected) 1.13f else 0.98f,
            animationSpec = ExpressiveMotion.spatialFast(reducedMotion = false),
            label = "nav_tab_icon_scale_${tab.name}"
        ).value
        iconLiftPx = animateFloatAsState(
            targetValue = if (isSelected) with(density) { (-1.5).dp.toPx() } else 0f,
            animationSpec = ExpressiveMotion.spatialFast(reducedMotion = false),
            label = "nav_tab_icon_lift_${tab.name}"
        ).value
        contentColor = animateColorAsState(
            targetValue = targetContentColor,
            animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = false),
            label = "nav_tab_content_${tab.name}"
        ).value
    }

    val pressModifier = if (!isLowEndOptimization && pressScale != 1f) {
        Modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale }
    } else Modifier
    val iconModifier = if (iconScale != 1f || iconLiftPx != 0f) {
        Modifier.size(22.dp).graphicsLayer {
            scaleX = iconScale
            scaleY = iconScale
            translationY = iconLiftPx
        }
    } else Modifier.size(22.dp)

    Box(
        modifier = modifier
            .then(pressModifier)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (badgeCount != null && badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = activeColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            val badgeLabel = if (badgeCount > 99) "99+" else badgeCount.toString()
                            if (isLowEndOptimization) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                AnimatedContent(
                                    targetState = badgeLabel,
                                    transitionSpec = { ExpressiveMotion.popNumberTransform(false) },
                                    label = "nav_badge_count_${tab.name}"
                                ) { countText ->
                                    Text(
                                        text = countText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = iconModifier
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = iconModifier
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}
