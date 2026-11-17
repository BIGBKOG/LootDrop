package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.ExpressivePlacementSpec
import com.example.ui.theme.rememberExpressiveCornerRadius
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.SteamAchievementEntity
import com.example.data.local.SteamGameEntity
import com.example.data.model.TrophyRarityTier
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedLabel
import com.example.ui.AchievementOverviewStats
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.AchievementWalkthroughHelper
import com.example.util.LocalLowEndOptimization

private enum class CompletionFilter(val label: String) {
    ALMOST_DONE("🔥 80%+ Almost Done"),
    MASTERED("👑 100% Mastered"),
    IN_PROGRESS("⭐ 40-79% Progress"),
    ALL("All Games");

    fun localized(isEs: Boolean): String = if (!isEs) label else when (this) {
        ALMOST_DONE -> "🔥 80%+ Casi listos"
        MASTERED -> "👑 100% Completados"
        IN_PROGRESS -> "⭐ 40-79% En progreso"
        ALL -> "Todos"
    }
}

@Composable
fun TrophyShowcaseContent(
    overviewStats: AchievementOverviewStats,
    rarestTrophies: List<SteamAchievementEntity>,
    closestGames: List<SteamGameEntity>,
    isSyncing: Boolean,
    steamUserId: String,
    onSyncClick: () -> Unit,
    onOpenSteamSettings: () -> Unit,
    onSelectGameForAchievements: (SteamGameEntity) -> Unit,
    topHeader: (@Composable () -> Unit)? = null,
    listState: LazyListState = rememberLazyListState(),
    isAnimationsEnabled: Boolean = true,
    isLowEndOptimization: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val effectiveLowEnd = isLowEndOptimization || LocalLowEndOptimization.current
    val reducedMotion = !isAnimationsEnabled || effectiveLowEnd
    var selectedFilter by remember { mutableStateOf(CompletionFilter.ALMOST_DONE) }

    val filterCounts = remember(closestGames) {
        var almostDone = 0
        var mastered = 0
        var inProgress = 0
        for (game in closestGames) {
            val pct = game.achievementsPercentage
            when {
                pct == 100 -> mastered++
                pct in 80..99 -> almostDone++
                pct in 40..79 -> inProgress++
            }
        }
        intArrayOf(almostDone, mastered, inProgress, closestGames.size)
    }

    val filteredGames = remember(closestGames, selectedFilter) {
        when (selectedFilter) {
            CompletionFilter.ALMOST_DONE -> closestGames.filter { it.achievementsPercentage in 80..99 }
            CompletionFilter.MASTERED -> closestGames.filter { it.achievementsPercentage == 100 }
            CompletionFilter.IN_PROGRESS -> closestGames.filter { it.achievementsPercentage in 40..79 }
            CompletionFilter.ALL -> closestGames
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("trophy_showcase_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 104.dp)
    ) {
        // 0. Top Menus (Slides up on scroll)
        if (topHeader != null) {
            item(key = "trophy_top_header") {
                topHeader()
            }
        }

        // 1. Hero Overview Stats Card
        item(key = "overview_stats") {
            TrophyOverviewHeader(
                stats = overviewStats,
                isSyncing = isSyncing,
                reducedMotion = reducedMotion,
                steamUserId = steamUserId,
                onSyncClick = onSyncClick,
                onOpenSteamSettings = onOpenSteamSettings
            )
        }

        // 2. Trophy Wall: Rarest Unlocked Trophies
        item(key = "rarest_trophies_section") {
            TrophyWallSection(
                trophies = rarestTrophies,
                onTrophyClick = { trophy ->
                    // Open community guide or Google search for context
                    AchievementWalkthroughHelper.openSteamGlobalAchievements(context, trophy.appId)
                }
            )
        }

        // 3. Closest to 100% Tracker Section Header & Filter Chips
        item(key = "closest_tracker_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                                .size(22.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (isEs) "RASTREADOR CASI AL 100%" else "CLOSEST TO 100% TRACKER",
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.6.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            val nearCount = filterCounts[0]
                            Text(
                                text = if (isEs) "$nearCount casi al 100%" else "$nearCount near 100%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = if (isEs) "Juegos a punto de completar al 100% para conseguir Trofeos Míticos fácilmente." else "Spotlight games on the verge of 100% completion for easy Mythical Trophies.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletionFilter.values().forEach { filter ->
                        val isSelected = filter == selectedFilter
                        val count = filterCounts[filter.ordinal]

                        val chipCorner = rememberExpressiveCornerRadius(
                            isSelected = isSelected,
                            baseRadius = 10.dp,
                            selectedRadius = 16.dp,
                            enabled = !reducedMotion
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = "${filter.localized(isEs)} ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(chipCorner)
                        )
                    }
                }
            }
        }

        // 4. Closest to 100% Game Cards
        if (filteredGames.isEmpty()) {
            item(key = "empty_games") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEs) "No hay juegos en esta categoría. ¡Sincroniza tu biblioteca de Steam para ordenar por progreso!" else "No games found in this category. Sync your Steam library to rank games by completion!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredGames, key = { "closest_${it.appId}" }) { game ->
                ClosestGameCard(
                    game = game,
                    lowEnd = effectiveLowEnd,
                    onClick = { onSelectGameForAchievements(game) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .then(
                            if (!reducedMotion) {
                                Modifier.animateItem(
                                    fadeInSpec = ExpressiveMotion.effectsDefault(),
                                    placementSpec = ExpressivePlacementSpec,
                                    fadeOutSpec = ExpressiveMotion.effectsFast()
                                )
                            } else Modifier
                        )
                )
            }
        }
    }
}

@Composable
private fun TrophyOverviewHeader(
    stats: AchievementOverviewStats,
    isSyncing: Boolean,
    reducedMotion: Boolean,
    steamUserId: String,
    onSyncClick: () -> Unit,
    onOpenSteamSettings: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val spinAngle = if (isSyncing && !reducedMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "trophy_spin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin_angle"
        ).value
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFE040FB).copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f)
                    )
                )
            )
            .border(1.dp, Color(0xFFE040FB).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Title Row + Sync Button
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE040FB).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = Color(0xFFE040FB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isEs) "CAZADOR DE LOGROS Y VITRINA" else "ACHIEVEMENT HUNTER & TROPHY SHOWCASE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE040FB),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (steamUserId.isNotBlank()) {
                                if (isEs) "Vinculado: $steamUserId" else "Linked: $steamUserId"
                            } else {
                                if (isEs) "Toca Sincronizar para vincular Steam" else "Tap Sync to link Steam"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onSyncClick,
                        enabled = !isSyncing,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = s.library.steamSyncCd,
                            tint = if (isSyncing) Color(0xFFE040FB) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (isSyncing && !reducedMotion) Modifier.rotate(spinAngle) else Modifier)
                        )
                    }

                    IconButton(
                        onClick = onOpenSteamSettings,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = s.library.steamSettingsCd,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Stat Badges Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    icon = "👑",
                    value = "${stats.masteredGamesCount}",
                    label = s.library.trophiesMasteredGames,
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )

                StatPill(
                    icon = "💎",
                    value = "${stats.mythicTrophiesCount}",
                    label = s.library.trophiesMythicCount,
                    accentColor = Color(0xFFE040FB),
                    modifier = Modifier.weight(1f)
                )

                StatPill(
                    icon = "🏆",
                    value = "${stats.totalUnlockedTrophies}",
                    label = s.library.trophiesTotalUnlocked,
                    accentColor = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )

                StatPill(
                    icon = "📊",
                    value = "${stats.averageCompletionPercentage}%",
                    label = s.library.trophiesAvgCompletion,
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: String,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = icon, fontSize = 11.sp)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun TrophyWallSection(
    trophies: List<SteamAchievementEntity>,
    onTrophyClick: (SteamAchievementEntity) -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = if (isEs) "MURO DE TROFEOS: MÁS RAROS" else "TROPHY WALL: RAREST UNLOCKED",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.6.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFB800).copy(alpha = 0.18f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(text = if (isEs) "RAREZA" else "RARITY", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFB800))
                }
            }

            Text(
                text = if (isEs) "${trophies.size} trofeos" else "${trophies.size} trophies",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (trophies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isEs) "Aún no hay trofeos desbloqueados. ¡Vincula tu cuenta de Steam para lucir tus logros más raros!" else "No unlocked trophies found yet. Link your Steam account to showcase your rarest achievements!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trophies, key = { "wall_${it.appId}_${it.apiName}" }) { trophy ->
                    TrophyWallCard(
                        trophy = trophy,
                        onClick = { onTrophyClick(trophy) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrophyWallCard(
    trophy: SteamAchievementEntity,
    onClick: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val tier = trophy.rarityTier
    val tierColor = tier.color
    val isMythic = tier == TrophyRarityTier.MYTHIC

    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (isMythic) 1.5.dp else 1.dp,
                color = if (isMythic) tierColor.copy(alpha = 0.8f) else tierColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Game Title & Rarity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trophy.gameName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tier.containerColor)
                        .border(1.dp, tierColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${tier.iconEmoji} ${trophy.formattedRarity}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tierColor
                    )
                }
            }

            // Trophy Icon & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tierColor.copy(alpha = 0.16f))
                        .border(1.dp, tierColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = tier.iconEmoji, fontSize = 18.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trophy.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = tier.localizedLabel(s),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                }
            }

            // Description
            Text(
                text = trophy.description.ifBlank {
                    if (isEs) "Desbloqueado en ${trophy.gameName}" else "Unlocked in ${trophy.gameName}"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ClosestGameCard(
    game: SteamGameEntity,
    lowEnd: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val percent = game.achievementsPercentage
    val remaining = (game.achievementsTotal - game.achievementsUnlocked).coerceAtLeast(0)
    val isNearHundred = percent in 80..99 && remaining in 1..5
    val isMastered = percent == 100
    val headerImageRequest = remember(context, game.headerImageUrl, lowEnd) {
        buildGiveawayImageRequest(
            context = context,
            url = game.headerImageUrl,
            isLowEndOptimization = lowEnd,
            width = if (lowEnd) 460 else 640,
            height = if (lowEnd) 190 else 280
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("closest_game_card_${game.appId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMastered) NeonGreen.copy(alpha = 0.6f)
            else if (isNearHundred) NeonCyan.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
        )
    ) {
        Column {
            // Header Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
            ) {
                AsyncImage(
                    model = headerImageRequest,
                    contentDescription = game.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )

                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                            )
                        )
                )

                // Spotlight callout pill
                if (isNearHundred) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF003B46).copy(alpha = 0.9f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isEs) "🔥 ¡SOLO FALTAN $remaining PARA EL 100%!" else "🔥 ONLY $remaining LEFT FOR 100%!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan
                        )
                    }
                } else if (isMastered) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F3B20).copy(alpha = 0.9f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isEs) "👑 100% COMPLETADO" else "👑 100% MASTERED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGreen
                        )
                    }
                }
            }

            // Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = game.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "${game.achievementsUnlocked} / ${game.achievementsTotal} ($percent%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMastered) NeonGreen else MaterialTheme.colorScheme.primary
                    )
                }

                // Smooth Progress Bar
                LinearProgressIndicator(
                    progress = { (percent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isMastered) NeonGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )

                // Bottom row: Playtime + Explore Walkthroughs button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⏱️ ${String.format(s.home.cardPlaytimeFormat, game.playtimeHours)} • ${game.playStatus.localizedLabel(s)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (isMastered) s.library.steamViewTrophies else if (isEs) "Ver Guías" else "View Walkthroughs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
