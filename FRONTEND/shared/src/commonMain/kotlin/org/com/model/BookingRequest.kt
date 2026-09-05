package org.com.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Serializable
data class BookingRequest(

    // ==================== IDENTIFICATION ====================

    @SerialName("id")
    val id: Long? = null,

    @SerialName("userId")
    val userId: Long? = null,

    @SerialName("roomId")
    val roomId: Long? = null,

    // ==================== ROOM INFORMATION ====================

    @SerialName("roomTitle")
    val roomTitle: String = "Unknown Room",

    // ==================== USER INFORMATION ====================

    @SerialName("userName")
    val userName: String = "Unknown User",

    @SerialName("userPhone")
    val userPhone: String = "Not provided",

    // ==================== BOOKING STATUS ====================

    @SerialName("status")
    val status: String = "PENDING",

    @SerialName("bookingDate")
    val bookingDate: String = "",

    // ==================== BOOKING DETAILS ====================

    @SerialName("totalPrice")
    val totalPrice: Double = 0.0,

    @SerialName("startDate")
    val startDate: String = "",

    @SerialName("endDate")
    val endDate: String = "",

    @SerialName("numberOfGuests")
    val numberOfGuests: Int = 1,

    @SerialName("specialRequests")
    val specialRequests: String? = null

) {

    // =========================================================
    // STATUS HELPERS
    // =========================================================

    fun isPending(): Boolean =
        status.equals("PENDING", ignoreCase = true)

    fun isAccepted(): Boolean =
        status.equals("ACCEPTED", ignoreCase = true)

    fun isRejected(): Boolean =
        status.equals("REJECTED", ignoreCase = true)

    fun isCancelled(): Boolean =
        status.equals("CANCELLED", ignoreCase = true)

    fun getStatusBadge(): String =
        when {
            isPending() -> "⏳ Pending"
            isAccepted() -> "✓ Accepted"
            isRejected() -> "✕ Rejected"
            isCancelled() -> "⚠ Cancelled"
            else -> status
        }

    // =========================================================
    // PRICE
    // =========================================================

    fun getFormattedPrice(): String =
        "TSh ${totalPrice.toLong()}"

    // =========================================================
    // BOOKING DURATION
    // =========================================================

    fun getDurationDays(): Any {
        return try {
            if (startDate.isBlank() || endDate.isBlank()) {
                return 1
            }

            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)

            val duration = end.toEpochDays() - start.toEpochDays()

            if (duration > 0) duration else 1

        } catch (e: Exception) {
            1
        }
    }

    // =========================================================
    // BOOKING SUMMARY
    // =========================================================

    fun getBookingSummary(): String {
        val days = getDurationDays()

        return when (days) {
            1 -> "1 day • $numberOfGuests guest(s)"
            else -> "$days days • $numberOfGuests guest(s)"
        }
    }

    // =========================================================
    // CONVENIENCE
    // =========================================================

    fun isValid(): Boolean {
        return userId != null &&
                roomId != null &&
                startDate.isNotBlank() &&
                endDate.isNotBlank() &&
                totalPrice >= 0 &&
                numberOfGuests > 0
    }

    companion object {

        fun create(
            userId: Long,
            userName: String,
            roomId: Long,
            roomTitle: String,
            startDate: String,
            endDate: String,
            numberOfGuests: Int,
            totalPrice: Double,
            userPhone: String = "",
            specialRequests: String? = null
        ): BookingRequest {

            return BookingRequest(
                userId = userId,
                userName = userName,
                userPhone = userPhone,
                roomId = roomId,
                roomTitle = roomTitle,
                startDate = startDate,
                endDate = endDate,
                numberOfGuests = numberOfGuests,
                totalPrice = totalPrice,
                status = "PENDING",
                bookingDate = currentDate(),
                specialRequests = specialRequests
            )
        }

        private fun currentDate(): String {
            return try {
                Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                    .toString()
            } catch (e: Exception) {
                ""
            }
        }
    }
}

