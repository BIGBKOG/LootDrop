package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.example.i18n.AppStrings
import com.example.i18n.LocalAppStrings
import com.example.i18n.StringsEn
import com.example.ui.theme.EpicGamesBlue
import com.example.ui.theme.GogPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NintendoRed
import com.example.ui.theme.PlayStationBlue
import com.example.ui.theme.SteamBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.XboxGreen
import com.example.util.LocalLowEndOptimization
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

internal fun defaultOnboardingSteps(s: AppStrings = StringsEn): List<OnboardingStep> {
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.Radar,
        Icons.Default.CardGiftcard,
        Icons.Default.Savings,
        Icons.Default.Palette
    )
    return s.dialogs.onboardingSteps.mapIndexed { index, step ->
        OnboardingStep(
            title = step.title,
            subtitle = step.subtitle,
            description = step.description,
            badge = step.badge,
            icon = icons.getOrElse(index) { Icons.Default.Person },
            tags = step.tags
        )
    }
}

@Composable
fun RadarScanAnimation() {
    val lowEnd = LocalLowEndOptimization.current
    val rotation: Float
    val pulseScale: Float
    val pulseAlpha: Float
    if (lowEnd) {
        rotation = 45f
        pulseScale = 0.72f
        pulseAlpha = 0.45f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "radar")
        rotation = infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
            label = "radarSweep"
        ).value
        pulseScale = infiniteTransition.animateFloat(
            initialValue = 0.3f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Restart),
            label = "radarPulse"
        ).value
        pulseAlpha = infiniteTransition.animateFloat(
            initialValue = 0.8f, targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Restart),
            label = "radarPulseAlpha"
        ).value
    }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width / 2 - 12.dp.toPx()

            for (i in 1..3) {
                drawCircle(primaryColor.copy(alpha = 0.25f), radius = maxRadius * (i / 3f), center = center, style = Stroke(width = 1.5.dp.toPx()))
            }
            drawCircle(NeonGreen.copy(alpha = pulseAlpha), radius = maxRadius * pulseScale, center = center, style = Stroke(width = 2.dp.toPx()))

            drawLine(primaryColor.copy(alpha = 0.3f), Offset(center.x, center.y - maxRadius), Offset(center.x, center.y + maxRadius), strokeWidth = 1.dp.toPx())
            drawLine(primaryColor.copy(alpha = 0.3f), Offset(center.x - maxRadius, center.y), Offset(center.x + maxRadius, center.y), strokeWidth = 1.dp.toPx())

            val rad = Math.toRadians(rotation.toDouble())
            val lineEnd = Offset((center.x + maxRadius * cos(rad)).toFloat(), (center.y + maxRadius * sin(rad)).toFloat())
            drawLine(
                brush = Brush.radialGradient(listOf(NeonGreen, primaryColor, Color.Transparent), center = center, radius = maxRadius),
                start = center, end = lineEnd, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round
            )

            drawCircle(PlayStationBlue, radius = 5.dp.toPx(), center = Offset(center.x - maxRadius * 0.45f, center.y - maxRadius * 0.4f))
            drawCircle(XboxGreen, radius = 5.dp.toPx(), center = Offset(center.x + maxRadius * 0.55f, center.y - maxRadius * 0.35f))
            drawCircle(NintendoRed, radius = 6.dp.toPx(), center = Offset(center.x, center.y + maxRadius * 0.55f))
            drawCircle(SteamBlue, radius = 5.dp.toPx(), center = Offset(center.x - maxRadius * 0.55f, center.y + maxRadius * 0.35f))
            drawCircle(EpicGamesBlue, radius = 5.dp.toPx(), center = Offset(center.x + maxRadius * 0.45f, center.y + maxRadius * 0.4f))
        }
        Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(NeonGreen))
    }
}

@Composable
fun DirectClaimAnimation() {
    val s = LocalAppStrings.current
    val lowEnd = LocalLowEndOptimization.current
    val bounceY: Float
    val isSimulatedClaimed: Boolean
    val buttonColor: Color
    val buttonScale: Float
    if (lowEnd) {
        bounceY = 0f
        isSimulatedClaimed = true
        buttonColor = NeonGreen
        buttonScale = 1.0f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "claim")
        bounceY = infiniteTransition.animateFloat(
            initialValue = -10f, targetValue = 10f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bounce"
        ).value
        var simulatedState by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1800)
                simulatedState = true
                delay(1800)
                simulatedState = false
            }
        }
        isSimulatedClaimed = simulatedState
        buttonColor = animateColorAsState(if (isSimulatedClaimed) NeonGreen else MaterialTheme.colorScheme.primary, tween(400), label = "btnColor").value
        buttonScale = animateFloatAsState(if (isSimulatedClaimed) 1.05f else 1.0f, tween(300), label = "btnScale").value
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = bounceY }
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color(0xFF131A2A))))
                .border(2.dp, if (isSimulatedClaimed) NeonGreen else MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
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
        Box(
            modifier = Modifier.scale(buttonScale).clip(RoundedCornerShape(10.dp)).background(buttonColor).padding(horizontal = 18.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = if (isSimulatedClaimed) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                    contentDescription = null, tint = Color(0xFF0B0F19), modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isSimulatedClaimed) s.dialogs.onboardingClaimAnimClaimed else s.dialogs.onboardingClaimAnimFree,
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B0F19)
                )
            }
        }
    }
}

@Composable
fun SavingsTrackerAnimation() {
    val s = LocalAppStrings.current
    val lowEnd = LocalLowEndOptimization.current
    val displayDollars: Float
    if (lowEnd) {
        displayDollars = 489.92f
    } else {
        var animatedDollars by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(Unit) {
            val targets = listOf(29.99f, 89.98f, 159.97f, 329.95f, 489.92f)
            while (true) {
                animatedDollars = 0f
                delay(300)
                for (t in targets) {
                    animatedDollars = t
                    delay(600)
                }
                delay(1500)
            }
        }

        displayDollars = animateFloatAsState(animatedDollars, tween(500, easing = FastOutSlowInEasing), label = "dollars").value
    }
    val progressFraction = (displayDollars / 500f).coerceIn(0.1f, 1f)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape).background(NeonGreen.copy(alpha = 0.2f)).border(1.5.dp, NeonGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Savings, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("$${String.format(Locale.US, "%.2f", displayDollars)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = NeonGreen)
        Text(s.dialogs.onboardingSavedAnimCaption, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth(0.85f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFF1E293B))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(NeonGreen, MaterialTheme.colorScheme.primary)))
            )
        }
    }
}

@Composable
fun AlertsAndThemesAnimation() {
    val lowEnd = LocalLowEndOptimization.current
    val bellRotation: Float
    val waveScale: Float
    val waveAlpha: Float
    if (lowEnd) {
        bellRotation = 0f
        waveScale = 1.0f
        waveAlpha = 0.45f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "alerts")
        bellRotation = infiniteTransition.animateFloat(
            initialValue = -18f, targetValue = 18f,
            animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "bell"
        ).value
        waveScale = infiniteTransition.animateFloat(
            initialValue = 0.6f, targetValue = 1.4f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
            label = "wave"
        ).value
        waveAlpha = infiniteTransition.animateFloat(
            initialValue = 0.8f, targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Restart),
            label = "waveAlpha"
        ).value
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(70.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(54.dp).scale(waveScale).clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = waveAlpha), CircleShape)
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { rotationZ = bellRotation }
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(NeonCyan, NeonGreen, NintendoRed, SteamBlue, GogPurple, Color(0xFFFBBF24)).forEach { color ->
                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(color).border(1.5.dp, Color(0xFF0F172A), CircleShape))
            }
        }
    }
}

@Composable
internal fun ProfileAvatarTourPreview(
    name: String,
    selectedUri: Uri?,
    avatarFile: File?,
    shouldRemove: Boolean,
    onPickPhoto: () -> Unit
) {
    val context = LocalContext.current
    val lowEnd = LocalLowEndOptimization.current
    val pulseAlpha = if (lowEnd) {
        0.8f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "profile_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.4f, targetValue = 0.9f,
            animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
            label = "pulseAlpha"
        ).value
    }

    Box(modifier = Modifier.fillMaxSize().clickable { onPickPhoto() }, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(2.dp, Brush.sweepGradient(listOf(NeonCyan.copy(alpha = pulseAlpha), NeonGreen.copy(alpha = pulseAlpha), NeonCyan.copy(alpha = pulseAlpha))), CircleShape)
                .background(Color(0xFF0F121A)),
            contentAlignment = Alignment.Center
        ) {
            val hasFile = !shouldRemove && avatarFile != null && avatarFile.exists()
            when {
                selectedUri != null -> AsyncImage(
                    model = remember(context, selectedUri, lowEnd) {
                        ImageRequest.Builder(context)
                            .data(selectedUri)
                            .crossfade(!lowEnd)
                            .bitmapConfig(if (lowEnd) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
                            .allowRgb565(true)
                            .precision(if (lowEnd) Precision.INEXACT else Precision.AUTOMATIC)
                            .size(240, 240)
                            .build()
                    },
                    contentDescription = "Selected Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                hasFile -> AsyncImage(
                    model = remember(context, avatarFile, lowEnd) {
                        ImageRequest.Builder(context)
                            .data(avatarFile)
                            .crossfade(!lowEnd)
                            .bitmapConfig(if (lowEnd) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
                            .allowRgb565(true)
                            .precision(if (lowEnd) Precision.INEXACT else Precision.AUTOMATIC)
                            .size(240, 240)
                            .build()
                    },
                    contentDescription = "Profile Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                name.isNotBlank() -> Text(name.trim().take(1).uppercase(), fontSize = 38.sp, fontWeight = FontWeight.Black, color = NeonCyan)
                else -> Icon(Icons.Default.Person, contentDescription = "Default Avatar", tint = NeonCyan, modifier = Modifier.size(50.dp))
            }

            Box(
                modifier = Modifier.size(28.dp).align(Alignment.BottomEnd).clip(CircleShape).background(Color(0xFF0B0E17)).border(1.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Add or Change Photo", tint = NeonCyan, modifier = Modifier.size(15.dp))
            }
        }
    }
}
