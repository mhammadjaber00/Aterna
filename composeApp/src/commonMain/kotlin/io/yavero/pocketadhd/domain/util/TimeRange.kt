package io.yavero.pocketadhd.domain.util

import kotlinx.datetime.*

object TimeRange {

    fun todayRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val today = Clock.System.todayIn(timeZone)
        val todayStart = today.atStartOfDayIn(timeZone)
        val todayEnd = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return todayStart to todayEnd
    }

    fun dayRange(date: LocalDate, timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val dayStart = date.atStartOfDayIn(timeZone)
        val dayEnd = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return dayStart to dayEnd
    }

    fun isToday(instant: Instant, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
        val (todayStart, todayEnd) = todayRange(timeZone)
        return instant >= todayStart && instant <= todayEnd
    }

    fun thisWeekRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val today = Clock.System.todayIn(timeZone)
        val daysFromMonday = today.dayOfWeek.ordinal 
        val monday = today.minus(daysFromMonday, DateTimeUnit.DAY)
        val sunday = monday.plus(6, DateTimeUnit.DAY)

        val weekStart = monday.atStartOfDayIn(timeZone)
        val weekEnd = sunday.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return weekStart to weekEnd
    }

    fun thisMonthRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): Pair<Instant, Instant> {
        val today = Clock.System.todayIn(timeZone)
        val firstOfMonth = LocalDate(today.year, today.month, 1)
        val lastOfMonth = firstOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

        val monthStart = firstOfMonth.atStartOfDayIn(timeZone)
        val monthEnd = lastOfMonth.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).minus(1, DateTimeUnit.MILLISECOND)

        return monthStart to monthEnd
    }
}