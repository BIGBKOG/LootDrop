package com.example.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.example.data.model.GiveawayItem
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.ui.components.buildGiveawayImageRequest
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.expressivePressScale
import com.example.util.LocalLowEndOptimization
import com.example.util.ShareGraphicGenerator
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
internal fun HomeBestFreeGamesSection(
    topGames: List<GiveawayItem>,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val items = remember(topGames) { topGames.take(3) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { items.size.coerceAtLeast(1) })
    val coroutineScope = rememberCoroutineScope()
    val lowEnd = isLowEndOptimization || LocalLowEndOptimization.current
    val reducedMotion = !isAnimationsEnabled || lowEnd

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_top_free_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(NeonGreen.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = if (isEs) "MEJORES 3 JUEGOS GRATIS HOY" else "BEST 3 FREE GAMES TODAY",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = if (isEs) "Drops mejor valorados 100% gratis • Desliza tarjetas" else "Top-rated 100% free drops • Swipe cards",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (reducedMotion) {
                        val pageNum = pagerState.currentPage + 1
                        Text(
                            text = if (isEs) "$pageNum de ${items.size}" else "$pageNum of ${items.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    } else {
                        AnimatedContent(
                            targetState = pagerState.currentPage + 1,
                            transitionSpec = { ExpressiveMotion.popNumberTransform(false) },
                            label = "carousel_page_badge"
                        ) { pageNum ->
                            Text(
                                text = if (isEs) "$pageNum de ${items.size}" else "$pageNum of ${items.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }
                }
            }
        }

        if (items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = NeonGreen, strokeWidth = 2.dp)
                        Text(
                            text = if (isEs) "Cargando los mejores juegos gratis..." else "Loading today's best free games...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().testTag("home_top_free_pager"),
                pageSpacing = 12.dp
            ) { page ->
                val game = items[page]
                val cardModifier = if (!reducedMotion) {
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        .absoluteValue.coerceIn(0f, 1f)
                    if (pageOffset > 0.001f) {
                        Modifier.graphicsLayer {
                            val scale = lerp(1f, 0.93f, pageOffset)
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(1f, 0.65f, pageOffset)
                        }
                    } else Modifier
                } else Modifier
                BestFreeGameCard(
                    game = game,
                    index = page,
                    onClick = { onItemClick(game) },
                    onClaimClick = { onClaimClick(game) },
                    isReducedMotion = reducedMotion,
                    isLowEndOptimization = lowEnd,
                    modifier = cardModifier
                )
            }

            if (items.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth = if (reducedMotion) {
                            if (isSelected) 24f else 6f
                        } else {
                            animateFloatAsState(
                                targetValue = if (isSelected) 24f else 6f,
                                animationSpec = ExpressiveMotion.spatialFast(false),
                                label = "carousel_dot_width_$index"
                            ).value
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(dotWidth.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonGreen else MaterialTheme.colorScheme.outline)
                                .clickable {
                                    coroutineScope.launch {
                                        if (reducedMotion) {
                                            pagerState.scrollToPage(index)
                                        } else {
                                            pagerState.animateScrollToPage(
                                                page = index,
                                                animationSpec = ExpressiveMotion.spatialDefault(false)
                                            )
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BestFreeGameCard(
    game: GiveawayItem,
    index: Int,
    onClick: () -> Unit,
    onClaimClick: () -> Unit,
    isReducedMotion: Boolean = false,
    isLowEndOptimization: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val coroutineScope = rememberCoroutineScope()
    val cardInteraction = remember { MutableInteractionSource() }
    val cardSurface = MaterialTheme.colorScheme.surface
    val heroImageRequest = remember(context, game.image, isLowEndOptimization) {
        buildGiveawayImageRequest(
            context = context,
            url = game.image,
            isLowEndOptimization = isLowEndOptimization,
            width = if (isLowEndOptimization) 720 else 960,
            height = if (isLowEndOptimization) 380 else 510
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .expressivePressScale(cardInteraction, enabled = !isReducedMotion, pressedScale = 0.97f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(interactionSource = cardInteraction, indication = null) { onClick() }
            .testTag("home_top_free_card_$index"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardSurface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 8.5f)) {
                AsyncImage(
                    model = heroImageRequest,
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, cardSurface.copy(alpha = 0.45f), cardSurface)
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xE60D0F15))
                            .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (game.worth.isNotBlank() && game.worth != "N/A" && game.worth != "$0.00") {
                                Text(
                                    text = game.worth,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                            Text(s.home.badgeFree, fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeonGreen)
                        }
                    }

                    if (game.metacriticScore > 0) {
                        val scoreColor = if (game.metacriticScore >= 75) NeonGreen else NeonGold
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xE60D0F15))
                                .border(1.dp, scoreColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text("METASCORE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text("${game.metacriticScore}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = scoreColor)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = game.platforms,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (game.endDate != "N/A" && game.endDate.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xCC000000))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(if (isEs) "Termina: ${game.endDate}" else "Ends: ${game.endDate}", fontSize = 10.sp, color = TextSecondary, maxLines = 1)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (game.description.isNotBlank()) {
                    Text(
                        text = game.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onClaimClick,
                        modifier = Modifier.weight(1.15f).height(40.dp).testTag("home_top_free_claim_btn_$index"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color(0xFF0B0F19)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(if (isEs) "Reclamar" else "Claim Loot", fontWeight = FontWeight.Black, fontSize = 12.sp, maxLines = 1)
                        }
                    }

                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(0.95f).height(40.dp).testTag("home_top_free_details_btn_$index"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(s.home.cardDetailsCta, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    ShareGraphicGenerator.shareGraphicCard(
                                        context = context,
                                        item = game,
                                        includeTextLink = true
                                    )
                                } catch (_: Exception) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            if (isEs) "🔥 Oferta Gratis: ${game.title} (${game.worth})!\nReclámalo aquí: ${game.openGiveawayUrl}\nDetectado con LootDrop"
                                            else "🔥 Free Game Deal: ${game.title} (${game.worth})!\nClaim here: ${game.openGiveawayUrl}\nTracked with LootDrop"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, s.system.notifShareChooser))
                                }
                            }
                        },
                        modifier = Modifier.size(40.dp).testTag("home_top_free_share_btn_$index"),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                            contentColor = NeonCyan
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "${s.common.share} ${game.title}", tint = NeonCyan, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }
    }
}
