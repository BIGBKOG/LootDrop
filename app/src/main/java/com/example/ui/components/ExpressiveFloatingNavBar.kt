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
import com.example.ui.MainTab

@Composable
fun ExpressiveFloatingNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    claimedCount: Int = 0,
    expiringSoonCount: Int = 0,
    isLowEndOptimization: Boolean = false,
    modifier: Modifier = Modifier
) {
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

    val animatedIndex by animateFloatAsState(
        targetValue = targetIndex.toFloat(),
        animationSpec = ExpressiveMotion.spatialDefault(reducedMotion = isLowEndOptimization),
        label = "sliding_nav_indicator_index"
    )
    // Playful M3 Expressive indicator stretch while gliding between tabs
    val glideDistance = abs(targetIndex.toFloat() - animatedIndex).coerceIn(0f, 1f)
    val pillCornerRadius by animateDpAsState(
        targetValue = if (glideDistance > 0.08f) 18.dp else 24.dp,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion = isLowEndOptimization),
        label = "nav_pill_corner"
    )

    val activePillBrush = remember(themePrimary, themeSecondary) {
        Brush.horizontalGradient(
            listOf(
                themePrimary.copy(alpha = 0.35f),
                themeSecondary.copy(alpha = 0.22f)
            )
        )
    }

    val activePillBorderColor by animateColorAsState(
        targetValue = themePrimary.copy(alpha = 0.75f),
        animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = isLowEndOptimization),
        label = "nav_pill_border_color"
    )

    val outerBorderColor by animateColorAsState(
        targetValue = themePrimary.copy(alpha = 0.32f),
        animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = isLowEndOptimization),
        label = "nav_outer_border_color"
    )

    Surface(
        modifier = modifier
            .width(navBarWidth)
            .height(navBarHeight)
            .then(
                if (isLowEndOptimization) {
                    Modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(36.dp))
                } else {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(36.dp),
                        spotColor = themePrimary.copy(alpha = 0.25f),
                        ambientColor = Color(0x40000000)
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
        color = Color(0xFF1B1E26),
        tonalElevation = if (isLowEndOptimization) 0.dp else 8.dp
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
                    .graphicsLayer {
                        if (!isLowEndOptimization && glideDistance > 0.01f) {
                            scaleX = 1f + glideDistance * 0.14f
                            scaleY = 1f - glideDistance * 0.06f
                        }
                    }
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
                    title = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selectedTab == MainTab.HOME,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.HOME) },
                    testTag = "nav_tab_home",
                    isLowEndOptimization = isLowEndOptimization,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.DISCOVER,
                    title = "Discover",
                    icon = Icons.Default.Explore,
                    isSelected = selectedTab == MainTab.DISCOVER,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.DISCOVER) },
                    testTag = "nav_tab_discover",
                    isLowEndOptimization = isLowEndOptimization,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.CALENDAR,
                    title = "Calendar",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = selectedTab == MainTab.CALENDAR,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.CALENDAR) },
                    testTag = "nav_tab_calendar",
                    badgeCount = if (expiringSoonCount > 0) expiringSoonCount else null,
                    isLowEndOptimization = isLowEndOptimization,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.CLAIMED,
                    title = "In Library",
                    icon = Icons.Default.CheckCircle,
                    isSelected = selectedTab == MainTab.CLAIMED,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.CLAIMED) },
                    testTag = "nav_tab_claimed",
                    badgeCount = if (claimedCount > 0) claimedCount else null,
                    isLowEndOptimization = isLowEndOptimization,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.SETTINGS,
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = selectedTab == MainTab.SETTINGS,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.SETTINGS) },
                    testTag = "nav_tab_settings",
                    isLowEndOptimization = isLowEndOptimization,
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
    val isPressed by interactionSource.collectIsPressedAsState()
    val density = LocalDensity.current

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.89f else 1.0f,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion = isLowEndOptimization),
        label = "nav_tab_press_${tab.name}"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.13f else 0.98f,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion = isLowEndOptimization),
        label = "nav_tab_icon_scale_${tab.name}"
    )

    val iconLiftPx by animateFloatAsState(
        targetValue = if (isSelected && !isLowEndOptimization) with(density) { (-1.5).dp.toPx() } else 0f,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion = isLowEndOptimization),
        label = "nav_tab_icon_lift_${tab.name}"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else Color(0xFF9E9FA7),
        animationSpec = ExpressiveMotion.effectsDefault(reducedMotion = isLowEndOptimization),
        label = "nav_tab_content_${tab.name}"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
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
                            contentColor = Color(0xFF0B0F19),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            AnimatedContent(
                                targetState = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                transitionSpec = { ExpressiveMotion.popNumberTransform(isLowEndOptimization) },
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
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor,
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                                translationY = iconLiftPx
                            }
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            translationY = iconLiftPx
                        }
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
