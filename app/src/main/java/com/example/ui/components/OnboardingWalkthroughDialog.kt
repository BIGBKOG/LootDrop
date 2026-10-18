package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import com.example.ui.density.LocalScreenDensityInfo
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.XboxGreen
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val badge: String,
    val icon: ImageVector,
    val tags: List<String>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingWalkthroughDialog(
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    val steps = remember {
        listOf(
            OnboardingStep(
                title = "PC & Console Free Radar",
                subtitle = "NEVER MISS A $60 DROP GONE FREE",
                description = "LootDrop continuously tracks PlayStation (PS5/PS4), Xbox (Series X|S/One), Nintendo Switch, Steam, Epic Games, GOG, and top stores 24/7. When a premium game drops to 100% discount, it appears on your radar immediately.",
                badge = "REAL-TIME RADAR",
                icon = Icons.Default.Radar,
                tags = listOf("PlayStation", "Xbox", "Nintendo", "Steam", "Epic Games", "GOG.com")
            ),
            OnboardingStep(
                title = "Direct 1-Tap Claim",
                subtitle = "ZERO ADS • OFFICIAL STORE LINKS",
                description = "Tap 'Claim Free' on any game to instantly open the verified official store page in your browser or launcher. Claim it directly to keep it permanently in your account forever.",
                badge = "PERMANENT TO KEEP",
                icon = Icons.Default.CardGiftcard,
                tags = listOf("1-Tap Store Link", "Permanent Keep", "Zero Paywalls", "Anti-Scam Verified")
            ),
            OnboardingStep(
                title = "Track Dollars Saved",
                subtitle = "YOUR FREE COLLECTION & STATS",
                description = "Mark claimed games with 'In Library' to calculate your total dollars saved. Watch your personal collection and financial savings grow without spending a single cent.",
                badge = "SAVINGS CALCULATOR",
                icon = Icons.Default.Savings,
                tags = listOf("$$$ Saved Counter", "In Library Tab", "Category Filters", "Sort by Worth")
            ),
            OnboardingStep(
                title = "Custom Alerts & UI",
                subtitle = "16 CYBER & BRAND THEMES • DROP ALERTS",
                description = "Get notified the instant flash giveaways drop so you never miss a 24-hour giveaway. Tailor LootDrop with 16 vibrant themes, OLED midnight mode, and rich or compact card views.",
                badge = "GAMER CUSTOMIZATION",
                icon = Icons.Default.Palette,
                tags = listOf("Instant Notifications", "16 Accent Themes", "OLED Dark Mode", "Custom Density")
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val totalSteps = steps.size
    val screenDensityInfo = LocalScreenDensityInfo.current
    val isCompactHeight = screenDensityInfo.isCompactHeight
    val isUltraCompactHeight = screenDensityInfo.isUltraCompactHeight
    val isSmallPhone = screenDensityInfo.isNarrowScreen || screenDensityInfo.widthDp < 380

    val animBoxSize = screenDensityInfo.walkthroughAnimBoxSize
    val buttonHeight = screenDensityInfo.walkthroughButtonHeight

    BackHandler {
        if (currentStepIndex > 0) {
            currentStepIndex--
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("onboarding_walkthrough_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = screenDensityInfo.maxDialogWidth)
                        .fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(top = 4.dp)
                        ) {
                            // Top App Header & Skip Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (isSmallPhone) 12.dp else 18.dp,
                                        vertical = if (isCompactHeight) 4.dp else 8.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCompactHeight) 28.dp else 32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(if (isCompactHeight) 16.dp else 18.dp)
                                        )
                                    }
                                    Text(
                                        text = "LootDrop Feature Tour",
                                        fontSize = if (isCompactHeight) 13.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 1.5.dp)
                                    ) {
                                        Text(
                                            text = "${screenDensityInfo.densityDpi} DPI",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = onFinish,
                                    modifier = Modifier.testTag("onboarding_skip_button")
                                ) {
                                    Text(
                                        text = "Skip",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))

                            // Step Indicator Progress Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = if (isCompactHeight) 4.dp else 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until totalSteps) {
                                    val isActive = i == currentStepIndex
                                    val isCompleted = i < currentStepIndex
                                    val pillWidth by animateDpAsState(
                                        targetValue = if (isActive) (if (isSmallPhone) 20.dp else 28.dp) else 8.dp,
                                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                        label = "pillWidth"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .height(if (isCompactHeight) 6.dp else 8.dp)
                                            .width(pillWidth)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isActive -> MaterialTheme.colorScheme.primary
                                                    isCompleted -> NeonGreen
                                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                                }
                                            )
                                            .clickable { currentStepIndex = i }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))
                        }
                    },
                    bottomBar = {
                        // Bottom Navigation Action Buttons - Fixed & Docked permanently above navigation bars
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (isSmallPhone) 12.dp else 18.dp,
                                        vertical = if (isUltraCompactHeight) 6.dp else if (isCompactHeight) 8.dp else 12.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (currentStepIndex > 0) {
                                    OutlinedButton(
                                        onClick = { currentStepIndex-- },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(buttonHeight)
                                            .testTag("onboarding_back_button"),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Back",
                                            fontSize = if (isSmallPhone) 12.sp else 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Button(
                                    onClick = {
                                        if (currentStepIndex < totalSteps - 1) {
                                            currentStepIndex++
                                        } else {
                                            onFinish()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(if (currentStepIndex > 0) 1.5f else 1f)
                                        .height(buttonHeight)
                                        .testTag("onboarding_next_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentStepIndex == totalSteps - 1) NeonGreen else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = if (currentStepIndex == totalSteps - 1) "Explore Free Loot" else "Next Step",
                                        fontSize = if (isSmallPhone) 13.sp else 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B0F19)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (currentStepIndex == totalSteps - 1) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color(0xFF0B0F19),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentStepIndex,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    (slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + fadeIn(tween(350))).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { -it },
                                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                                        ) + fadeOut(tween(350))
                                    )
                                } else {
                                    (slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                                    ) + fadeIn(tween(350))).togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { it },
                                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                                        ) + fadeOut(tween(350))
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            label = "onboarding_step_content"
                        ) { stepIdx ->
                            val step = steps[stepIdx]
                            val animScale = (animBoxSize.value / 135f).coerceIn(0.6f, 1.15f)

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = if (isSmallPhone) 14.dp else 20.dp)
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Top
                            ) {
                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 2.dp else 6.dp))

                                // Custom Animation Canvas per Step (DPI adaptive size and scale)
                                Box(
                                    modifier = Modifier
                                        .size(animBoxSize)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                scaleX = animScale
                                                scaleY = animScale
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (stepIdx) {
                                            0 -> RadarScanAnimation()
                                            1 -> DirectClaimAnimation()
                                            2 -> SavingsTrackerAnimation()
                                            3 -> AlertsAndThemesAnimation()
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 6.dp else if (isCompactHeight) 8.dp else 12.dp))

                                // Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(
                                            horizontal = if (isUltraCompactHeight) 8.dp else 10.dp,
                                            vertical = if (isUltraCompactHeight) 2.dp else 4.dp
                                        )
                                ) {
                                    Text(
                                        text = step.badge,
                                        fontSize = if (isUltraCompactHeight) 9.5.sp else 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 4.dp else 8.dp))

                                // Main Title
                                Text(
                                    text = step.title,
                                    fontSize = if (isUltraCompactHeight) 17.sp else if (isCompactHeight) 19.sp else 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 2.dp else 4.dp))

                                // Subtitle
                                Text(
                                    text = step.subtitle,
                                    fontSize = if (isUltraCompactHeight) 9.5.sp else if (isCompactHeight) 10.5.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 4.dp else 8.dp))

                                // Description
                                Text(
                                    text = step.description,
                                    fontSize = if (isUltraCompactHeight) 11.5.sp else if (isCompactHeight) 12.5.sp else 13.5.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = if (isUltraCompactHeight) 15.sp else if (isCompactHeight) 16.5.sp else 18.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 6.dp else 10.dp))

                                // Highlight Chips
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalArrangement = Arrangement.spacedBy(if (isUltraCompactHeight) 4.dp else 6.dp)
                                ) {
                                    step.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surface)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(
                                                    horizontal = if (isUltraCompactHeight) 7.dp else 9.dp,
                                                    vertical = if (isUltraCompactHeight) 3.dp else 5.dp
                                                )
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = if (isUltraCompactHeight) 10.5.sp else 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (isUltraCompactHeight) 6.dp else 12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Live Radar Display
 * Sweep line rotating 360 degrees, pulsing radar rings, and blinking game target markers.
 */
@Composable
fun RadarScanAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulseAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val neonGreen = NeonGreen

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width / 2 - 12.dp.toPx()

            // Concentric radar circles
            for (i in 1..3) {
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = maxRadius * (i / 3f),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Expanding sonar pulse wave
            drawCircle(
                color = neonGreen.copy(alpha = pulseAlpha),
                radius = maxRadius * pulseScale,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Crosshair lines
            drawLine(
                color = primaryColor.copy(alpha = 0.3f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.3f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Radar Sweep line
            val rad = Math.toRadians(rotation.toDouble())
            val lineEnd = Offset(
                x = (center.x + maxRadius * cos(rad)).toFloat(),
                y = (center.y + maxRadius * sin(rad)).toFloat()
            )
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(neonGreen, primaryColor, Color.Transparent),
                    center = center,
                    radius = maxRadius
                ),
                start = center,
                end = lineEnd,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Target Loot nodes (PlayStation, Xbox, Nintendo, Steam, Epic blips)
            val blipPs = Offset(center.x - maxRadius * 0.45f, center.y - maxRadius * 0.4f)
            val blipXbox = Offset(center.x + maxRadius * 0.55f, center.y - maxRadius * 0.35f)
            val blipNintendo = Offset(center.x, center.y + maxRadius * 0.55f)
            val blipSteam = Offset(center.x - maxRadius * 0.55f, center.y + maxRadius * 0.35f)
            val blipEpic = Offset(center.x + maxRadius * 0.45f, center.y + maxRadius * 0.4f)

            drawCircle(color = PlayStationBlue, radius = 5.dp.toPx(), center = blipPs)
            drawCircle(color = XboxGreen, radius = 5.dp.toPx(), center = blipXbox)
            drawCircle(color = NintendoRed, radius = 6.dp.toPx(), center = blipNintendo)
            drawCircle(color = SteamBlue, radius = 5.dp.toPx(), center = blipSteam)
            drawCircle(color = EpicGamesBlue, radius = 5.dp.toPx(), center = blipEpic)
        }

        // Center hub radar dot
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(neonGreen)
        )
    }
}

/**
 * Animated 1-Tap Claim Showcase
 * Floating drop box + particle burst + animated button clicking into "CLAIMED!"
 */
@Composable
fun DirectClaimAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "claim")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    var isSimulatedClaimed by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1800)
            isSimulatedClaimed = true
            delay(1800)
            isSimulatedClaimed = false
        }
    }

    val buttonColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSimulatedClaimed) NeonGreen else MaterialTheme.colorScheme.primary,
        animationSpec = tween(400),
        label = "btnColor"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isSimulatedClaimed) 1.05f else 1.0f,
        animationSpec = tween(300),
        label = "btnScale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Floating Loot Drop Chest / Gamepad
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = bounceY }
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            Color(0xFF131A2A)
                        )
                    )
                )
                .border(
                    2.dp,
                    if (isSimulatedClaimed) NeonGreen else MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSimulatedClaimed) Icons.Default.CheckCircle else Icons.Default.SportsEsports,
                contentDescription = null,
                tint = if (isSimulatedClaimed) NeonGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Simulated Claim Button
        Box(
            modifier = Modifier
                .scale(buttonScale)
                .clip(RoundedCornerShape(10.dp))
                .background(buttonColor)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isSimulatedClaimed) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                    contentDescription = null,
                    tint = Color(0xFF0B0F19),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isSimulatedClaimed) "100% CLAIMED!" else "CLAIM FREE ($59.99 -> $0)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B0F19)
                )
            }
        }
    }
}

/**
 * Animated Dollar Savings Tracker Showcase
 * Counting up money saved with a glowing green vault bar.
 */
@Composable
fun SavingsTrackerAnimation() {
    var animatedDollars by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            animatedDollars = 0f
            delay(300)
            val targets = listOf(29.99f, 89.98f, 159.97f, 329.95f, 489.92f)
            for (t in targets) {
                animatedDollars = t
                delay(600)
            }
            delay(1500)
        }
    }

    val displayDollars by animateFloatAsState(
        targetValue = animatedDollars,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "dollars"
    )

    val progressFraction = (displayDollars / 500f).coerceIn(0.1f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(NeonGreen.copy(alpha = 0.2f))
                .border(1.5.dp, NeonGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = NeonGreen,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$${String.format(java.util.Locale.US, "%.2f", displayDollars)}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = NeonGreen
        )

        Text(
            text = "SAVED IN FREE GAMES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Animated collection progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(NeonGreen, MaterialTheme.colorScheme.primary)
                        )
                    )
            )
        }
    }
}

/**
 * Animated Notification Alerts & Cyber Theming Showcase
 * Swinging notification bell and morphing accent theme color swatches.
 */
@Composable
fun AlertsAndThemesAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "alerts")
    val bellRotation by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell"
    )

    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Bell with expanding notification wave
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse wave
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .scale(waveScale)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = waveAlpha),
                        CircleShape
                    )
            )

            // Swinging Bell
            Box(
                modifier = Modifier
                    .graphicsLayer { rotationZ = bellRotation }
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cyber theme color circles
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val themeColors = listOf(
                NeonCyan,
                NeonGreen,
                NintendoRed,
                SteamBlue,
                GogPurple,
                Color(0xFFFBBF24)
            )
            themeColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.5.dp, Color(0xFF0F172A), CircleShape)
                )
            }
        }
    }
}
