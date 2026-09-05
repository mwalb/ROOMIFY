package org.com.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Long? = null,
    val userId: Long? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val roomId: Long? = null,
    val roomTitle: String? = null,
    val status: String = "PENDING",
    val totalPrice: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val numberOfGuests: Int = 1,
    val specialRequests: String? = null,
    val createdAt: String? = null
)
