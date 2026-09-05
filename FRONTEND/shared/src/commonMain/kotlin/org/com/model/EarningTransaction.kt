package org.com.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EarningTransaction(
    @SerialName("id")
    val id: Long = 0L,

    @SerialName("propertyId")
    val propertyId: Long = 0L,

    @SerialName("propertyTitle")
    val propertyTitle: String = "",

    @SerialName("amount")
    val amount: Double = 0.0,

    @SerialName("commission")
    val commission: Double = 0.0,

    // PENDING, PAID, WITHDRAWN
    @SerialName("status")
    val status: String = "",

    @SerialName("date")
    val date: String = "",

    @SerialName("tenantName")
    val tenantName: String = ""
)