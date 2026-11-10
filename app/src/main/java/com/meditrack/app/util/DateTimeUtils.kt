package com.meditrack.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Central date/time utilities for formatting and parsing across the app.
 * Uses ISO format yyyy-MM-dd for database consistency.
 */
object DateTimeUtils {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val displayTimeFmt = SimpleDateFormat("h:mm a", Locale.getDefault()) // e.g. "2:30 PM"
    private val dayMonthFmt = SimpleDateFormat("d MMM", Locale.getDefault())

    fun todayIso(): String = dateFmt.format(Date())

    fun formatDate(date: Date): String = dateFmt.format(date)
    fun parseDate(iso: String): Date = dateFmt.parse(iso) ?: Date()

    fun formatDisplayDate(iso: String): String {
        val date = parseDate(iso)
        return displayDateFmt.format(date)
    }

    fun formatTime(timeStr: String): String {
        // Convert "HH:mm" to display format "h:mm a"
        return try {
            val parsed = timeFmt.parse(timeStr)
            if (parsed != null) displayTimeFmt.format(parsed) else timeStr
        } catch (e: Exception) {
            timeStr
        }
    }

    fun formatDayMonth(iso: String): String {
        val date = parseDate(iso)
        return dayMonthFmt.format(date)
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    fun getTomorrowIso(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return dateFmt.format(cal.time)
    }

    fun getNDaysAgoIso(n: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -n)
        return dateFmt.format(cal.time)
    }

    fun getCurrentTime(): String = timeFmt.format(Date())

    fun isTimeInFuture(timeStr: String): Boolean {
        return try {
            val cal = Calendar.getInstance()
            cal.time = timeFmt.parse(timeStr) ?: return false
            val now = Calendar.getInstance()
            cal.set(Calendar.YEAR, now.get(Calendar.YEAR))
            cal.set(Calendar.MONTH, now.get(Calendar.MONTH))
            cal.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR))
            cal.after(now)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the number of days from [fromIso] to [toIso].
     * Negative = [toIso] is in the past relative to [fromIso].
     */
    fun daysBetween(fromIso: String, toIso: String): Int {
        return try {
            val from = parseDate(fromIso).time
            val to = parseDate(toIso).time
            ((to - from) / (1000L * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            Int.MAX_VALUE
        }
    }

    /**
     * Returns the number of days until [expiryIso] from today.
     * Negative means already expired.
     */
    fun daysUntilExpiry(expiryIso: String): Int = daysBetween(todayIso(), expiryIso)

    /**
     * Returns the ISO date N days from today.
     */
    fun getNDaysFromNow(n: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, n)
        return dateFmt.format(cal.time)
    }
}
