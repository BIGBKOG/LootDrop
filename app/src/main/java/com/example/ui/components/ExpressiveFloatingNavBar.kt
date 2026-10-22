package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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

/**
 * Material 3 Expressive Floating Navigation Bar synced with the user's theme choice.
 * Features a floating capsule pill with dynamic theme-reactive borders and a fluid
 * sliding indicator capsule that smoothly glides with spring physics across the bar.
 */
@Composable
fun ExpressiveFloatingNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    claimedCount: Int = 0,
    expiringSoonCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val navBarWidth = 356.dp
    val navBarHeight = 64.dp
    val tabCount = 4

    val targetIndex = when (selectedTab) {
        MainTab.DISCOVER -> 0
        MainTab.CALENDAR -> 1
        MainTab.CLAIMED -> 2
        MainTab.SETTINGS -> 3
    }

    // Theme synchronization: dynamically sync active colors with user's accent theme choice!
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary

    // Fluid spring animation for the sliding indicator pill
    val animatedIndex by animateFloatAsState(
        targetValue = targetIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sliding_nav_indicator_index"
    )

    // Dynamic gradient brush for the sliding active indicator synced to the theme
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
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_pill_border_color"
    )

    val outerBorderColor by animateColorAsState(
        targetValue = themePrimary.copy(alpha = 0.32f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_outer_border_color"
    )

    // Outer floating capsule container matching reference screenshot, synced to theme
    Surface(
        modifier = modifier
            .width(navBarWidth)
            .height(navBarHeight)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(36.dp),
                spotColor = themePrimary.copy(alpha = 0.25f),
                ambientColor = Color(0x40000000)
            )
            .border(
                width = 1.dp,
                color = outerBorderColor,
                shape = RoundedCornerShape(36.dp)
            )
            .testTag("bottom_nav_bar"),
        shape = RoundedCornerShape(36.dp),
        color = Color(0xFF1B1E26), // Dark slate/charcoal matching screenshot
        tonalElevation = 8.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val totalWidth = maxWidth
            val singleTabWidth = totalWidth / tabCount

            // Sliding Active Pill Indicator that slides across tabs, synced to chosen theme
            Box(
                modifier = Modifier
                    .offset(x = singleTabWidth * animatedIndex)
                    .width(singleTabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(activePillBrush)
                    .border(
                        width = 1.2.dp,
                        color = activePillBorderColor,
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            // The Navigation Items placed on top of the sliding indicator
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveNavTab(
                    tab = MainTab.DISCOVER,
                    title = "Discover",
                    icon = Icons.Default.Explore,
                    isSelected = selectedTab == MainTab.DISCOVER,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.DISCOVER) },
                    testTag = "nav_tab_discover",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                ExpressiveNavTab(
                    tab = MainTab.SETTINGS,
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    isSelected = selectedTab == MainTab.SETTINGS,
                    activeColor = themePrimary,
                    onClick = { onTabSelected(MainTab.SETTINGS) },
                    testTag = "nav_tab_settings",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
    badgeCount: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press scale
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav_tab_press_${tab.name}"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav_tab_icon_scale_${tab.name}"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else Color(0xFF9E9FA7),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
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
                            Text(
                                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
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
