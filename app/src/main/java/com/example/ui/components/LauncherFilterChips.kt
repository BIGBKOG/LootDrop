package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherType
import com.example.ui.theme.GamingDarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.material.icons.filled.Check

@Composable
fun LauncherFilterChips(
    selectedLaunchers: Set<LauncherType>,
    onLauncherToggle: (LauncherType) -> Unit,
    counts: Map<LauncherType, Int>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("launcher_filter_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
    ) {
        items(LauncherType.values()) { launcher ->
            val isSelected = if (launcher == LauncherType.ALL) {
                selectedLaunchers.isEmpty() || selectedLaunchers.contains(LauncherType.ALL)
            } else {
                selectedLaunchers.contains(launcher)
            }
            val count = counts[launcher] ?: 0
            val badgeColor = Color(launcher.badgeColorHex)

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) badgeColor.copy(alpha = 0.22f) else GamingDarkSurface,
                label = "chip_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) badgeColor else Color(0xFF26334D),
                label = "chip_border"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onLauncherToggle(launcher) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("launcher_chip_${launcher.name.lowercase()}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Small launcher indicator dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor)
                    )

                    Text(
                        text = launcher.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )

                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) badgeColor else Color(0xFF1E283C))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val isDarkBrand = launcher == LauncherType.PLAYSTATION || launcher == LauncherType.XBOX || launcher == LauncherType.EPIC
                            Text(
                                text = count.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) (if (isDarkBrand) Color.White else Color(0xFF0B0F19)) else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
