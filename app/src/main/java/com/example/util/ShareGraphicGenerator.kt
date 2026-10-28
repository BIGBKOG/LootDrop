package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.LootDropApplication
import com.example.data.model.GiveawayItem
import com.example.ui.calendar.ExpirationCalendarUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Shareable Graphic Generator
 *
 * Renders high-resolution (1200x675, 16:9) gamer share cards using Android Canvas and [Picture.draw].
 * Features:
 * - High-res game banner art with cinematic gradient transitions.
 * - Strikethrough original price and bold "100% OFF / FREE" badge.
 * - Store tags (Steam, Epic, PlayStation, Xbox, GOG, etc.).
 * - Expiration date badge.
 * - High-contrast LootDrop watermark branding.
 * - Temporary FileProvider cache export with 1-tap ACTION_SEND image/png intent.
 */
object ShareGraphicGenerator {

    private const val TAG = "ShareGraphicGenerator"
    const val CARD_WIDTH = 1200
    const val CARD_HEIGHT = 675
    private const val BANNER_HEIGHT = 420

    /**
     * Converts a Compose [ImageBitmap] to an Android [Bitmap] using [asAndroidBitmap].
     */
    fun toAndroidBitmap(imageBitmap: ImageBitmap): Bitmap {
        return imageBitmap.asAndroidBitmap()
    }

    /**
     * Converts an Android [Bitmap] to a Compose [ImageBitmap] using [asImageBitmap].
     */
    fun toComposeImageBitmap(bitmap: Bitmap): ImageBitmap {
        return bitmap.asImageBitmap()
    }

    /**
     * Generates a crisp 1200x675 bitmap card for the provided [item].
     * Uses [Picture] recording and [Picture.draw] onto an ARGB_8888 bitmap.
     */
    suspend fun generateCardBitmap(context: Context, item: GiveawayItem): Bitmap = withContext(Dispatchers.IO) {
        // 1. Pre-load banner bitmap via Coil
        val bannerBitmap = fetchBannerBitmap(context, item)

        // 2. Begin recording with Android Picture
        val picture = Picture()
        val canvas = picture.beginRecording(CARD_WIDTH, CARD_HEIGHT)

        // 3. Render all card components onto the canvas
        drawCard(canvas, item, bannerBitmap)

        // 4. End recording
        picture.endRecording()

        // 5. Draw the picture onto an Android Bitmap using Picture.draw()
        val outputBitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(outputBitmap)
        picture.draw(bitmapCanvas)

        outputBitmap
    }

    /**
     * Saves the given bitmap into the app's FileProvider cache directory and returns the File.
     */
    suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap, giveawayId: Int): File = withContext(Dispatchers.IO) {
        val shareDir = File(context.cacheDir, "share_cards").apply {
            if (!exists()) mkdirs()
        }

        // Clean up cached share cards older than 24 hours to keep disk footprint minimal
        cleanOldShareCards(shareDir)

        val file = File(shareDir, "lootdrop_freebie_${giveawayId}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    }

    /**
     * Obtains the FileProvider content [Uri] for an image file.
     */
    fun getShareableUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * 1-Tap Instant Share Intent:
     * Generates the bitmap card, caches it to FileProvider, and triggers [Intent.ACTION_SEND]
     * with MIME type "image/png" to easily post to Instagram Stories, Discord channels, Reddit, or SMS.
     */
    suspend fun shareGraphicCard(
        context: Context,
        item: GiveawayItem,
        includeTextLink: Boolean = true
    ) {
        val bitmap = generateCardBitmap(context, item)
        val file = saveBitmapToCache(context, bitmap, item.id)
        val contentUri = getShareableUri(context, file)

        withContext(Dispatchers.Main) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Free Game: ${item.title} (100% OFF)")

                if (includeTextLink) {
                    val worthInfo = if (item.worth.isNotBlank() && item.worth != "N/A") " (Orig. ${item.worth})" else ""
                    val expBadge = ExpirationCalendarUtils.getExpirationBadgeText(item.endDate)
                    val expInfo = if (expBadge.isNotBlank()) "\n⏳ Expiration: $expBadge" else ""

                    val shareText = "🔥 FREE GAME ALERT: ${item.title}$worthInfo 100% FREE on ${item.platforms}!$expInfo\n" +
                            "👉 Claim here: ${item.openGiveawayUrl}\n\n" +
                            "Tracked with LootDrop Free Games Radar (lootdrop.app)"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Free Game Card").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            context.startActivity(chooser)
        }
    }

    // =========================================================================
    // Private Drawing & Resource Helpers
    // =========================================================================

    private suspend fun fetchBannerBitmap(context: Context, item: GiveawayItem): Bitmap? {
        val imageUrl = item.image.ifBlank { item.thumbnail }
        if (imageUrl.isBlank()) return null

        return try {
            val imageLoader = (context.applicationContext as? LootDropApplication)?.newImageLoader()
                ?: ImageLoader(context)

            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Must be software bitmap for canvas operations & testability
                .build()

            val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
            (result as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load banner art for ${item.title}: ${e.message}")
            null
        }
    }

    private fun cleanOldShareCards(dir: File) {
        val threshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000L) // 24 hours
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < threshold) {
                file.delete()
            }
        }
    }

    private fun drawCard(canvas: Canvas, item: GiveawayItem, bannerBitmap: Bitmap?) {
        val cardRect = RectF(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat())

        // 1. Base Dark Canvas Background (#090D16 -> #020617)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(),
                Color.parseColor("#090D16"),
                Color.parseColor("#020617"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(cardRect, bgPaint)

        // 2. Banner Art (Upper 420px)
        val bannerRect = RectF(0f, 0f, CARD_WIDTH.toFloat(), BANNER_HEIGHT.toFloat())
        if (bannerBitmap != null) {
            drawScaledBanner(canvas, bannerBitmap, bannerRect)
        } else {
            drawBannerFallback(canvas, bannerRect, item.title)
        }

        // 3. Dark Gradient Overlay over the banner (smooth cinematic fade to card background)
        val fadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 160f, 0f, BANNER_HEIGHT.toFloat() + 20f,
                intArrayOf(
                    Color.TRANSPARENT,
                    Color.parseColor("#66090D16"),
                    Color.parseColor("#CC090D16"),
                    Color.parseColor("#FF090D16")
                ),
                floatArrayOf(0f, 0.45f, 0.8f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(RectF(0f, 160f, CARD_WIDTH.toFloat(), BANNER_HEIGHT.toFloat() + 20f), fadePaint)

        // 4. Badges on Banner: Top Left "100% OFF" + "FREE TO KEEP"
        drawTopBadges(canvas, item)

        // 5. Expiration Badge: Top Right
        drawExpirationBadge(canvas, item)

        // 6. Pricing Row: Strikethrough Original Price + FREE Highlight
        drawPricingRow(canvas, item)

        // 7. Game Title (Bold, Multi-line with Drop Shadow)
        val titleBottomY = drawGameTitle(canvas, item.title)

        // 8. Store / Platform Tags (Steam, Epic, PlayStation, Xbox, GOG, etc.)
        drawStoreTags(canvas, item, titleBottomY)

        // 9. LootDrop Watermark Branding in the Corner
        drawLootDropWatermark(canvas)

        // 10. Sleek Neon Accent Border
        drawOuterBorder(canvas, cardRect)
    }

    private fun drawScaledBanner(canvas: Canvas, bitmap: Bitmap, destRect: RectF) {
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()

        // Center-crop calculation
        val scale = maxOf(destRect.width() / srcWidth, destRect.height() / srcHeight)
        val scaledWidth = srcWidth * scale
        val scaledHeight = srcHeight * scale
        val left = destRect.left + (destRect.width() - scaledWidth) / 2f
        val top = destRect.top + (destRect.height() - scaledHeight) / 2f

        canvas.save()
        canvas.clipRect(destRect)
        val dst = RectF(left, top, left + scaledWidth, top + scaledHeight)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(bitmap, null, dst, paint)
        canvas.restore()
    }

    private fun drawBannerFallback(canvas: Canvas, destRect: RectF, title: String) {
        val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                destRect.left, destRect.top, destRect.right, destRect.bottom,
                Color.parseColor("#1E1B4B"), // Deep indigo
                Color.parseColor("#0F172A"), // Slate dark
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(destRect, fallbackPaint)

        // Subtle decorative controller glyph / radar line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#20FFFFFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, 0f, destRect.right, destRect.bottom, linePaint)
        canvas.drawLine(0f, destRect.bottom, destRect.right, 0f, linePaint)
    }

    private fun drawTopBadges(canvas: Canvas, item: GiveawayItem) {
        var startX = 36f
        val topY = 32f
        val badgeHeight = 44f

        // 100% OFF Pill (Vibrant Neon Green)
        val offBadgeText = "100% OFF"
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f
            color = Color.parseColor("#022C14") // Dark emerald
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textWidth = textPaint.measureText(offBadgeText)
        val badgeWidth = textWidth + 34f
        val offRect = RectF(startX, topY, startX + badgeWidth, topY + badgeHeight)

        val offBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E676") // Vibrant Neon Green
        }
        canvas.drawRoundRect(offRect, 12f, 12f, offBgPaint)
        canvas.drawText(offBadgeText, startX + 17f, topY + 29f, textPaint)

        startX += badgeWidth + 14f

        // "FREE TO KEEP" Badge (Cyan Accent)
        val keepText = "FREE TO KEEP"
        val keepTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 19f
            color = Color.parseColor("#00E5FF")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val keepWidth = keepTextPaint.measureText(keepText) + 30f
        val keepRect = RectF(startX, topY, startX + keepWidth, topY + badgeHeight)

        val keepBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CC0B132B")
        }
        val keepBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(keepRect, 12f, 12f, keepBgPaint)
        canvas.drawRoundRect(keepRect, 12f, 12f, keepBorderPaint)
        canvas.drawText(keepText, startX + 15f, topY + 29f, keepTextPaint)
    }

    private fun drawExpirationBadge(canvas: Canvas, item: GiveawayItem) {
        val rawBadge = ExpirationCalendarUtils.getExpirationBadgeText(item.endDate)
        val badgeText = if (rawBadge.isNotBlank() && rawBadge != "N/A") {
            "⏳ $rawBadge"
        } else if (item.endDate.isNotBlank() && item.endDate != "N/A") {
            "⏳ Ends ${item.endDate.split(" ").firstOrNull() ?: item.endDate}"
        } else {
            "⏳ Limited Time"
        }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            color = Color.parseColor("#FDE68A") // Warm Amber
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textWidth = textPaint.measureText(badgeText)
        val badgeWidth = textWidth + 34f
        val rightX = CARD_WIDTH - 36f
        val leftX = rightX - badgeWidth
        val topY = 32f
        val badgeHeight = 44f
        val expRect = RectF(leftX, topY, rightX, topY + badgeHeight)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9271602") // Dark amber
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(expRect, 12f, 12f, bgPaint)
        canvas.drawRoundRect(expRect, 12f, 12f, borderPaint)
        canvas.drawText(badgeText, leftX + 17f, topY + 29f, textPaint)
    }

    private fun drawPricingRow(canvas: Canvas, item: GiveawayItem) {
        val startX = 36f
        val baselineY = 405f

        val hasWorth = item.worth.isNotBlank() && item.worth != "N/A" && item.worth != "$0.00"
        val origWorth = if (hasWorth) item.worth else "$19.99"

        // 1. Original Price (Muted Gray with Strike-Through)
        val strikePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 28f
            color = Color.parseColor("#EF4444") // Coral red
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val worthWidth = strikePaint.measureText(origWorth)
        canvas.drawText(origWorth, startX, baselineY, strikePaint)

        // Draw visible red strikethrough line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EF4444")
            strokeWidth = 3.5f
        }
        canvas.drawLine(startX - 2f, baselineY - 10f, startX + worthWidth + 4f, baselineY - 10f, linePaint)

        // 2. Arrow Divider
        var currentX = startX + worthWidth + 16f
        val arrowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
            color = Color.parseColor("#94A3B8")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("➔", currentX, baselineY, arrowPaint)
        currentX += arrowPaint.measureText("➔") + 16f

        // 3. FREE Highlight
        val freePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 34f
            color = Color.parseColor("#00E676") // Neon green
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("FREE", currentX, baselineY, freePaint)
        currentX += freePaint.measureText("FREE") + 14f

        // 4. Savings badge pill: "(Save 100%)"
        val pillText = "(100% OFF)"
        val pillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 19f
            color = Color.parseColor("#A7F3D0")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(pillText, currentX, baselineY - 3f, pillPaint)
    }

    private fun drawGameTitle(canvas: Canvas, title: String): Float {
        val startX = 36f
        val topY = 422f
        val maxTextWidth = 1120

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 42f
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 2f, Color.parseColor("#CC000000"))
        }

        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(title, 0, title.length, titlePaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.15f)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(title, titlePaint, maxTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.15f, 0f, false)
        }

        canvas.save()
        canvas.translate(startX, topY)
        staticLayout.draw(canvas)
        canvas.restore()

        return topY + staticLayout.height
    }

    private data class StoreTag(
        val name: String,
        val bgColor: Int,
        val textColor: Int,
        val borderColor: Int
    )

    private fun drawStoreTags(canvas: Canvas, item: GiveawayItem, titleBottomY: Float) {
        val startY = maxOf(titleBottomY + 14f, 545f)
        var currentX = 36f
        val tagHeight = 36f

        // Extract Store Tags matching user requirements (Steam, Epic, PlayStation, Xbox, GOG, etc.)
        val tags = mutableListOf<StoreTag>()

        if (item.isSteam) {
            tags.add(StoreTag("STEAM", Color.parseColor("#1B2838"), Color.parseColor("#66C0F4"), Color.parseColor("#2A475E")))
        }
        if (item.isEpic) {
            tags.add(StoreTag("EPIC GAMES", Color.parseColor("#1F2937"), Color.parseColor("#38BDF8"), Color.parseColor("#0284C7")))
        }
        if (item.isPlayStation) {
            tags.add(StoreTag("PLAYSTATION", Color.parseColor("#003791"), Color.parseColor("#93C5FD"), Color.parseColor("#1D4ED8")))
        }
        if (item.isXbox) {
            tags.add(StoreTag("XBOX", Color.parseColor("#107C10"), Color.parseColor("#86EFAC"), Color.parseColor("#16A34A")))
        }
        if (item.isGog) {
            tags.add(StoreTag("GOG", Color.parseColor("#4A044E"), Color.parseColor("#E879F9"), Color.parseColor("#A21CAF")))
        }
        if (item.isNintendo) {
            tags.add(StoreTag("NINTENDO", Color.parseColor("#7F1D1D"), Color.parseColor("#FCA5A5"), Color.parseColor("#DC2626")))
        }
        if (item.platforms.lowercase().contains("ubisoft") || item.platforms.lowercase().contains("uplay")) {
            tags.add(StoreTag("UBISOFT", Color.parseColor("#075985"), Color.parseColor("#7DD3FC"), Color.parseColor("#0284C7")))
        }
        if (item.platforms.lowercase().contains("prime") || item.platforms.lowercase().contains("amazon")) {
            tags.add(StoreTag("PRIME GAMING", Color.parseColor("#78350F"), Color.parseColor("#FCD34D"), Color.parseColor("#D97706")))
        }

        // Fallback generic tag if none resolved
        if (tags.isEmpty()) {
            val fallbackLabel = item.platforms.split(",").firstOrNull()?.trim()?.uppercase(Locale.ROOT) ?: "PC / CONSOLE"
            tags.add(StoreTag(fallbackLabel, Color.parseColor("#1E293B"), Color.parseColor("#CBD5E1"), Color.parseColor("#475569")))
        }

        // Always add Giveaway Type badge (Game / DLC / Loot)
        val typeLabel = item.type.ifBlank { "GAME" }.uppercase(Locale.ROOT)
        tags.add(StoreTag(typeLabel, Color.parseColor("#0F172A"), Color.parseColor("#00E5FF"), Color.parseColor("#00E5FF")))

        val tagTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Render tags up to watermark boundary
        val maxRightX = CARD_WIDTH - 360f

        for (tag in tags) {
            tagTextPaint.color = tag.textColor
            val textWidth = tagTextPaint.measureText(tag.name)
            val tagWidth = textWidth + 24f

            if (currentX + tagWidth > maxRightX) break

            val tagRect = RectF(currentX, startY, currentX + tagWidth, startY + tagHeight)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tag.bgColor
            }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tag.borderColor
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }

            canvas.drawRoundRect(tagRect, 8f, 8f, bgPaint)
            canvas.drawRoundRect(tagRect, 8f, 8f, borderPaint)
            canvas.drawText(tag.name, currentX + 12f, startY + 24f, tagTextPaint)

            currentX += tagWidth + 10f
        }
    }

    private fun drawLootDropWatermark(canvas: Canvas) {
        val rightX = CARD_WIDTH - 36f
        val startY = 540f

        // Brand Name "LootDrop" in Cyan & White
        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 28f
            color = Color.parseColor("#00E5FF") // Vibrant Cyan
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(6f, 0f, 2f, Color.parseColor("#8000E5FF"))
        }
        val brandText = "⚡ LootDrop"
        val brandWidth = brandPaint.measureText(brandText)
        val brandX = rightX - brandWidth
        canvas.drawText(brandText, brandX, startY + 26f, brandPaint)

        // Subtitle "Free Games Radar"
        val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            color = Color.parseColor("#94A3B8") // Slate muted
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val subText = "Free Games Radar • lootdrop.app"
        val subWidth = subPaint.measureText(subText)
        val subX = rightX - subWidth
        canvas.drawText(subText, subX, startY + 52f, subPaint)

        // Small decorative indicator dot
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E676")
        }
        canvas.drawCircle(subX - 10f, startY + 47f, 4f, dotPaint)
    }

    private fun drawOuterBorder(canvas: Canvas, rect: RectF) {
        // Multi-color gradient gamer border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(
                    Color.parseColor("#00E5FF"), // Cyan
                    Color.parseColor("#10B981"), // Emerald
                    Color.parseColor("#8B5CF6"), // Purple
                    Color.parseColor("#00E5FF")  // Cyan
                ),
                floatArrayOf(0f, 0.35f, 0.7f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        val insetRect = RectF(rect.left + 2f, rect.top + 2f, rect.right - 2f, rect.bottom - 2f)
        canvas.drawRoundRect(insetRect, 18f, 18f, borderPaint)
    }
}
