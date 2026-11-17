package com.example.ui.components

import android.text.format.DateUtils
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.BuildConfig
import com.example.data.model.GiveawayItem
import com.example.data.model.PublicReview
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.theme.NeonGreen
import com.example.util.ReviewModerationFilter
import java.util.Locale

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
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    var isWriteDialogOpen by remember { mutableStateOf(false) }
    val avgRating = remember(reviews) { if (reviews.isEmpty()) 5.0 else reviews.map { it.rating }.average() }
    val reviewCount = reviews.size

    Column(
        modifier = modifier.fillMaxWidth().testTag("public_reviews_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ReviewStarGold))
                Text(s.dialogs.reviewsCommunityTitle, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = ReviewStarGold, letterSpacing = 1.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("Cloudflare R2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .background(Brush.verticalGradient(listOf(ReviewStarGold.copy(alpha = 0.08f), MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.65f))))
                .border(1.dp, ReviewStarGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(16.dp)
                .testTag("reviews_overview_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(String.format(Locale.US, "%.1f", avgRating), fontSize = 28.sp, fontWeight = FontWeight.Black, color = ReviewStarGold)
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            val roundedScore = avgRating.toInt().coerceIn(1, 5)
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= roundedScore) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= roundedScore) ReviewStarGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isEs) {
                                if (reviewCount == 1) "1 reseña de jugadores" else "$reviewCount reseñas de jugadores"
                            } else {
                                if (reviewCount == 1) "1 review from players" else "$reviewCount reviews from players"
                            },
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

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
                    modifier = Modifier.graphicsLayer { scaleX = writeScale; scaleY = writeScale }.testTag("write_review_cta_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReviewStarGold, contentColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(s.dialogs.reviewsWriteReview, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.dialogs.reviewsNoReviewsYet, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    Text(
                        text = if (isEs) "Comparte tu puntuación y opinión con la comunidad. Se sincronizan públicamente en la nube R2." else "Share your rating and thoughts with the community. Reviews sync publicly to our R2 cloud.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { isWriteDialogOpen = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ReviewStarGold),
                        border = BorderStroke(1.dp, ReviewStarGold.copy(alpha = 0.5f))
                    ) {
                        Text(s.dialogs.reviewsWriteReview, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reviews.forEach { review ->
                    ReviewCard(review = review, hasVoted = hasVotedHelpful(review.id), onVoteHelpful = { onVoteHelpful(review.id) })
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
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (review.isForApp) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, if (review.isForApp) ReviewStarGold.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("review_card_${review.id}")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val displayAuthorName = remember(review.id, review.authorName) { ReviewModerationFilter.sanitizeForDisplay(review.authorName) }
                    val initial = displayAuthorName.firstOrNull()?.uppercaseChar()?.toString() ?: "G"
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (review.isForApp) listOf(ReviewStarGold, Color(0xFFEA580C))
                                    else listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = displayAuthorName, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            if (review.isForApp) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ReviewStarGold.copy(alpha = 0.18f))
                                        .border(0.8.dp, ReviewStarGold.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(if (isEs) "⭐ RESEÑA APP" else "⭐ APP REVIEW", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = ReviewStarGold)
                                }
                            } else if (review.isVerifiedClaimer) {
                                Icon(Icons.Default.Verified, contentDescription = s.dialogs.reviewsVerifiedClaimer, tint = NeonGreen, modifier = Modifier.size(14.dp))
                            }
                        }

                        val timeAgo = remember(review.timestamp, isEs) {
                            runCatching {
                                DateUtils.getRelativeTimeSpanString(
                                    review.timestamp, System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE
                                ).toString()
                            }.getOrDefault(if (isEs) "Reciente" else "Recently")
                        }
                        Text(
                            text = if (review.isForApp) {
                                if (isEs) "App LootDrop • $timeAgo" else "LootDrop App • $timeAgo"
                            } else "${review.platform} • $timeAgo",
                            fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (i <= review.rating) ReviewStarGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            val cleanCardTitle = remember(review.id, review.title) { ReviewModerationFilter.sanitizeForDisplay(review.title) }
            if (cleanCardTitle.isNotBlank()) {
                Text(cleanCardTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            val cleanCardComment = remember(review.id, review.comment) { ReviewModerationFilter.sanitizeForDisplay(review.comment) }
            if (cleanCardComment.isNotBlank()) {
                Text(cleanCardComment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasVoted) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, if (hasVoted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable(enabled = !hasVoted) { onVoteHelpful() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("helpful_btn_${review.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = s.dialogs.reviewsHelpfulDone,
                        tint = if (hasVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = String.format(s.dialogs.reviewsHelpfulCountFormat, review.helpfulCount),
                        fontSize = 11.sp,
                        fontWeight = if (hasVoted) FontWeight.Bold else FontWeight.Medium,
                        color = if (hasVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (review.authorTag.isNotBlank() && review.authorTag != "Gamer") {
                    Text("🏷️ ${review.authorTag}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun LootDropAppRatingCard(
    reviews: List<PublicReview>,
    defaultAuthorName: String,
    onPostAppReview: (rating: Int, title: String, comment: String) -> Unit,
    onVoteHelpful: (reviewId: String) -> Unit,
    hasVotedHelpful: (reviewId: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    var isWriteDialogOpen by remember { mutableStateOf(false) }
    var isBrowseDialogOpen by remember { mutableStateOf(false) }

    val appReviews = remember(reviews) { reviews.filter { it.isForApp } }
    val avgAppRating = remember(appReviews) { if (appReviews.isEmpty()) 5.0 else appReviews.map { it.rating }.average() }
    val count = appReviews.size

    Card(
        modifier = modifier.fillMaxWidth().testTag("lootdrop_app_rating_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ReviewStarGold.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ReviewStarGold, Color(0xFFF97316)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(s.dialogs.reviewsAppRatingTitle.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = ReviewStarGold, letterSpacing = 0.8.sp)
                        Text(if (isEs) "Reseñas de la comunidad en Cloudflare R2" else "Community Reviews on Cloudflare R2", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("V${BuildConfig.VERSION_NAME}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(String.format(Locale.US, "%.1f", avgAppRating), fontSize = 32.sp, fontWeight = FontWeight.Black, color = ReviewStarGold)
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            val roundedScore = avgAppRating.toInt().coerceIn(1, 5)
                            for (i in 1..5) {
                                Icon(
                                    imageVector = if (i <= roundedScore) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= roundedScore) ReviewStarGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isEs) {
                                if (count == 1) "1 valoración y reseña" else "$count valoraciones y reseñas"
                            } else {
                                "$count player ${if (count == 1) "rating" else "ratings"} & reviews"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(if (isEs) "🏆 Obra Maestra" else "🏆 Masterpiece", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { isWriteDialogOpen = true },
                    modifier = Modifier.weight(1f).testTag("rate_lootdrop_app_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ReviewStarGold, contentColor = Color(0xFF0F172A))
                ) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(s.dialogs.reviewsDialogAppTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { isBrowseDialogOpen = true },
                    modifier = Modifier.weight(1f).testTag("browse_community_reviews_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(if (isEs) "Ver todas (${reviews.size})" else "All Reviews (${reviews.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
            onPostReview = { rating, title, comment, _ -> onPostAppReview(rating, title, comment) },
            onVoteHelpful = onVoteHelpful,
            hasVotedHelpful = hasVotedHelpful
        )
    }
}
