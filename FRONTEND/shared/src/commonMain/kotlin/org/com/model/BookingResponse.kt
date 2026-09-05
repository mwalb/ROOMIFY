package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class BookingResponse(

    @SerialName("id")
    val id: Long? = null,

    @SerialName("userId")
    val userId: Long? = null,

    @SerialName("roomId")
    val roomId: Long? = null,

    @SerialName("status")
    val status: String = "",

    @SerialName("totalPrice")
    val totalPrice: Double = 0.0,

    @SerialName("startDate")
    val startDate: String = "",

    @SerialName("endDate")
    val endDate: String = "",

    @SerialName("createdAt")
    val createdAt: String = "",

    @SerialName("roomTitle")
    val roomTitle: String = "",

    @SerialName("tenantName")
    val tenantName: String = "",

    @SerialName("tenantEmail")
    val tenantEmail: String = "",

    @SerialName("ownerName")
    val ownerName: String = "",

    @SerialName("ownerEmail")
    val ownerEmail: String = "",

    // Optional aliases coming from backend
    @SerialName("userName")
    val userName: String? = null,

    @SerialName("userEmail")
    val userEmail: String? = null
) {

    // ------------------------------------------------------------
    // ALIASES (same behavior as your Java getters)
    // ------------------------------------------------------------

    fun getDisplayUserName(): String {
        return userName ?: tenantName
    }

    fun getDisplayUserEmail(): String {
        return userEmail ?: tenantEmail
    }

    // ------------------------------------------------------------
    // FORMATTING HELPERS
    // ------------------------------------------------------------

    fun getFormattedPrice(): String {
        val rounded = (totalPrice * 100).roundToInt() / 100.0
        return "TSh $rounded"
    }

    fun getBookingPeriod(): String {
        return "$startDate - $endDate"
    }

    // ------------------------------------------------------------
    // STATUS HELPERS
    // ------------------------------------------------------------

    fun isPending(): Boolean = status.equals("PENDING", ignoreCase = true)

    fun isConfirmed(): Boolean = status.equals("CONFIRMED", ignoreCase = true)

    fun isCancelled(): Boolean = status.equals("CANCELLED", ignoreCase = true)

    fun isCompleted(): Boolean = status.equals("COMPLETED", ignoreCase = true)

    companion object {
        fun empty() = BookingResponse()
    }
}