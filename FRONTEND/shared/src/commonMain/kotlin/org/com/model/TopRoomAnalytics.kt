package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopRoomAnalytics(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("title")
    val title: String = "",

    @SerialName("revenue")
    val revenue: Double = 0.0,

    @SerialName("bookingsCount")
    val bookingsCount: Long = 0L
)
