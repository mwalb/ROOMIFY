package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class AreaBookingAnalytics(

    @SerialName("area")
    val area: String = "",

    @SerialName("bookingCount")
    val bookingCount: Long = 0L,

    @SerialName("averagePrice")
    val averagePrice: Double = 0.0,

    @SerialName("occupancyRate")
    val occupancyRate: Double = 0.0
) {

    // Pure Kotlin formatting for KMP commonMain
    fun getFormattedAveragePrice(): String {
        val rounded = (averagePrice * 100).roundToInt() / 100.0
        return "TSh $rounded"
    }

    fun getFormattedOccupancyRate(): String {
        val percent = (occupancyRate * 1000).roundToInt() / 10.0
        return "$percent%"
    }

    fun getBookingCountFormatted(): String {
        return when (bookingCount) {
            0L -> "No bookings"
            1L -> "1 booking"
            else -> "$bookingCount bookings"
        }
    }

    fun getOccupancyStatus(): String {
        return when {
            occupancyRate >= 0.9 -> "🔥 Very High"
            occupancyRate >= 0.7 -> "📈 High"
            occupancyRate >= 0.4 -> "📊 Medium"
            occupancyRate >= 0.2 -> "📉 Low"
            else -> "❄️ Very Low"
        }
    }

    fun isPopular(): Boolean = bookingCount > 100

    fun isHighOccupancy(): Boolean = occupancyRate > 0.8

    fun isLowOccupancy(): Boolean = occupancyRate < 0.3

    companion object {
        fun empty() = AreaBookingAnalytics()
    }
}