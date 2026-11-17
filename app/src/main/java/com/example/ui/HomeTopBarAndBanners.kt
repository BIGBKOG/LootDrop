package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppUpdateInfo
import com.example.data.model.GameCategory
import com.example.data.model.LauncherType
import com.example.data.model.UserProfile
import com.example.data.model.formatStorageGb
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedDisplayName
import com.example.i18n.localizedLabel
import com.example.ui.components.ExpressiveTopActionBar
import com.example.ui.components.ProfileAvatarCircle
import com.example.ui.theme.CollapsibleHeaderState
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.collapsibleHeaderLayout
import com.example.util.LocalLowEndOptimization
import java.util.Locale

@Composable
internal fun HomeTopBarChrome(
    currentTab: MainTab,
    userProfile: UserProfile,
    reducedMotion: Boolean,
    collapsibleHeaderState: CollapsibleHeaderState,
    isSearchExpanded: Boolean,
    searchQuery: String,
    activeFilterCount: Int,
    unreadNotificationCount: Int,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean,
    reopenStatusMessage: String?,
    appUpdateInfo: AppUpdateInfo?,
    onOpenProfile: () -> Unit,
    onToggleSearch: () -> Unit,
    onFilterClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onOpenUpdateDialog: () -> Unit,
    onQuickUpdateClick: (AppUpdateInfo) -> Unit,
    onDismissReopenBanner: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .collapsibleHeaderLayout(collapsibleHeaderState, applyParallaxFade = !reducedMotion)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (currentTab == MainTab.HOME) {
                    ProfileAvatarCircle(profile = userProfile, size = 36.dp, onClick = onOpenProfile)
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "LootDrop Logo",
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                    )
                }
                Column {
                    val tabBadgeLabel = when (currentTab) {
                        MainTab.HOME -> s.common.tabHome.uppercase()
                        MainTab.SETTINGS -> s.common.tabSettings.uppercase()
                        MainTab.CALENDAR -> s.common.tabCalendar.uppercase()
                        MainTab.CLAIMED -> s.common.tabClaimed.uppercase()
                        MainTab.DISCOVER -> "RADAR"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Loot", style = MaterialTheme.typography.titleLarge, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                        Text("Drop", style = MaterialTheme.typography.titleLarge, fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                .border(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            if (reducedMotion) {
                                Text(tabBadgeLabel, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                            } else {
                                AnimatedContent(
                                    targetState = tabBadgeLabel,
                                    transitionSpec = { ExpressiveMotion.popNumberTransform(false) },
                                    label = "top_tab_badge"
                                ) { tabBadgeText ->
                                    Text(tabBadgeText, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, letterSpacing = 0.5.sp)
                                }
                            }
                        }
                    }
                    val subtitleLabel = when (currentTab) {
                        MainTab.HOME -> if (userProfile.name.isNotBlank()) {
                            if (isEs) "HOLA, ${userProfile.name.uppercase()}" else "WELCOME, ${userProfile.name.uppercase()}"
                        } else {
                            if (isEs) "ACTIVIDAD RECIENTE Y DESTACADOS" else "RECENT ACTIVITY & TOP PICKS"
                        }
                        MainTab.SETTINGS -> if (isEs) "AJUSTES Y PREFERENCIAS" else "APP PREFERENCES & CONTROLS"
                        MainTab.CALENDAR -> if (isEs) "RADAR Y FECHAS DE CIERRE" else "EXPIRATION RADAR & TIMELINE"
                        MainTab.CLAIMED -> if (isEs) "MI BÓVEDA DE JUEGOS" else "MY CLAIMED GAME VAULT"
                        MainTab.DISCOVER -> if (isEs) "RADAR GRATIS DE PC Y CONSOLAS" else "PC & CONSOLE FREE RADAR"
                    }
                    if (reducedMotion) {
                        Text(
                            text = subtitleLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        AnimatedContent(
                            targetState = subtitleLabel,
                            transitionSpec = { ExpressiveMotion.popNumberTransform(false) },
                            label = "top_tab_subtitle"
                        ) { subtitleText ->
                            Text(
                                text = subtitleText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            ExpressiveTopActionBar(
                isSearchExpanded = isSearchExpanded && currentTab != MainTab.SETTINGS && currentTab != MainTab.HOME,
                searchQuery = searchQuery,
                onToggleSearch = onToggleSearch,
                activeFilterCount = activeFilterCount,
                onFilterClick = onFilterClick,
                unreadNotificationCount = unreadNotificationCount,
                onNotificationClick = onNotificationClick,
                isAnimationsEnabled = isAnimationsEnabled,
                isLowEndOptimization = isLowEndOptimization
            )
        }

        AnimatedVisibility(
            visible = reopenStatusMessage != null && currentTab != MainTab.SETTINGS,
            enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
            exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
        ) {
            reopenStatusMessage?.let { msg ->
                val hasUpdate = appUpdateInfo?.hasUpdate == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, if (hasUpdate) NeonGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable { if (hasUpdate) onOpenUpdateDialog() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("reopen_status_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (hasUpdate) Icons.Default.NewReleases else Icons.Default.Sync,
                            contentDescription = null,
                            tint = if (hasUpdate) NeonGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(msg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    if (hasUpdate) {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen)
                                .clickable { onQuickUpdateClick(appUpdateInfo) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                .testTag("banner_one_tap_update_button")
                        ) {
                            Text(if (isEs) "Actualizar" else "Update", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                    IconButton(onClick = onDismissReopenBanner, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = s.common.dismissCd, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ClaimedSavingsBanner(
    savingsUsd: Double,
    claimedCount: Int,
    totalStorageGb: Float = 0f,
    isAnimationsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val isEs = s.language == AppLanguage.SPANISH
    val effectiveAnimations = isAnimationsEnabled && !LocalLowEndOptimization.current
    val (borderAlpha, medalBob, medalRot) = rememberClaimedBannerAnimations(enabled = effectiveAnimations)
    val animatedSavings = if (effectiveAnimations) {
        animateFloatAsState(
            targetValue = savingsUsd.toFloat(),
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "animatedSavings"
        ).value
    } else {
        savingsUsd.toFloat()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        NeonGreen.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f)
                    )
                )
            )
            .border(1.dp, NeonGreen.copy(alpha = borderAlpha * 0.6f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("claimed_savings_banner")
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(NeonGreen.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(15.dp))
                    }
                    Text(
                        if (isEs) "TOTAL DE DINERO AHORRADO" else "TOTAL MONEY SAVED",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonGreen, letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = String.format(Locale.US, "$%.2f", animatedSavings),
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (isEs) "AHORRADOS" else "SAVED",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen, modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val avgSaved = if (claimedCount > 0) animatedSavings.toDouble() / claimedCount else 0.0
                Text(
                    text = if (claimedCount > 0) {
                        if (isEs) "$claimedCount juegos reclamados • prom. ${String.format(Locale.US, "$%.2f", avgSaved)} / juego"
                        else "$claimedCount claimed games • avg ${String.format(Locale.US, "$%.2f", avgSaved)} / game"
                    } else {
                        if (isEs) "Reclama juegos para llevar el control de tu ahorro"
                        else "Claim games to track your savings backlog"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (totalStorageGb > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("💾", fontSize = 11.sp)
                        Text(
                            text = if (isEs) "Espacio estimado: ${formatStorageGb(totalStorageGb)} totales en disco"
                            else "Storage Footprint: ${formatStorageGb(totalStorageGb)} total planned drive space",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .then(
                        if (effectiveAnimations) {
                            Modifier.graphicsLayer { translationY = medalBob; rotationZ = medalRot }
                        } else Modifier
                    )
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .border(1.dp, NeonGreen.copy(alpha = borderAlpha), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
internal fun ActiveFiltersSummaryPill(
    selectedLaunchers: Set<LauncherType>,
    selectedTypeFilter: TypeFilter,
    selectedCategories: Set<GameCategory>,
    selectedSort: SortOption,
    onOpenFilterSheet: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenFilterSheet)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
            Text(
                text = buildString {
                    if (selectedLaunchers.isNotEmpty() && !selectedLaunchers.contains(LauncherType.ALL)) {
                        append(selectedLaunchers.joinToString(", ") { it.localizedDisplayName(s) })
                    }
                    if (selectedTypeFilter != TypeFilter.ALL && selectedTypeFilter.apiMatch != null) {
                        if (isNotEmpty()) append(" • ")
                        append(selectedTypeFilter.localizedLabel(s))
                    }
                    if (selectedCategories.isNotEmpty()) {
                        if (isNotEmpty()) append(" • ")
                        append(
                            if (selectedCategories.size <= 2) selectedCategories.joinToString(", ") { it.localizedDisplayName(s) }
                            else if (s.language == AppLanguage.SPANISH) "${selectedCategories.size} Categorías"
                            else "${selectedCategories.size} Categories"
                        )
                    }
                    if (selectedSort != SortOption.NEWEST) {
                        if (isNotEmpty()) append(" • ")
                        append(selectedSort.localizedLabel(s))
                    }
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = s.common.reset,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onResetFilters).padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun rememberAnimatedBeamAngle(enabled: Boolean): Float {
    if (!enabled) return 0f
    val transition = rememberInfiniteTransition(label = "borderBeamTransition")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3800, easing = LinearEasing), RepeatMode.Restart),
        label = "borderBeamAngle"
    )
    return angle
}

@Composable
private fun rememberClaimedBannerAnimations(enabled: Boolean): Triple<Float, Float, Float> {
    if (!enabled) return Triple(0.6f, 0f, 0f)
    val transition = rememberInfiniteTransition(label = "claimedBannerAnim")
    val borderAlpha by transition.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "claimedBorderAlpha")
    val medalBob by transition.animateFloat(-2.5f, 2.5f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "medalBob")
    val medalRot by transition.animateFloat(-5f, 5f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "medalRot")
    return Triple(borderAlpha, medalBob, medalRot)
}

@Composable
internal fun rememberEmptyBob(enabled: Boolean): Float {
    if (!enabled) return 0f
    val transition = rememberInfiniteTransition(label = "emptyBob")
    val bob by transition.animateFloat(-4f, 4f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "emptyIconBob")
    return bob
}
