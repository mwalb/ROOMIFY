package org.com.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TenantInterest(

    @SerialName("tenantId")
    val tenantId: Long = 0L,

    @SerialName("tenantName")
    val tenantName: String = "",

    @SerialName("tenantEmail")
    val tenantEmail: String = "",

    @SerialName("tenantPhone")
    val tenantPhone: String = "",

    @SerialName("interestDate")
    val interestDate: String = "",

    @SerialName("message")
    val message: String = "",

    // PENDING, CONTACTED, VIEWED, BOOKED
    @SerialName("status")
    val status: String = "PENDING"
) {

    // Helper functions
    fun isPending(): Boolean = status == "PENDING"

    fun isContacted(): Boolean = status == "CONTACTED"

    fun isViewed(): Boolean = status == "VIEWED"

    fun isBooked(): Boolean = status == "BOOKED"

    fun hasMessage(): Boolean = message.isNotBlank()

    companion object {
        fun empty() = TenantInterest()
    }
}