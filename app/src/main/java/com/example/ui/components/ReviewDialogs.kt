package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.data.model.PublicReview
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.util.ReviewModerationFilter

internal val ReviewStarGold = Color(0xFFFFB800)

@Composable
fun WriteReviewDialog(
    gameTitle: String,
    defaultAuthorName: String,
    initialIsAppReview: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, title: String, comment: String, isAppReview: Boolean) -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    var isAppReview by remember { mutableStateOf(initialIsAppReview || gameTitle.contains("LootDrop", ignoreCase = true)) }
    var rating by remember { mutableIntStateOf(5) }
    var reviewTitle by remember { mutableStateOf("") }
    var reviewComment by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf(defaultAuthorName) }
    var isSubmitting by remember { mutableStateOf(false) }
    var moderationError by remember { mutableStateOf<String?>(null) }

    val ratingLabels = if (isEs) {
        listOf(
            "1 - Evitar / Injugable",
            "2 - Mediocre / Con fallos",
            "3 - Normal / Entretenido",
            "4 - Muy bueno / Recomendado",
            "5 - ¡Obra maestra / Imprescindible!"
        )
    } else {
        listOf(
            "1 - Skip / Unplayable",
            "2 - Mediocre / Flawed",
            "3 - Average / Fun with Friends",
            "4 - Great / Highly Recommended",
            "5 - Masterpiece / Must Play!"
        )
    }

    val goldFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ReviewStarGold,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = ReviewStarGold,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f).padding(16.dp).testTag("write_review_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ReviewStarGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAppReview) s.dialogs.reviewsDialogAppTitle else s.dialogs.reviewsDialogPostTitle,
                            fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAppReview) {
                                if (isEs) "LootDrop V${BuildConfig.VERSION_NAME} • Radar de Juegos Gratis" else "LootDrop V${BuildConfig.VERSION_NAME} • Free Game Tracker"
                            } else gameTitle,
                            fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = if (isAppReview) ReviewStarGold else MaterialTheme.colorScheme.primary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = s.common.close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (gameTitle.isNotBlank() && !gameTitle.contains("LootDrop", ignoreCase = true)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (!isAppReview) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent)
                                .clickable { isAppReview = false }.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEs) "🎮 Reseña de Juego" else "🎮 Game Review",
                                fontSize = 11.sp,
                                fontWeight = if (!isAppReview) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isAppReview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (isAppReview) ReviewStarGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { isAppReview = true }.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⭐ ${s.dialogs.reviewsDialogAppTitle}",
                                fontSize = 11.sp,
                                fontWeight = if (isAppReview) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAppReview) ReviewStarGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            val isSelected = i <= rating
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isEs) "$i estrellas" else "$i Stars",
                                tint = if (isSelected) ReviewStarGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(38.dp).clip(CircleShape).clickable { rating = i }.testTag("star_picker_$i")
                            )
                        }
                    }
                    Text(ratingLabels[(rating - 1).coerceIn(0, 4)], fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ReviewStarGold)
                }

                OutlinedTextField(
                    value = reviewTitle,
                    onValueChange = { reviewTitle = it; moderationError = null },
                    label = {
                        Text(
                            text = if (isEs) {
                                if (isAppReview) "Título (ej. ¡El mejor radar de juegos!)" else "Título (ej. Historia y gráficos increíbles)"
                            } else {
                                if (isAppReview) "Review Headline (e.g. Best game tracker ever!)" else "Review Headline (e.g. Incredible story & visuals)"
                            },
                            fontSize = 12.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("review_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = goldFieldColors
                )

                OutlinedTextField(
                    value = reviewComment,
                    onValueChange = { reviewComment = it; moderationError = null },
                    label = {
                        Text(
                            text = if (isEs) {
                                if (isAppReview) "¿Qué te gusta de LootDrop? (Alertas, temas...)" else s.dialogs.reviewsCommentPlaceholder
                            } else {
                                if (isAppReview) "What do you love about LootDrop? (Alerts, themes, backups...)" else "Your thoughts on gameplay, performance, story..."
                            },
                            fontSize = 12.sp
                        )
                    },
                    minLines = 3, maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("review_comment_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = goldFieldColors
                )

                OutlinedTextField(
                    value = authorName,
                    onValueChange = { authorName = it; moderationError = null },
                    label = { Text(s.library.profileGamerTagLabel, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("review_author_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Text(
                    text = if (isEs) "☁️ Las reseñas se suben públicamente a Cloudflare R2 y las ven todos los jugadores en Android y Web. Solo lenguaje respetuoso (sin insultos, direcciones ni teléfonos)." else "☁️ Reviews are uploaded publicly to our Cloudflare R2 bucket and visible to all gamers on Android & Web. Respectful language only (no swears, slurs, addresses, or phone numbers).",
                    fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp
                )

                AnimatedVisibility(visible = moderationError != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("review_moderation_error"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = if (isEs) "Aviso de moderación" else "Moderation Warning", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(18.dp))
                        Text(moderationError.orEmpty(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer, lineHeight = 16.sp)
                    }
                }

                Button(
                    onClick = {
                        val validation = ReviewModerationFilter.validateReviewInput(reviewTitle, reviewComment, authorName)
                        if (!validation.isAllowed) {
                            moderationError = validation.errorMessage
                            isSubmitting = false
                        } else {
                            moderationError = null
                            isSubmitting = true
                            onSubmit(rating, reviewTitle, reviewComment, isAppReview)
                        }
                    },
                    enabled = !isSubmitting && (reviewTitle.isNotBlank() || reviewComment.isNotBlank()),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_review_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReviewStarGold, contentColor = Color(0xFF0F172A),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0F172A), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (isEs) "🚀 ${s.dialogs.reviewsSubmitBtn}" else if (isAppReview) "🚀 Post LootDrop App Rating" else "🚀 Post Review to Community",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AllCommunityReviewsDialog(
    reviews: List<PublicReview>,
    defaultAuthorName: String,
    onDismiss: () -> Unit,
    onPostReview: (rating: Int, title: String, comment: String, isAppReview: Boolean) -> Unit,
    onVoteHelpful: (reviewId: String) -> Unit,
    hasVotedHelpful: (reviewId: String) -> Boolean
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    var selectedFilter by remember { mutableStateOf("ALL") }
    var isWriteDialogOpen by remember { mutableStateOf(false) }

    val filteredReviews = remember(reviews, selectedFilter) {
        when (selectedFilter) {
            "APP" -> reviews.filter { it.isForApp }
            "GAMES" -> reviews.filter { !it.isForApp }
            else -> reviews
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.96f).height(640.dp).padding(12.dp).testTag("all_community_reviews_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, ReviewStarGold.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(ReviewStarGold), contentAlignment = Alignment.Center) {
                            Text("⭐", fontSize = 16.sp)
                        }
                        Column {
                            Text(s.dialogs.reviewsCommunityTitle, fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text(if (isEs) "Sincronizado con Cloudflare R2" else "Synced via Cloudflare R2", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = s.common.close, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "ALL" to "${if (isEs) "Todas" else "All"} (${reviews.size})",
                            "APP" to "⭐ App (${reviews.count { it.isForApp }})",
                            "GAMES" to "🎮 ${if (isEs) "Juegos" else "Games"} (${reviews.count { !it.isForApp }})"
                        ).forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ReviewStarGold else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable { selectedFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { isWriteDialogOpen = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ReviewStarGold),
                        border = BorderStroke(1.dp, ReviewStarGold.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("✍️ ${if (isEs) "Valorar" else "Rate App"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (filteredReviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(if (isEs) "Aún no hay reseñas en esta categoría." else "No reviews found in this category yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredReviews, key = { it.id }) { review ->
                            ReviewCard(review = review, hasVoted = hasVotedHelpful(review.id), onVoteHelpful = { onVoteHelpful(review.id) })
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
