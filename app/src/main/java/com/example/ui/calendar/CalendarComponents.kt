package com.example.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.rememberExpressiveCornerRadius
import java.time.LocalDate
import java.util.Locale

internal val CalendarNeonAmber = Color(0xFFFFB300)

@Composable
internal fun CalendarHeroBanner(
    totalExpiringCount: Int,
    totalValueUsd: Double,
    isCompact: Boolean,
    onToggleCompact: () -> Unit,
    reducedMotion: Boolean
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceHigh = MaterialTheme.colorScheme.surfaceContainerHigh

    val bannerBrush = remember(primaryColor, secondaryColor, surfaceHigh) {
        Brush.horizontalGradient(
            listOf(
                primaryColor.copy(alpha = 0.14f),
                secondaryColor.copy(alpha = 0.08f),
                surfaceHigh.copy(alpha = 0.45f)
            )
        )
    }

    val chevronRot by animateFloatAsState(
        targetValue = if (isCompact) 0f else 180f,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion),
        label = "cal_hero_chevron"
    )

    Card(
        onClick = onToggleCompact,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("calendar_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBrush)
                .padding(horizontal = 14.dp, vertical = if (isCompact) 8.dp else 11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 26.dp else 32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(primaryColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(if (isCompact) 15.dp else 18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isEs) "RADAR DE EXPIRACIÓN" else "EXPIRING GAMES RADAR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        AnimatedVisibility(
                            visible = !isCompact,
                            enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
                            exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
                        ) {
                            Text(
                                text = if (isEs) {
                                    "$totalExpiringCount juego${if (totalExpiringCount != 1) "s" else ""} por vencer en próx. 14 días"
                                } else {
                                    "$totalExpiringCount game${if (totalExpiringCount != 1) "s" else ""} expiring in next 14 days"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(0.8.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isEs) String.format(Locale.US, "$%.2f GRATIS", totalValueUsd) else String.format(Locale.US, "$%.2f FREE", totalValueUsd),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isCompact) {
                            if (isEs) "Mostrar calendario" else "Show calendar strip"
                        } else {
                            if (isEs) "Ocultar calendario" else "Hide calendar strip"
                        },
                        tint = primaryColor,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = chevronRot }
                    )
                }
            }
        }
    }
}

@Composable
internal fun CalendarDayChip(
    label: String,
    dateNumber: String,
    month: String,
    expiringCount: Int,
    isSelected: Boolean,
    isUrgent: Boolean = false,
    reducedMotion: Boolean = false,
    tagKey: String = label.lowercase(),
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> primaryColor
            isUrgent -> CalendarNeonAmber.copy(alpha = 0.8f)
            expiringCount > 0 -> primaryColor.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
        },
        animationSpec = ExpressiveMotion.effectsDefault(reducedMotion),
        label = "calendar_chip_border"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> primaryColor.copy(alpha = 0.18f)
            isUrgent -> CalendarNeonAmber.copy(alpha = 0.12f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = ExpressiveMotion.effectsDefault(reducedMotion),
        label = "calendar_chip_container"
    )

    val chipScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.91f
            isSelected -> 1.04f
            else -> 1f
        },
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion),
        label = "calendar_chip_scale"
    )

    val chipCorner = rememberExpressiveCornerRadius(
        isSelected = isSelected,
        isPressed = isPressed,
        baseRadius = 12.dp,
        selectedRadius = 17.dp,
        pressedRadius = 20.dp,
        enabled = !reducedMotion
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(chipCorner),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor),
        modifier = Modifier
            .width(62.dp)
            .height(78.dp)
            .graphicsLayer {
                scaleX = chipScale
                scaleY = chipScale
            }
            .testTag("calendar_chip_${tagKey}_$dateNumber")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) primaryColor else if (isUrgent) CalendarNeonAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (expiringCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isUrgent) CalendarNeonAmber else primaryColor)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$expiringCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isUrgent) Color(0xFF0B0F19) else MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Text(
                    text = month,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun CalendarEmptyState(
    selectedDate: LocalDate?,
    onResetFilter: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("calendar_empty_state_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (selectedDate != null) {
                    if (isEs) "Sin Expiraciones en esta Fecha" else "No Expirations on this Date"
                } else {
                    s.home.calendarEmptyTitle
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (selectedDate != null) {
                    s.home.calendarDaySelectedNone
                } else {
                    s.home.calendarEmptySubtitle
                },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onResetFilter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = s.home.calendarShowAllDates,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
