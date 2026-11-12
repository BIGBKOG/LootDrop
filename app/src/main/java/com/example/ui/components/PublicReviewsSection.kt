package com.example.ui.components

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.model.GiveawayItem
import com.example.data.model.PublicReview
import com.example.ui.theme.GamingCardBorder
import com.example.ui.theme.GamingDarkBackground
import com.example.ui.theme.GamingDarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

private val StarGold = Color(0xFFFFB800)

@Composable
fun PublicReviewsSection(
    item: GiveawayItem,
    reviews: List<PublicReview>,
    defaultAuthorName: String,
    onPostReview: (rating: Int, title: String, comment: String) -> Unit,
    onVoteHelpful: (reviewId: String) -> Unit,
    hasVotedHelpful: (reviewId: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var isWriteDialogOpen by remember { mutableStateOf(false) }

    val avgRating = remember(reviews) {
        if (reviews.isEmpty()) 5.0 else reviews.map { it.rating }.average()
    }
    val reviewCount = reviews.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("public_reviews_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(StarGold)
                )
                Text(
                    text = "PUBLIC COMMUNITY REVIEWS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StarGold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF20293A))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Cloudflare R2",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }

        // Overview & Score Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF131B2B),
                            GamingDarkSurfaceVariant
                        )
                    )
                )
                .border(1.2.dp, StarGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(14.dp)
                .testTag("reviews_overview_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", avgRating),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = StarGold
                        )
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                val roundedScore = avgRating.toInt().coerceIn(1, 5)
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= roundedScore) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (i <= roundedScore) StarGold else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (reviewCount == 1) "1 review from players" else "$reviewCount reviews from players",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Write Review Button
                    val writeInteraction = remember { MutableInteractionSource() }
                    val isWritePressed by writeInteraction.collectIsPressedAsState()
                    val writeScale by animateFloatAsState(
                        targetValue = if (isWritePressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "write_review_scale"
                    )

                    Button(
                        onClick = { isWriteDialogOpen = true },
                        interactionSource = writeInteraction,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = writeScale
                                scaleY = writeScale
                            }
                            .testTag("write_review_cta_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarGold,
                            contentColor = Color(0xFF0F172A)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Leave Review",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Reviews List
        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1522))
                    .border(1.dp, GamingCardBorder, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎮 Be the first to leave a review!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Share your rating and thoughts with the community. Reviews sync publicly to our R2 cloud.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { isWriteDialogOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StarGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StarGold.copy(alpha = 0.5f))
                    ) {
                        Text("Write First Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reviews.forEach { review ->
                    ReviewCard(
                        review = review,
                        hasVoted = hasVotedHelpful(review.id),
                        onVoteHelpful = { onVoteHelpful(review.id) }
                    )
                }
            }
        }
    }

    if (isWriteDialogOpen) {
        WriteReviewDialog(
            gameTitle = item.title,
            defaultAuthorName = defaultAuthorName,
            initialIsAppReview = false,
            onDismiss = { isWriteDialogOpen = false },
            onSubmit = { rating, title, comment, _ ->
                onPostReview(rating, title, comment)
                isWriteDialogOpen = false
            }
        )
    }
}

@Composable
fun ReviewCard(
    review: PublicReview,
    hasVoted: Boolean,
    onVoteHelpful: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (review.isForApp) Color(0xFF141A29) else Color(0xFF0F1522))
            .border(
                1.dp,
                if (review.isForApp) StarGold.copy(alpha = 0.45f) else GamingCardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
            .testTag("review_card_${review.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Author, Badge, Platform, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val initial = review.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "G"
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (review.isForApp) {
                                    Brush.linearGradient(listOf(StarGold, Color(0xFFEA580C)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = review.authorName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (review.isForApp) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StarGold.copy(alpha = 0.18f))
                                        .border(0.8.dp, StarGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "⭐ APP REVIEW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StarGold
                                    )
                                }
                            } else if (review.isVerifiedClaimer) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Claimer",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        val timeAgo = remember(review.timestamp) {
                            try {
                                DateUtils.getRelativeTimeSpanString(
                                    review.timestamp,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_RELATIVE
                                ).toString()
                            } catch (_: Exception) {
                                "Recently"
                            }
                        }

                        Text(
                            text = if (review.isForApp) "LootDrop App • $timeAgo" else "${review.platform} • $timeAgo",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // Star rating badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) StarGold else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Review Title
            if (review.title.isNotBlank()) {
                Text(
                    text = review.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Review Body Comment
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 18.sp
                )
            }

            // Helpful vote footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (hasVoted) NeonCyan.copy(alpha = 0.15f) else Color(0xFF1E293B))
                        .border(
                            1.dp,
                            if (hasVoted) NeonCyan.copy(alpha = 0.5f) else Color(0xFF334155),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(enabled = !hasVoted) { onVoteHelpful() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("helpful_btn_${review.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Helpful",
                        tint = if (hasVoted) NeonCyan else TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Helpful (${review.helpfulCount})",
                        fontSize = 11.sp,
                        fontWeight = if (hasVoted) FontWeight.Bold else FontWeight.Medium,
                        color = if (hasVoted) NeonCyan else TextSecondary
                    )
                }

                if (review.authorTag.isNotBlank() && review.authorTag != "Gamer") {
                    Text(
                        text = "🏷️ ${review.authorTag}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

/**
 * Dedicated LootDrop App Rating Card for Settings & Home Screen.
 */
@Composable
fun LootDropAppRatingCard(
    reviews: List<PublicReview>,
    defaultAuthorName: String,
    onPostAppReview: (rating: Int, title: String, comment: String) -> Unit,
    onVoteHelpful: (reviewId: String) -> Unit,
    hasVotedHelpful: (reviewId: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var isWriteDialogOpen by remember { mutableStateOf(false) }
    var isBrowseDialogOpen by remember { mutableStateOf(false) }

    val appReviews = remember(reviews) { reviews.filter { it.isForApp } }
    val avgAppRating = remember(appReviews) {
        if (appReviews.isEmpty()) 5.0 else appReviews.map { it.rating }.average()
    }
    val count = appReviews.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lootdrop_app_rating_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, StarGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(StarGold, Color(0xFFF97316))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LOOTDROP APP RATING",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StarGold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Community Reviews on Cloudflare R2",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF20293A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "V${BuildConfig.VERSION_NAME}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            // Big Score Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1522))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", avgAppRating),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = StarGold
                    )
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            val roundedScore = avgAppRating.toInt().coerceIn(1, 5)
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= roundedScore) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= roundedScore) StarGold else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "$count player ${if (count == 1) "rating" else "ratings"} & reviews",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "🏆 Masterpiece",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { isWriteDialogOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rate_lootdrop_app_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarGold,
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rate LootDrop", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { isBrowseDialogOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("browse_community_reviews_btn"),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("All Reviews (${reviews.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (isWriteDialogOpen) {
        WriteReviewDialog(
            gameTitle = "LootDrop App",
            defaultAuthorName = defaultAuthorName,
            initialIsAppReview = true,
            onDismiss = { isWriteDialogOpen = false },
            onSubmit = { rating, title, comment, _ ->
                onPostAppReview(rating, title, comment)
                isWriteDialogOpen = false
            }
        )
    }

    if (isBrowseDialogOpen) {
        AllCommunityReviewsDialog(
            reviews = reviews,
            defaultAuthorName = defaultAuthorName,
            onDismiss = { isBrowseDialogOpen = false },
            onPostReview = { rating, title, comment, isAppReview ->
                if (isAppReview) {
                    onPostAppReview(rating, title, comment)
                } else {
                    onPostAppReview(rating, title, comment)
                }
            },
            onVoteHelpful = onVoteHelpful,
            hasVotedHelpful = hasVotedHelpful
        )
    }
}

@Composable
fun WriteReviewDialog(
    gameTitle: String,
    defaultAuthorName: String,
    initialIsAppReview: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, title: String, comment: String, isAppReview: Boolean) -> Unit
) {
    var isAppReview by remember { mutableStateOf(initialIsAppReview || gameTitle.contains("LootDrop", ignoreCase = true)) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewTitle by remember { mutableStateOf("") }
    var reviewComment by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf(defaultAuthorName) }
    var isSubmitting by remember { mutableStateOf(false) }

    val ratingLabels = listOf(
        "1 - Skip / Unplayable",
        "2 - Mediocre / Flawed",
        "3 - Average / Fun with Friends",
        "4 - Great / Highly Recommended",
        "5 - Masterpiece / Must Play!"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(16.dp)
                .testTag("write_review_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101726)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, StarGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAppReview) "Rate LootDrop App" else "Leave a Public Review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isAppReview) "LootDrop V${BuildConfig.VERSION_NAME} • Free Game Tracker" else gameTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAppReview) StarGold else NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                // Review Target Switcher (Only if opened from game detail)
                if (gameTitle.isNotBlank() && !gameTitle.contains("LootDrop", ignoreCase = true)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F1522))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isAppReview) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isAppReview = false }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎮 Game Review",
                                fontSize = 11.sp,
                                fontWeight = if (!isAppReview) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isAppReview) NeonCyan else TextMuted
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAppReview) StarGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isAppReview = true }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⭐ Rate LootDrop",
                                fontSize = 11.sp,
                                fontWeight = if (isAppReview) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAppReview) StarGold else TextMuted
                            )
                        }
                    }
                }

                // Interactive Star Rating Picker
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            val isSelected = i <= rating
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i Stars",
                                tint = if (isSelected) StarGold else TextMuted,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { rating = i }
                                    .testTag("star_picker_$i")
                            )
                        }
                    }

                    Text(
                        text = ratingLabels[(rating - 1).coerceIn(0, 4)],
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarGold
                    )
                }

                // Review Title Field
                OutlinedTextField(
                    value = reviewTitle,
                    onValueChange = { reviewTitle = it },
                    label = {
                        Text(
                            text = if (isAppReview) "Review Headline (e.g. Best game tracker ever!)" else "Review Headline (e.g. Incredible story & visuals)",
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_title_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StarGold,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = StarGold,
                        unfocusedLabelColor = TextMuted
                    )
                )

                // Review Comment Field
                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it },
                    label = {
                        Text(
                            text = if (isAppReview) "What do you love about LootDrop? (Alerts, themes, backups...)" else "Your thoughts on gameplay, performance, story...",
                            fontSize = 12.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_comment_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StarGold,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = StarGold,
                        unfocusedLabelColor = TextMuted
                    )
                )

                // Gamer Tag / Author Name Field
                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it },
                    label = { Text("Display Gamer Tag", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_author_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = NeonCyan,
                        unfocusedLabelColor = TextMuted
                    )
                )

                Text(
                    text = "☁️ Reviews are uploaded publicly to our Cloudflare R2 bucket and visible to all gamers on Android & Web.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 14.sp
                )

                // Submit Button
                Button(
                    onClick = {
                        isSubmitting = true
                        onSubmit(rating, reviewTitle, reviewComment, isAppReview)
                    },
                    enabled = !isSubmitting && (reviewTitle.isNotBlank() || reviewComment.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_review_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarGold,
                        contentColor = Color(0xFF0F172A),
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = TextMuted
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF0F172A),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isAppReview) "🚀 Post LootDrop App Rating" else "🚀 Post Review to Community",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full Community Reviews Dialog for browsing all game and app reviews.
 */
@Composable
fun AllCommunityReviewsDialog(
    reviews: List<PublicReview>,
    defaultAuthorName: String,
    onDismiss: () -> Unit,
    onPostReview: (rating: Int, title: String, comment: String, isAppReview: Boolean) -> Unit,
    onVoteHelpful: (reviewId: String) -> Unit,
    hasVotedHelpful: (reviewId: String) -> Boolean
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var isWriteDialogOpen by remember { mutableStateOf(false) }

    val filteredReviews = remember(reviews, selectedFilter) {
        when (selectedFilter) {
            "APP" -> reviews.filter { it.isForApp }
            "GAMES" -> reviews.filter { !it.isForApp }
            else -> reviews
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(640.dp)
                .padding(12.dp)
                .testTag("all_community_reviews_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0F19)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, StarGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StarGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⭐", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "COMMUNITY REVIEWS",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Synced via Cloudflare R2",
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Filter Chips & Write Review CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ALL" to "All (${reviews.size})",
                            "APP" to "⭐ App (${reviews.count { it.isForApp }})",
                            "GAMES" to "🎮 Games (${reviews.count { !it.isForApp }})"
                        ).forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) StarGold else Color(0xFF1E293B))
                                    .clickable { selectedFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F172A) else TextPrimary
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { isWriteDialogOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StarGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StarGold.copy(alpha = 0.6f)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("✍️ Rate App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Reviews List
                if (filteredReviews.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reviews found in this category yet.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredReviews, key = { it.id }) { review ->
                            ReviewCard(
                                review = review,
                                hasVoted = hasVotedHelpful(review.id),
                                onVoteHelpful = { onVoteHelpful(review.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isWriteDialogOpen) {
        WriteReviewDialog(
            gameTitle = "LootDrop App",
            defaultAuthorName = defaultAuthorName,
            initialIsAppReview = true,
            onDismiss = { isWriteDialogOpen = false },
            onSubmit = { rating, title, comment, isApp ->
                onPostReview(rating, title, comment, isApp)
                isWriteDialogOpen = false
            }
        )
    }
}
