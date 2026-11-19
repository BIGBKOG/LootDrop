package com.example.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GiveawayItem
import com.example.i18n.AppLanguage
import com.example.i18n.LocalAppStrings
import com.example.i18n.localizedLabel
import com.example.preferences.CardDensity
import com.example.ui.components.GiveawayCard
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.theme.ExpressiveMotion
import com.example.ui.theme.ExpressivePlacementSpec
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.rememberExpressiveCornerRadius
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val CALENDAR_LOCALE_ES: Locale = Locale.forLanguageTag("es")
private val CALENDAR_LOCALE_FR: Locale = Locale.forLanguageTag("fr")

private data class CalendarIndexSummary(
    val dayExpiringCountMap: Map<LocalDate, Int>,
    val itemsByDate: Map<LocalDate, List<GiveawayItem>>,
    val next14DaysItems: List<GiveawayItem>,
    val next7DaysItems: List<GiveawayItem>,
    val urgentItems: List<GiveawayItem>,
    val ongoingGiveaways: List<GiveawayItem>,
    val totalExpiringIn14Days: Int,
    val totalValueExpiring14Days: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTabContent(
    giveaways: List<GiveawayItem>,
    isLoading: Boolean,
    errorMessage: String? = null,
    searchQuery: String = "",
    cardDensity: CardDensity,
    isShowOriginalPrice: Boolean,
    isShowExpiration: Boolean,
    isAnimationsEnabled: Boolean,
    isLowEndOptimization: Boolean = false,
    beamAngle: Float = 0f,
    screenDensityInfo: ScreenDensityInfo,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onItemClick: (GiveawayItem) -> Unit,
    onClaimClick: (GiveawayItem) -> Unit,
    onToggleBookmark: (GiveawayItem) -> Unit,
    onToggleClaimed: (GiveawayItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    val lang = s.language
    val today = remember { LocalDate.now() }
    val upcomingDays = remember(today) {
        ExpirationCalendarUtils.getUpcomingCalendarDays(today, daysCount = 15)
    }

    val reducedMotion = !isAnimationsEnabled || isLowEndOptimization

    // Selected day from day-strip picker: null means "All Days in scope"
    var selectedCalendarDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedScope by remember { mutableStateOf(CalendarFilterScope.NEXT_14_DAYS) }
    var showOngoingGiveaways by remember { mutableStateOf(false) }
    var isCalendarHeaderCompact by rememberSaveable { mutableStateOf(false) }

    // Single-pass index build across giveaways for day counts, scope lists, ongoing list, and summary metrics
    val calendarSummary = remember(giveaways, today) {
        val dayCountMap = HashMap<LocalDate, Int>(24)
        val byDate = HashMap<LocalDate, MutableList<GiveawayItem>>(24)
        val next14 = ArrayList<GiveawayItem>(giveaways.size)
        val next7 = ArrayList<GiveawayItem>(giveaways.size)
        val urgent = ArrayList<GiveawayItem>(16)
        val ongoing = ArrayList<GiveawayItem>(16)
        var value14 = 0.0

        for (item in giveaways) {
            val endDateStr = item.endDate
            if (endDateStr.isBlank() || endDateStr.equals("N/A", ignoreCase = true)) {
                ongoing.add(item)
                continue
            }
            val parsedDate = ExpirationCalendarUtils.parseExpirationDate(endDateStr) ?: continue
            dayCountMap[parsedDate] = (dayCountMap[parsedDate] ?: 0) + 1
            byDate.getOrPut(parsedDate) { ArrayList() }.add(item)
            val days = ChronoUnit.DAYS.between(today, parsedDate)
            if (days in 0L..14L) {
                next14.add(item)
                value14 += item.numericWorth
            }
            if (days in 0L..7L) {
                next7.add(item)
            }
            if (days in 0L..1L) {
                urgent.add(item)
            }
        }
        CalendarIndexSummary(
            dayExpiringCountMap = dayCountMap,
            itemsByDate = byDate,
            next14DaysItems = next14,
            next7DaysItems = next7,
            urgentItems = urgent,
            ongoingGiveaways = ongoing,
            totalExpiringIn14Days = next14.size,
            totalValueExpiring14Days = value14
        )
    }

    val dayExpiringCountMap = calendarSummary.dayExpiringCountMap
    val ongoingGiveaways = calendarSummary.ongoingGiveaways
    val totalExpiringIn14Days = calendarSummary.totalExpiringIn14Days
    val totalValueExpiring14Days = calendarSummary.totalValueExpiring14Days

    val filteredTimelineItems by remember(calendarSummary, selectedCalendarDate, selectedScope) {
        derivedStateOf {
            val pickedDate = selectedCalendarDate
            if (pickedDate != null) {
                calendarSummary.itemsByDate[pickedDate].orEmpty()
            } else {
                when (selectedScope) {
                    CalendarFilterScope.NEXT_14_DAYS -> calendarSummary.next14DaysItems
                    CalendarFilterScope.NEXT_7_DAYS -> calendarSummary.next7DaysItems
                    CalendarFilterScope.URGENT_48H -> calendarSummary.urgentItems
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_pull_to_refresh_box")
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = 6.dp, bottom = 104.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("calendar_timeline_list")
        ) {
            // Header: Calendar Expiration Summary Hero Card (scrolls away cleanly AND can be tucked compactly)
            item(key = "calendar_hero_banner") {
                CalendarHeroBanner(
                    totalExpiringCount = totalExpiringIn14Days,
                    totalValueUsd = totalValueExpiring14Days,
                    isCompact = isCalendarHeaderCompact,
                    onToggleCompact = { isCalendarHeaderCompact = !isCalendarHeaderCompact },
                    reducedMotion = reducedMotion
                )
            }

            // Interactive Horizontal Day-Strip Calendar Bar
            item(key = "calendar_day_strip") {
                AnimatedVisibility(
                    visible = !isCalendarHeaderCompact,
                    enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
                    exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.SPANISH -> "CALENDARIO DE EXPIRACIÓN • PRÓX. 14 DÍAS"
                                AppLanguage.FRENCH -> "CALENDRIER D'EXPIRATION • 14 PROCH. JOURS"
                                AppLanguage.ENGLISH -> "EXPIRATION CALENDAR • NEXT 14 DAYS"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        if (selectedCalendarDate != null) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.SPANISH -> "Limpiar Fecha"
                                    AppLanguage.FRENCH -> "Effacer la Date"
                                    AppLanguage.ENGLISH -> "Clear Date Filter"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonCyan,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedCalendarDate = null }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_day_strip_row")
                    ) {
                        // "All" pill button
                        item {
                            CalendarDayChip(
                                label = when (lang) {
                                    AppLanguage.SPANISH -> "TODO"
                                    AppLanguage.FRENCH -> "TOUT"
                                    AppLanguage.ENGLISH -> "ALL"
                                },
                                dateNumber = when (lang) {
                                    AppLanguage.FRENCH -> "14J"
                                    else -> "14D"
                                },
                                month = when (lang) {
                                    AppLanguage.SPANISH -> "RANGO"
                                    AppLanguage.FRENCH -> "PLAGE"
                                    AppLanguage.ENGLISH -> "RANGE"
                                },
                                expiringCount = totalExpiringIn14Days,
                                isSelected = selectedCalendarDate == null,
                                reducedMotion = reducedMotion,
                                tagKey = "all",
                                onClick = { selectedCalendarDate = null }
                            )
                        }

                        // Day pills for each upcoming day
                        items(upcomingDays) { day ->
                            val daysBetween = ChronoUnit.DAYS.between(today, day)
                            val enLabel = when (daysBetween) {
                                0L -> "TODAY"
                                1L -> "TOM"
                                else -> day.dayOfWeek.name.take(3)
                            }
                            val label = when (lang) {
                                AppLanguage.ENGLISH -> enLabel
                                AppLanguage.SPANISH -> when (daysBetween) {
                                    0L -> "HOY"
                                    1L -> "MAÑ"
                                    else -> day.dayOfWeek.getDisplayName(TextStyle.SHORT, CALENDAR_LOCALE_ES)
                                        .replace(".", "").uppercase().take(3)
                                }
                                AppLanguage.FRENCH -> when (daysBetween) {
                                    0L -> "AUJ."
                                    1L -> "DEM"
                                    else -> day.dayOfWeek.getDisplayName(TextStyle.SHORT, CALENDAR_LOCALE_FR)
                                        .replace(".", "").uppercase().take(3)
                                }
                            }
                            val count = dayExpiringCountMap[day] ?: 0
                            val monthStr = when (lang) {
                                AppLanguage.SPANISH -> day.month.getDisplayName(TextStyle.SHORT, CALENDAR_LOCALE_ES)
                                    .replace(".", "").uppercase().take(3)
                                AppLanguage.FRENCH -> day.month.getDisplayName(TextStyle.SHORT, CALENDAR_LOCALE_FR)
                                    .replace(".", "").uppercase().take(3)
                                AppLanguage.ENGLISH -> day.month.name.take(3)
                            }

                            CalendarDayChip(
                                label = label,
                                dateNumber = day.dayOfMonth.toString(),
                                month = monthStr,
                                expiringCount = count,
                                isSelected = selectedCalendarDate == day,
                                isUrgent = daysBetween <= 1L && count > 0,
                                reducedMotion = reducedMotion,
                                tagKey = enLabel.lowercase(),
                                onClick = {
                                    selectedCalendarDate = if (selectedCalendarDate == day) null else day
                                }
                            )
                        }
                    }
                    }
                }
            }

            // Scope filter chips (when viewing all upcoming days)
            item(key = "calendar_scope_chips") {
                AnimatedVisibility(
                    visible = selectedCalendarDate == null,
                    enter = ExpressiveMotion.expressiveExpandEnter(reducedMotion),
                    exit = ExpressiveMotion.expressiveCollapseExit(reducedMotion)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalendarFilterScope.entries.forEach { scope ->
                            val isSelected = selectedScope == scope
                            val chipCorner = rememberExpressiveCornerRadius(
                                isSelected = isSelected,
                                baseRadius = 10.dp,
                                selectedRadius = 16.dp,
                                enabled = !reducedMotion
                            )
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedScope = scope },
                                label = {
                                    Text(
                                        text = scope.localizedLabel(s),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(chipCorner),
                                modifier = Modifier.testTag("calendar_scope_${scope.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Results count header
            item(key = "calendar_results_header") {
                val headerTitle = if (selectedCalendarDate != null) {
                    val dateLocale = when (lang) {
                        AppLanguage.SPANISH -> CALENDAR_LOCALE_ES
                        AppLanguage.FRENCH -> CALENDAR_LOCALE_FR
                        AppLanguage.ENGLISH -> Locale.ENGLISH
                    }
                    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", dateLocale)
                    when (lang) {
                        AppLanguage.SPANISH -> "Expiran el ${selectedCalendarDate!!.format(formatter)}"
                        AppLanguage.FRENCH -> "Expirent le ${selectedCalendarDate!!.format(formatter)}"
                        AppLanguage.ENGLISH -> "Expiring on ${selectedCalendarDate!!.format(formatter)}"
                    }
                } else {
                    when (lang) {
                        AppLanguage.SPANISH -> "Expiraciones (${selectedScope.localizedLabel(s)})"
                        AppLanguage.FRENCH -> "Expirations (${selectedScope.localizedLabel(s)})"
                        AppLanguage.ENGLISH -> "${selectedScope.localizedLabel(s)} Expirations"
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = when (lang) {
                            AppLanguage.SPANISH -> "${filteredTimelineItems.size} juegos"
                            AppLanguage.FRENCH -> "${filteredTimelineItems.size} jeux"
                            AppLanguage.ENGLISH -> "${filteredTimelineItems.size} games"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state for selected date / scope
            if (filteredTimelineItems.isEmpty()) {
                item(key = "calendar_empty_state") {
                    CalendarEmptyState(
                        selectedDate = selectedCalendarDate,
                        onResetFilter = {
                            selectedCalendarDate = null
                            selectedScope = CalendarFilterScope.NEXT_14_DAYS
                        }
                    )
                }
            } else {
                // Render Giveaway cards for expiring games with M3 Expressive placement springs
                items(
                    items = filteredTimelineItems,
                    key = { "cal_giveaway_${it.id}" }
                ) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!reducedMotion) {
                                    Modifier.animateItem(
                                        fadeInSpec = ExpressiveMotion.effectsDefault(),
                                        placementSpec = ExpressivePlacementSpec,
                                        fadeOutSpec = ExpressiveMotion.effectsFast()
                                    )
                                } else Modifier
                            )
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        Column {
                            // Urgency countdown indicator ribbon above the card
                            val badgeText = ExpirationCalendarUtils.getExpirationBadgeText(item.endDate, s = s)
                            val daysLeft = ExpirationCalendarUtils.daysRemaining(item.endDate, today)
                            val isUrgent = daysLeft != null && daysLeft <= 1L

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isUrgent) Icons.Default.LocalFireDepartment else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (isUrgent) CalendarNeonAmber else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = badgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUrgent) CalendarNeonAmber else MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (item.numericWorth > 0) {
                                    Text(
                                        text = "${s.home.badgeWorthPrefix}: ${item.worth}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            GiveawayCard(
                                item = item,
                                cardDensity = cardDensity,
                                showOriginalPrice = isShowOriginalPrice,
                                showExpiration = isShowExpiration,
                                onClick = { onItemClick(item) },
                                onClaimClick = { onClaimClick(item) },
                                onToggleBookmark = { onToggleBookmark(item) },
                                onToggleClaimed = { onToggleClaimed(item) },
                                isAnimationsEnabled = isAnimationsEnabled && !isLowEndOptimization,
                                isLowEndOptimization = isLowEndOptimization,
                                beamAngle = beamAngle
                            )
                        }
                    }
                }
            }

            // Ongoing giveaways collapsible section (games without fixed end date, so user misses nothing)
            if (ongoingGiveaways.isNotEmpty() && selectedCalendarDate == null) {
                item(key = "calendar_ongoing_section") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("ongoing_giveaways_toggle_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showOngoingGiveaways = !showOngoingGiveaways },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.SPANISH -> "Juegos Sin Fecha Límite (${ongoingGiveaways.size})"
                                                AppLanguage.FRENCH -> "Offres Sans Date Limite (${ongoingGiveaways.size})"
                                                AppLanguage.ENGLISH -> "Ongoing Giveaways (${ongoingGiveaways.size})"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.SPANISH -> "Promociones activas hasta que se agoten las claves"
                                                AppLanguage.FRENCH -> "Promotions actives jusqu'à épuisement des clés"
                                                AppLanguage.ENGLISH -> "Active promotions available until publisher keys run out"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val chevronRotation = if (reducedMotion) {
                                    if (showOngoingGiveaways) 180f else 0f
                                } else {
                                    animateFloatAsState(
                                        targetValue = if (showOngoingGiveaways) 180f else 0f,
                                        animationSpec = ExpressiveMotion.spatialFast(false),
                                        label = "ongoing_chevron"
                                    ).value
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (showOngoingGiveaways) {
                                            when (lang) {
                                                AppLanguage.SPANISH -> "Ocultar"
                                                AppLanguage.FRENCH -> "Masquer"
                                                AppLanguage.ENGLISH -> "Hide"
                                            }
                                        } else {
                                            when (lang) {
                                                AppLanguage.SPANISH -> "Ver"
                                                AppLanguage.FRENCH -> "Voir"
                                                AppLanguage.ENGLISH -> "Show"
                                            }
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .graphicsLayer { rotationZ = chevronRotation }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showOngoingGiveaways) {
                    items(
                        items = ongoingGiveaways,
                        key = { "cal_ongoing_${it.id}" }
                    ) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (!reducedMotion) {
                                        Modifier.animateItem(
                                            fadeInSpec = ExpressiveMotion.effectsDefault(),
                                            placementSpec = ExpressivePlacementSpec,
                                            fadeOutSpec = ExpressiveMotion.effectsFast()
                                        )
                                    } else Modifier
                                )
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            GiveawayCard(
                                item = item,
                                cardDensity = cardDensity,
                                showOriginalPrice = isShowOriginalPrice,
                                showExpiration = false,
                                onClick = { onItemClick(item) },
                                onClaimClick = { onClaimClick(item) },
                                onToggleBookmark = { onToggleBookmark(item) },
                                onToggleClaimed = { onToggleClaimed(item) },
                                isAnimationsEnabled = isAnimationsEnabled && !isLowEndOptimization,
                                isLowEndOptimization = isLowEndOptimization,
                                beamAngle = beamAngle
                            )
                        }
                    }
                }
            }
        }
    }
}
