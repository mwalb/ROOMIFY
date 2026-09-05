package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(

    @SerialName("userId")
    val userId: Long = 0L,

    @SerialName("title")
    val title: String = "",

    @SerialName("message")
    val message: String = "",

    @SerialName("bookingId")
    val bookingId: String? = null
)

