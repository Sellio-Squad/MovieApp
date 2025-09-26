package com.karrar.movieapp.utilities

import java.text.SimpleDateFormat
import java.util.Locale

object DateFormatter {

    /**
     * Converts date from "dd/MM/yyyy" format to "yyyy,MMM dd" format
     * Example: "24/12/2023" -> "2023,Dec 24"
     */
    fun toUiDate(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("yyyy,MMM dd", Locale.US)
            val date = inputFormat.parse(inputDate)
            date?.let { outputFormat.format(it) } ?: inputDate
        } catch (e: Exception) {
            inputDate
        }
    }

    /**
     * Converts minutes to hours and minutes format
     * Example: 135 -> "2h 15m"
     */
    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}h ${mins}m"
    }

    /**
     * Converts date from one format to another
     * @param inputDate The date string to format
     * @param inputPattern The pattern of the input date
     * @param outputPattern The desired output pattern
     * @param locale The locale to use (default: Locale.US)
     */
    fun formatDate(
        inputDate: String,
        inputPattern: String,
        outputPattern: String,
        locale: Locale = Locale.US
    ): String {
        return try {
            val inputFormat = SimpleDateFormat(inputPattern, locale)
            val outputFormat = SimpleDateFormat(outputPattern, locale)
            val date = inputFormat.parse(inputDate)
            date?.let { outputFormat.format(it) } ?: inputDate
        } catch (e: Exception) {
            inputDate
        }
    }

    /**
     * Converts date from API format to UI display format
     * Example: "2025-07-31" -> "Jul 31, 2025"
     */
    fun formatApiDate(dateString: String): String {
        return formatDate(
            inputDate = dateString,
            inputPattern = "yyyy-MM-dd",
            outputPattern = "MMM dd, yyyy"
        )
    }
}