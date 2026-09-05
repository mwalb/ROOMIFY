package org.com.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class PriceRangeAnalytics(
    @JsonNames("priceRange")
    val priceRange: String = "",

    @JsonNames("bookingCount")
    val bookingCount: Long = 0L,

    @JsonNames("averagePrice")
    val averagePrice: Double = 0.0
)