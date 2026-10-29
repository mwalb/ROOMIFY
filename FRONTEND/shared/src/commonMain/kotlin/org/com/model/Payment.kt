package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("bookingId")
    val bookingId: Long? = null,

    @SerialName("roomId")
    val roomId: Long? = null,

    @SerialName("userId")
    val userId: Long? = null,

    @SerialName("ownerId")
    val ownerId: Long? = null,

    @SerialName("amount")
    val amount: Double = 0.0,

    @SerialName("provider")
    val provider: String = "M-PESA",

    @SerialName("phone")
    val phone: String = "",

    @SerialName("referenceNumber")
    val referenceNumber: String = "",

    @SerialName("status")
    val status: String = "SUCCESS",

    @SerialName("message")
    val message: String = "",

    @SerialName("createdAt")
    val createdAt: String? = null
)

@Serializable
data class PaymentRequest(
    val bookingId: Long? = null,
    val userId: Long? = null,
    val amount: Double = 0.0,
    val provider: String = "M-PESA",
    val phone: String = ""
)

@Serializable
data class FinancesSummary(
    val totalRevenue: Double = 0.0,
    val completedPaymentsCount: Int = 0,
    val recentPayments: List<Payment> = emptyList(),
    val currency: String = "TZS"
)
