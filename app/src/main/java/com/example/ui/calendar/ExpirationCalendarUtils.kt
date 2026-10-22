package com.example.ui.calendar

import com.example.data.model.GiveawayItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Calendar and expiration date calculation utilities for LootDrop.
 * Specializes in identifying, filtering, and grouping giveaways expiring
 * in the upcoming week or two (7 to 14 days).
 */
object ExpirationCalendarUtils {

    /**
     * Parses the expiration date string from GamerPower API (e.g. "2026-10-25 15:00:00" or "2026-10-25").
     * Returns null if date is "N/A", blank, or invalid.
     */
    fun parseExpirationDate(endDateStr: String?): LocalDate? {
        if (endDateStr.isNullOrBlank() || endDateStr.equals("N/A", ignoreCase = true)) return null
        return try {
            val trimmed = endDateStr.trim()
            val datePart = trimmed.split(Regex("[ T]")).firstOrNull() ?: return null
            LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
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
    fun getExpirationBadgeText(endDateStr: String?, now: LocalDateTime = LocalDateTime.now()): String {
        if (endDateStr.isNullOrBlank() || endDateStr.equals("N/A", ignoreCase = true)) {
            return "Limited Keys"
        }
        return try {
            val trimmed = endDateStr.trim()
            val parts = trimmed.split(Regex("[ T]"))
            val datePart = parts.firstOrNull() ?: return "Expiring"
            val localDate = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
            val today = now.toLocalDate()
            val daysBetween = ChronoUnit.DAYS.between(today, localDate)

            when {
                daysBetween < 0 -> "Expired"
                daysBetween == 0L -> {
                    if (parts.size > 1 && parts[1].contains(":")) {
                        val timeParts = parts[1].split(":")
                        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 23
                        val min = timeParts.getOrNull(1)?.toIntOrNull() ?: 59
                        val targetTime = localDate.atTime(hour, min)
                        val hoursRemaining = ChronoUnit.HOURS.between(now, targetTime)
                        if (hoursRemaining in 1..24) {
                            "Ends in ${hoursRemaining}h"
                        } else if (hoursRemaining <= 0) {
                            "Ending now"
                        } else {
                            "Ends Today"
                        }
                    } else {
                        "Ends Today"
                    }
                }
                daysBetween == 1L -> "Ends Tomorrow"
                daysBetween in 2..6 -> "Ends in $daysBetween days (${localDate.dayOfWeek.name.take(3)})"
                daysBetween in 7..14 -> "Ends in $daysBetween days (${localDate.month.name.take(3)} ${localDate.dayOfMonth})"
                else -> "Ends ${localDate.month.name.take(3)} ${localDate.dayOfMonth}"
            }
        } catch (_: Exception) {
            "Ends soon"
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
    URGENT_48H("Urgent (≤ 48h)"),
    ALL_UPCOMING("All Upcoming")
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
