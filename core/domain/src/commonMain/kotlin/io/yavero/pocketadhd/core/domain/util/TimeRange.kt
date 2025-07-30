package io.yavero.pocketadhd.core.domain.util

import kotlinx.datetime.*

/**
 * Utility functions for handling time ranges, particularly for "today" calculations
 * that work correctly across time zones and handle DST transitions.
 */
object TimeRange {

    /**
     * Returns the start and end of today in the given timezone.
     *
     * @param timeZone The timezone to use for "today" calculation
     * @return Pair of (todayStart, todayEnd) as Instants
     */
    fun todayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val today = Clock.System.todayIn(timeZone)
        val todayStart = today.atStartOfDayIn(timeZone)
        val todayEnd = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return todayStart to todayEnd
    }

    /**
     * Returns the start and end of a specific date in the given timezone.
     *
     * @param date The date to get the range for
     * @param timeZone The timezone to use
     * @return Pair of (dayStart, dayEnd) as Instants
     */
    fun dayRange(date: LocalDate, timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val dayStart = date.atStartOfDayIn(timeZone)
        val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return dayStart to dayEnd
    }

    /**
     * Checks if an instant falls within today's range in the given timezone.
     *
     * @param instant The instant to check
     * @param timeZone The timezone to use for "today" calculation
     * @return true if the instant is within today's range
     */
    fun isToday(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val (todayStart, todayEnd) = todayRange(timeZone)
        return instant >= todayStart && instant <= todayEnd
    }

    /**
     * Returns the start and end of the current week (Monday to Sunday) in the given timezone.
     *
     * @param timeZone The timezone to use
     * @return Pair of (weekStart, weekEnd) as Instants
     */
    fun thisWeekRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val today = Clock.System.todayIn(timeZone)
        val daysFromMonday = today.dayOfWeek.ordinal // Monday = 0, Sunday = 6
        val monday = today.minus(daysFromMonday, DateTimeUnit.DAY)
        val sunday = monday.plus(6, DateTimeUnit.DAY)

        val weekStart = monday.atStartOfDayIn(timeZone)
        val weekEnd = sunday.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return weekStart to weekEnd
    }

    /**
     * Returns the start and end of the current month in the given timezone.
     *
     * @param timeZone The timezone to use
     * @return Pair of (monthStart, monthEnd) as Instants
     */
    fun thisMonthRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val today = Clock.System.todayIn(timeZone)
        val firstOfMonth = LocalDate(today.year, today.month, 1)
        val lastOfMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

        val monthStart = firstOfMonth.atStartOfDayIn(timeZone)
        val monthEnd = lastOfMonth.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return monthStart to monthEnd
    }
}