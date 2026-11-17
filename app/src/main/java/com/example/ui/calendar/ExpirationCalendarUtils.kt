package com.example.ui.calendar

import com.example.data.model.GiveawayItem
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.i18n.StringsEn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Calendar and expiration date calculation utilities for LootDrop.
 * Specializes in identifying, filtering, and grouping giveaways expiring
 * in the upcoming week or two (7 to 14 days).
 */
object ExpirationCalendarUtils {

    private const val MAX_DATE_CACHE = 256
    private val cacheLock = Any()
    private val LOCALE_ES: Locale = Locale.forLanguageTag("es")
    private val INVALID_DATE_SENTINEL: LocalDate = LocalDate.MIN
    private val PARSED_DATE_CACHE = object : LinkedHashMap<String, LocalDate>(64, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LocalDate>?): Boolean = size > MAX_DATE_CACHE
    }

    fun clearCache() {
        synchronized(cacheLock) {
            PARSED_DATE_CACHE.clear()
        }
    }

    /**
     * Parses the expiration date string from GamerPower API (e.g. "2026-10-25 15:00:00" or "2026-10-25").
     * Returns null if date is "N/A", blank, or invalid.
     */
    fun parseExpirationDate(endDateStr: String?): LocalDate? {
        if (endDateStr.isNullOrBlank() || endDateStr.equals("N/A", ignoreCase = true)) return null
        val trimmed = endDateStr.trim()
        synchronized(cacheLock) {
            PARSED_DATE_CACHE[trimmed]?.let { cached ->
                return if (cached === INVALID_DATE_SENTINEL) null else cached
            }
        }
        val parsed = try {
            val sep = trimmed.indexOfAny(charArrayOf(' ', 'T'))
            val datePart = if (sep > 0) trimmed.substring(0, sep) else trimmed
            LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
        synchronized(cacheLock) {
            PARSED_DATE_CACHE[trimmed] = parsed ?: INVALID_DATE_SENTINEL
        }
        return parsed
    }

    /**
     * Returns the number of days remaining until expiration relative to [fromDate].
     * Returns null if no expiration date is set.
     */
    fun daysRemaining(endDateStr: String?, fromDate: LocalDate = LocalDate.now()): Long? {
        val target = parseExpirationDate(endDateStr) ?: return null
        return ChronoUnit.DAYS.between(fromDate, target)
    }

    /**
     * Checks if a giveaway expires within the specified day window [minDays..maxDays].
     */
    fun isExpiringInDayRange(
        endDateStr: String?,
        minDays: Long = 0,
        maxDays: Long = 14,
        fromDate: LocalDate = LocalDate.now()
    ): Boolean {
        val days = daysRemaining(endDateStr, fromDate) ?: return false
        return days in minDays..maxDays
    }

    /**
     * Checks if a giveaway expires on the exact given [targetDate].
     */
    fun isExpiringOnDate(endDateStr: String?, targetDate: LocalDate): Boolean {
        val date = parseExpirationDate(endDateStr) ?: return false
        return date == targetDate
    }

    /**
     * Human-readable, gaming-themed countdown string.
     */
    fun getExpirationBadgeText(
        endDateStr: String?,
        now: LocalDateTime = LocalDateTime.now(),
        s: AppStrings = StringsEn
    ): String {
        if (endDateStr.isNullOrBlank() || endDateStr.equals("N/A", ignoreCase = true)) {
            return s.home.expLimitedKeys
        }
        val isEs = s.language == AppLanguage.SPANISH
        return try {
            val trimmed = endDateStr.trim()
            val localDate = parseExpirationDate(trimmed) ?: return s.home.expEndsSoon
            val today = now.toLocalDate()
            val daysBetween = ChronoUnit.DAYS.between(today, localDate)

            val dayShort = if (isEs) {
                localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, LOCALE_ES).replace(".", "").uppercase()
            } else {
                localDate.dayOfWeek.name.take(3)
            }
            val monthShort = if (isEs) {
                localDate.month.getDisplayName(TextStyle.SHORT, LOCALE_ES).replace(".", "").uppercase()
            } else {
                localDate.month.name.take(3)
            }

            when {
                daysBetween < 0 -> s.home.expExpired
                daysBetween == 0L -> {
                    val sep = trimmed.indexOfAny(charArrayOf(' ', 'T'))
                    val timePart = if (sep > 0 && sep + 1 < trimmed.length) trimmed.substring(sep + 1) else null
                    if (timePart != null && timePart.contains(":")) {
                        val colon = timePart.indexOf(':')
                        val hour = timePart.substring(0, colon).toIntOrNull() ?: 23
                        val secondColon = timePart.indexOf(':', colon + 1)
                        val minStr = if (secondColon > 0) timePart.substring(colon + 1, secondColon) else timePart.substring(colon + 1)
                        val min = minStr.toIntOrNull() ?: 59
                        val targetTime = localDate.atTime(hour, min)
                        val hoursRemaining = ChronoUnit.HOURS.between(now, targetTime)
                        if (hoursRemaining in 1..24) {
                            s.home.expEndsInHoursFormat.format(hoursRemaining)
                        } else if (hoursRemaining <= 0) {
                            s.home.expEndingNow
                        } else {
                            s.home.expEndsToday
                        }
                    } else {
                        s.home.expEndsToday
                    }
                }
                daysBetween == 1L -> s.home.expEndsTomorrow
                daysBetween in 2..6 -> "${s.home.expEndsInDaysFormat.format(daysBetween)} ($dayShort)"
                daysBetween in 7..14 -> "${s.home.expEndsInDaysFormat.format(daysBetween)} ($monthShort ${localDate.dayOfMonth})"
                else -> s.home.expEndsOnDateFormat.format(monthShort, localDate.dayOfMonth)
            }
        } catch (_: Exception) {
            s.home.expEndsSoon
        }
    }

    /**
     * Generates a sequence of [daysCount] dates starting from [startDate].
     */
    fun getUpcomingCalendarDays(
        startDate: LocalDate = LocalDate.now(),
        daysCount: Int = 15
    ): List<LocalDate> {
        return (0 until daysCount).map { startDate.plusDays(it.toLong()) }
    }
}

/**
 * Filter scope for the Calendar expiration tab.
 */
enum class CalendarFilterScope(val label: String) {
    NEXT_14_DAYS("Next 14 Days"),
    NEXT_7_DAYS("Next 7 Days"),
    URGENT_48H("Urgent (≤ 48h)")
}

/**
 * Groups a list of giveaways into timeline categories for display.
 */
data class CalendarTimelineGroup(
    val title: String,
    val subtitle: String,
    val isUrgent: Boolean,
    val items: List<GiveawayItem>
)
