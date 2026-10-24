package com.example.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GiveawayItem
import com.example.preferences.CardDensity
import com.example.ui.components.GiveawayCard
import com.example.ui.density.ScreenDensityInfo
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val NeonAmber = Color(0xFFFFB300)

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
    val today = remember { LocalDate.now() }
    val upcomingDays = remember(today) {
        ExpirationCalendarUtils.getUpcomingCalendarDays(today, daysCount = 15)
    }

    // Selected day from day-strip picker: null means "All Days in scope"
    var selectedCalendarDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedScope by remember { mutableStateOf(CalendarFilterScope.NEXT_14_DAYS) }
    var showOngoingGiveaways by remember { mutableStateOf(false) }

    // Map each day to the count of giveaways expiring on that day
    val dayExpiringCountMap = remember(giveaways, today) {
        val map = mutableMapOf<LocalDate, Int>()
        for (day in upcomingDays) {
            map[day] = giveaways.count { item ->
                ExpirationCalendarUtils.isExpiringOnDate(item.endDate, day)
            }
        }
        map
    }

    // Filter giveaways according to selected day or scope
    val filteredTimelineItems by remember(giveaways, selectedCalendarDate, selectedScope, today) {
        derivedStateOf {
            if (selectedCalendarDate != null) {
                giveaways.filter { ExpirationCalendarUtils.isExpiringOnDate(it.endDate, selectedCalendarDate!!) }
            } else {
                when (selectedScope) {
                    CalendarFilterScope.NEXT_14_DAYS -> {
                        giveaways.filter {
                            ExpirationCalendarUtils.isExpiringInDayRange(it.endDate, 0, 14, today)
                        }
                    }
                    CalendarFilterScope.NEXT_7_DAYS -> {
                        giveaways.filter {
                            ExpirationCalendarUtils.isExpiringInDayRange(it.endDate, 0, 7, today)
                        }
                    }
                    CalendarFilterScope.URGENT_48H -> {
                        giveaways.filter {
                            ExpirationCalendarUtils.isExpiringInDayRange(it.endDate, 0, 1, today)
                        }
                    }
                }
            }
        }
    }

    // Ongoing giveaways (no set expiration date, e.g. "N/A" or unlimited until keys run out)
    val ongoingGiveaways by remember(giveaways) {
        derivedStateOf {
            giveaways.filter {
                it.endDate.isBlank() || it.endDate.equals("N/A", ignoreCase = true)
            }
        }
    }

    // Metrics for the summary card
    val totalExpiringIn14Days = remember(giveaways, today) {
        giveaways.count { ExpirationCalendarUtils.isExpiringInDayRange(it.endDate, 0, 14, today) }
    }
    val totalValueExpiring14Days = remember(giveaways, today) {
        giveaways.filter { ExpirationCalendarUtils.isExpiringInDayRange(it.endDate, 0, 14, today) }
            .sumOf { it.numericWorth }
    }
    val urgentCount = remember(giveaways, today) {
        giveaways.count { ExpirationCalendarUtils.isExpiringInDayRange(it.endDate, 0, 1, today) }
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
            contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("calendar_timeline_list")
        ) {
            // Header: Calendar Expiration Summary Hero Card
            item(key = "calendar_hero_banner") {
                CalendarHeroBanner(
                    totalExpiringCount = totalExpiringIn14Days,
                    totalValueUsd = totalValueExpiring14Days,
                    urgentCount = urgentCount,
                    isLowEndOptimization = isLowEndOptimization
                )
            }

            // Interactive Horizontal Day-Strip Calendar Bar
            item(key = "calendar_day_strip") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXPIRATION CALENDAR • NEXT 14 DAYS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )

                        if (selectedCalendarDate != null) {
                            Text(
                                text = "Clear Date Filter",
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
                                label = "ALL",
                                dateNumber = "14D",
                                month = "RANGE",
                                expiringCount = totalExpiringIn14Days,
                                isSelected = selectedCalendarDate == null,
                                onClick = { selectedCalendarDate = null }
                            )
                        }

                        // Day pills for each upcoming day
                        items(upcomingDays) { day ->
                            val daysBetween = ChronoUnit.DAYS.between(today, day)
                            val label = when (daysBetween) {
                                0L -> "TODAY"
                                1L -> "TOM"
                                else -> day.dayOfWeek.name.take(3)
                            }
                            val count = dayExpiringCountMap[day] ?: 0
                            val monthStr = day.month.name.take(3)

                            CalendarDayChip(
                                label = label,
                                dateNumber = day.dayOfMonth.toString(),
                                month = monthStr,
                                expiringCount = count,
                                isSelected = selectedCalendarDate == day,
                                isUrgent = daysBetween <= 1L && count > 0,
                                onClick = {
                                    selectedCalendarDate = if (selectedCalendarDate == day) null else day
                                }
                            )
                        }
                    }
                }
            }

            // Scope filter chips (when viewing all upcoming days)
            if (selectedCalendarDate == null) {
                item(key = "calendar_scope_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CalendarFilterScope.values().forEach { scope ->
                            val isSelected = selectedScope == scope
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedScope = scope },
                                label = {
                                    Text(
                                        text = scope.label,
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("calendar_scope_${scope.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Results count header
            item(key = "calendar_results_header") {
                val headerTitle = if (selectedCalendarDate != null) {
                    val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                    "Expiring on ${selectedCalendarDate!!.format(formatter)}"
                } else {
                    "${selectedScope.label} Expirations"
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "${filteredTimelineItems.size} games",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
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
                // Render Giveaway cards for expiring games
                items(
                    items = filteredTimelineItems,
                    key = { "cal_giveaway_${it.id}" }
                ) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        Column {
                            // Urgency countdown indicator ribbon above the card
                            val badgeText = ExpirationCalendarUtils.getExpirationBadgeText(item.endDate)
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
                                        tint = if (isUrgent) NeonAmber else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = badgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUrgent) NeonAmber else MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (item.numericWorth > 0) {
                                    Text(
                                        text = "Value: ${item.worth}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted
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
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                            text = "Ongoing Giveaways (${ongoingGiveaways.size})",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Active promotions available until publisher keys run out",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = if (showOngoingGiveaways) "Hide" else "Show",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
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

/**
 * Top summary banner for the Calendar Expiration tab.
 */
@Composable
private fun CalendarHeroBanner(
    totalExpiringCount: Int,
    totalValueUsd: Double,
    urgentCount: Int,
    isLowEndOptimization: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val bannerBrush = remember(primaryColor, secondaryColor) {
        Brush.horizontalGradient(
            listOf(
                primaryColor.copy(alpha = 0.22f),
                secondaryColor.copy(alpha = 0.12f),
                Color(0xFF1E2538)
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("calendar_hero_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24)),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBrush)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "EXPIRING GAMES RADAR",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Next week or two of upcoming game drops & giveaways",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Total Value Expiring Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "$%.2f FREE", totalValueUsd),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatPill(
                        label = "Expiring in 14 Days",
                        value = "$totalExpiringCount Games",
                        color = primaryColor,
                        modifier = Modifier.weight(1f)
                    )

                    StatPill(
                        label = "Urgent (≤ 48h)",
                        value = if (urgentCount > 0) "$urgentCount Ending" else "None",
                        color = if (urgentCount > 0) NeonAmber else TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (urgentCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonAmber.copy(alpha = 0.12f))
                            .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$urgentCount game${if (urgentCount > 1) "s" else ""} ending in under 48 hours! Claim now before keys run out.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E2330),
        border = BorderStroke(1.dp, Color(0xFF2E384D)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Individual day chip button in the horizontal day-strip.
 */
@Composable
private fun CalendarDayChip(
    label: String,
    dateNumber: String,
    month: String,
    expiringCount: Int,
    isSelected: Boolean,
    isUrgent: Boolean = false,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> primaryColor
            isUrgent -> NeonAmber.copy(alpha = 0.8f)
            expiringCount > 0 -> primaryColor.copy(alpha = 0.45f)
            else -> Color(0xFF2A3245)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "calendar_chip_border"
    )

    val containerColor = when {
        isSelected -> primaryColor.copy(alpha = 0.22f)
        isUrgent -> NeonAmber.copy(alpha = 0.12f)
        else -> Color(0xFF1A1F2C)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor),
        modifier = Modifier
            .width(62.dp)
            .height(78.dp)
            .testTag("calendar_chip_${label.lowercase()}_$dateNumber")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) primaryColor else if (isUrgent) NeonAmber else TextMuted,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) primaryColor else TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (expiringCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isUrgent) NeonAmber else primaryColor)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$expiringCount",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0B0F19)
                    )
                }
            } else {
                Text(
                    text = month,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }
        }
    }
}

/**
 * Empty state when no games expire on the selected date or period.
 */
@Composable
private fun CalendarEmptyState(
    selectedDate: LocalDate?,
    onResetFilter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("calendar_empty_state_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161A24)),
        border = BorderStroke(1.dp, Color(0xFF2A3245))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (selectedDate != null) "No Expirations on this Date" else "No Expirations in Selected Range",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (selectedDate != null) {
                    "No free games are scheduled to end on this day. Check other upcoming dates or view all next 14 days!"
                } else {
                    "No giveaways have scheduled end dates in this window. Games may be ongoing until keys are exhausted."
                },
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onResetFilter,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "View All 14 Days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B0F19)
                )
            }
        }
    }
}
