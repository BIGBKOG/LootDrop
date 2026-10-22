package com.example.ui.components

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

/**
 * ShaderBrush that rotates a SweepGradient around the center of the bounding box.
 * Produces a focused bright hotspot with a luminous fading trail that travels around
 * the card's rounded perimeter.
 */
class RotatingBorderBeamBrush(
    private val baseColorArgb: Int,
    private val accentColorArgb: Int,
    private val coreColorArgb: Int,
    private val tailColorArgb: Int,
    private val softTailArgb: Int,
    private val angle: Float
) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val colors = intArrayOf(
            baseColorArgb,
            accentColorArgb,
            coreColorArgb,
            accentColorArgb,
            tailColorArgb,
            softTailArgb,
            baseColorArgb,
            baseColorArgb
        )
        val positions = floatArrayOf(
            0.00f,
            0.03f,
            0.07f,
            0.12f,
            0.20f,
            0.30f,
            0.38f,
            1.00f
        )
        val shader = SweepGradient(cx, cy, colors, positions)
        val matrix = Matrix()
        matrix.postRotate(angle, cx, cy)
        shader.setLocalMatrix(matrix)
        return shader
    }
}

/**
 * Draws an expressive animated border beam with a bright spot that travels smoothly
 * around the perimeter of the card, highlighting the card's active theme.
 *
 * Uses drawWithContent so that the angle calculation happens during the draw phase,
 * avoiding recomposition of the card's interior composables.
 */
fun Modifier.borderBeam(
    strokeWidth: Dp,
    cornerRadius: Dp,
    borderColor: Color,
    beamAngleProvider: () -> Float,
    isAnimated: Boolean = true
): Modifier = this.drawWithContent {
    // 1. Draw card background, covers, badges & text first
    drawContent()

    // 2. Draw outer border with traveling bright hotspot
    val strokePx = strokeWidth.toPx()
    val halfStroke = strokePx / 2f
    val cornerPx = cornerRadius.toPx()
    val adjustedRadius = CornerRadius((cornerPx - halfStroke).coerceAtLeast(0f))

    val brush = if (isAnimated) {
        val angle = beamAngleProvider()
        val baseColor = borderColor.copy(alpha = 0.28f)
        val accentColor = borderColor.copy(alpha = 0.95f)
        val coreColor = Color.White
        val tailColor = borderColor.copy(alpha = 0.70f)
        val softTail = borderColor.copy(alpha = 0.40f)

        RotatingBorderBeamBrush(
            baseColorArgb = baseColor.toArgb(),
            accentColorArgb = accentColor.toArgb(),
            coreColorArgb = coreColor.toArgb(),
            tailColorArgb = tailColor.toArgb(),
            softTailArgb = softTail.toArgb(),
            angle = angle
        )
    } else {
        SolidColor(borderColor)
    }

    drawRoundRect(
        brush = brush,
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(
            width = (size.width - strokePx).coerceAtLeast(0f),
            height = (size.height - strokePx).coerceAtLeast(0f)
        ),
        cornerRadius = adjustedRadius,
        style = Stroke(width = strokePx)
    )
}
