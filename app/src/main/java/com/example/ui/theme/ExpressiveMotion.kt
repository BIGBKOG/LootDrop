package com.example.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Material 3 Expressive Motion System
 *
 * Implements M3 Expressive spatial springs (playful overshoot for bounds, translation,
 * scale, and shape morphing) alongside critical-damped effects springs (no overshoot
 * for color, opacity, and elevation). Respects reduced-motion / low-end device flags.
 */
object ExpressiveMotion {

    // ── Spatial Springs (Bounds, Scale, Offset, Shape) ───────────────────────

    fun <T> spatialFast(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) {
            tween(durationMillis = 120, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = 0.68f,
                stiffness = 900f
            )
        }

    fun <T> spatialDefault(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) {
            tween(durationMillis = 160, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = 0.74f,
                stiffness = 440f
            )
        }

    fun <T> spatialSlow(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) {
            tween(durationMillis = 200, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = 0.78f,
                stiffness = 240f
            )
        }

    // ── Effects Springs (Alpha, Color, Elevation — Zero Overshoot) ───────────

    fun <T> effectsFast(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) {
            tween(durationMillis = 100)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 1200f
            )
        }

    fun <T> effectsDefault(reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) {
            tween(durationMillis = 150)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 520f
            )
        }

    // ── Expressive Container & Content Transitions ───────────────────────────

    fun expressiveExpandEnter(reducedMotion: Boolean = false): EnterTransition =
        if (reducedMotion) {
            fadeIn(tween(120)) + expandVertically(tween(150))
        } else {
            fadeIn(effectsDefault()) +
                expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.76f,
                        stiffness = 420f
                    )
                ) +
                scaleIn(
                    initialScale = 0.96f,
                    animationSpec = spatialFast()
                )
        }

    fun expressiveCollapseExit(reducedMotion: Boolean = false): ExitTransition =
        if (reducedMotion) {
            fadeOut(tween(100)) + shrinkVertically(tween(130))
        } else {
            fadeOut(effectsFast()) +
                shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = 520f
                    )
                ) +
                scaleOut(
                    targetScale = 0.97f,
                    animationSpec = effectsFast()
                )
        }

    fun popNumberTransform(reducedMotion: Boolean = false): ContentTransform =
        if (reducedMotion) {
            fadeIn(tween(100)) togetherWith fadeOut(tween(80))
        } else {
            (fadeIn(effectsFast()) +
                scaleIn(initialScale = 0.78f, animationSpec = spatialFast()) +
                slideInVertically(animationSpec = spatialFast()) { -it / 3 })
                .togetherWith(
                    fadeOut(effectsFast()) +
                        scaleOut(targetScale = 0.84f, animationSpec = effectsFast()) +
                        slideOutVertically(animationSpec = effectsFast()) { it / 3 }
                )
        }

    fun directionalTabSlideTransform(
        forward: Boolean,
        reducedMotion: Boolean = false
    ): ContentTransform =
        if (reducedMotion) {
            fadeIn(tween(120)) togetherWith fadeOut(tween(100))
        } else {
            val dir = if (forward) 1 else -1
            (slideInHorizontally(animationSpec = spatialDefault()) { (it * 0.14f * dir).roundToInt() } +
                fadeIn(effectsDefault()) +
                scaleIn(initialScale = 0.98f, animationSpec = spatialDefault()))
                .togetherWith(
                    slideOutHorizontally(animationSpec = effectsDefault()) { (-it * 0.10f * dir).roundToInt() } +
                        fadeOut(effectsFast()) +
                        scaleOut(targetScale = 0.98f, animationSpec = effectsFast())
                )
        }
}

/**
 * Tactile M3 Expressive press scale modifier using an existing [InteractionSource].
 */
fun Modifier.expressivePressScale(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.94f
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion = !enabled),
        label = "expressive_press_scale"
    )
    if (scale == 1f) this else this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * M3 Expressive corner radius morphing on selection / press.
 */
@Composable
fun rememberExpressiveCornerRadius(
    isSelected: Boolean = false,
    isPressed: Boolean = false,
    baseRadius: Dp = 12.dp,
    selectedRadius: Dp = 18.dp,
    pressedRadius: Dp = 22.dp,
    enabled: Boolean = true
): Dp {
    val target = when {
        isPressed -> pressedRadius
        isSelected -> selectedRadius
        else -> baseRadius
    }
    val radius by animateDpAsState(
        targetValue = target,
        animationSpec = ExpressiveMotion.spatialFast(reducedMotion = !enabled),
        label = "expressive_corner_radius"
    )
    return radius
}

/**
 * State controller for fluid Material 3 Expressive collapsing top bars & banners.
 *
 * Tracks dynamically measured header height (`maxHeightPx`) and smoothly collapses/expands
 * via a [NestedScrollConnection], snapping cleanly with a spatial spring when scrolling ends.
 */
@Stable
class CollapsibleHeaderState(
    private val coroutineScope: CoroutineScope,
    private val reducedMotion: Boolean
) {
    var maxHeightPx by mutableFloatStateOf(0f)
        private set

    var heightOffset by mutableFloatStateOf(0f)
        private set

    private var snapJob: Job? = null

    val collapsedFraction: Float
        get() = if (maxHeightPx <= 1f) 0f else (-heightOffset / maxHeightPx).coerceIn(0f, 1f)

    val isCollapsed: Boolean
        get() = maxHeightPx > 1f && heightOffset <= -maxHeightPx + 1f

    fun updateMaxHeight(newHeightPx: Float) {
        if (abs(maxHeightPx - newHeightPx) > 0.5f) {
            val wasFullyCollapsed = isCollapsed
            maxHeightPx = newHeightPx
            heightOffset = if (wasFullyCollapsed) {
                -newHeightPx
            } else {
                heightOffset.coerceIn(-newHeightPx, 0f)
            }
        }
    }

    fun applyScrollDelta(deltaY: Float): Float {
        if (maxHeightPx <= 1f) return 0f
        snapJob?.cancel()
        val previous = heightOffset
        val updated = (previous + deltaY).coerceIn(-maxHeightPx, 0f)
        heightOffset = updated
        return updated - previous
    }

    fun expandAnimated() {
        snapTo(0f)
    }

    fun collapseAnimated() {
        if (maxHeightPx > 1f) {
            snapTo(-maxHeightPx)
        }
    }

    fun snapAfterScroll(velocityY: Float = 0f) {
        if (maxHeightPx <= 1f) return
        val threshold = -maxHeightPx * 0.45f
        val target = when {
            velocityY > 350f -> 0f
            velocityY < -350f -> -maxHeightPx
            heightOffset >= threshold -> 0f
            else -> -maxHeightPx
        }
        if (abs(heightOffset - target) < 1f) {
            heightOffset = target
            return
        }
        snapTo(target)
    }

    private fun snapTo(targetOffset: Float) {
        snapJob?.cancel()
        snapJob = coroutineScope.launch {
            val anim = Animatable(heightOffset)
            anim.animateTo(
                targetValue = targetOffset.coerceIn(-maxHeightPx, 0f),
                animationSpec = ExpressiveMotion.spatialDefault(reducedMotion = reducedMotion)
            ) {
                heightOffset = value
            }
        }
    }
}

@Composable
fun rememberCollapsibleHeaderState(
    reducedMotion: Boolean = false
): CollapsibleHeaderState {
    val scope = rememberCoroutineScope()
    return remember(scope, reducedMotion) {
        CollapsibleHeaderState(
            coroutineScope = scope,
            reducedMotion = reducedMotion
        )
    }
}

@Composable
fun rememberCollapsibleHeaderNestedScrollConnection(
    state: CollapsibleHeaderState,
    canCollapseProvider: () -> Boolean
): NestedScrollConnection {
    return remember(state, canCollapseProvider) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy == 0f || state.maxHeightPx <= 1f) return Offset.Zero

                return when {
                    // Dragging finger UP (scrolling content down) -> slide top bar up smoothly
                    dy < 0f && canCollapseProvider() -> {
                        val consumedY = state.applyScrollDelta(dy)
                        Offset(0f, consumedY)
                    }
                    // Dragging finger DOWN (scrolling content up) -> immediately bring top bar back
                    dy > 0f && state.heightOffset < 0f -> {
                        val consumedY = state.applyScrollDelta(dy)
                        Offset(0f, consumedY)
                    }
                    else -> Offset.Zero
                }
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (state.heightOffset < 0f && state.heightOffset > -state.maxHeightPx) {
                    state.snapAfterScroll(velocityY = available.y)
                }
                return Velocity.Zero
            }
        }
    }
}

/**
 * Dynamic height-collapsing layout modifier driven by [CollapsibleHeaderState].
 * Slides the header smoothly upward out of bounds while shrinking its occupied height 1:1
 * so scrolling content expands seamlessly to fill the screen.
 */
fun Modifier.collapsibleHeaderLayout(
    state: CollapsibleHeaderState,
    applyParallaxFade: Boolean = true
): Modifier = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
        state.updateMaxHeight(placeable.height.toFloat())
        val offset = state.heightOffset.roundToInt()
        val visibleHeight = (placeable.height + offset).coerceIn(0, placeable.height)
        layout(placeable.width, visibleHeight) {
            placeable.placeRelative(0, offset)
        }
    }
    .graphicsLayer {
        if (applyParallaxFade) {
            val fraction = state.collapsedFraction
            alpha = (1f - fraction * 0.9f).coerceIn(0f, 1f)
            val subtleScale = 1f - fraction * 0.03f
            scaleX = subtleScale
            scaleY = subtleScale
        }
    }

/**
 * Computes Material 3 Expressive integer offset for placement animations.
 */
val ExpressivePlacementSpec: FiniteAnimationSpec<IntOffset> = spring(
    dampingRatio = 0.76f,
    stiffness = 420f
)
