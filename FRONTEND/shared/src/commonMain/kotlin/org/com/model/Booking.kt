package org.com.model

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: Long? = null,
    val userId: Long? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val userPhone: String? = null,
    val roomId: Long? = null,
    val roomTitle: String? = null,
    val roomImageUrl: String? = null,
    val status: String = "PENDING",
    val totalPrice: Double = 0.0,
    val rentAmount: Double = 0.0,
    val facilitationFee: Double = 0.0,
    val listingType: String = "DIRECT_BY_OWNER",
    val dalaliId: Long? = null,
    val startDate: String = "",
    val endDate: String = "",
    val numberOfGuests: Int = 1,
    val specialRequests: String? = null,
    val bookingType: String = "RESERVATION", // VIEWING or RESERVATION
    val viewingTimeSlot: String? = null, // e.g. "Asubuhi (09:00 - 12:00)", "Mchana (13:00 - 16:00)", "Jioni (16:00 - 18:30)"
    val meetingPoint: String? = null, // e.g. "Kituo cha Mwendokasi cha Kimara"
    val bookingDate: String? = null,
    val createdAt: String? = null
)
