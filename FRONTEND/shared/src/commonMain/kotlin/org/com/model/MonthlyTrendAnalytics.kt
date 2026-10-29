package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonthlyTrendAnalytics(
    @SerialName("month")
    val month: String = "",

    @SerialName("bookingCount")
    val bookingCount: Long = 0L,

    @SerialName("totalRevenue")
    val totalRevenue: Double = 0.0
)
