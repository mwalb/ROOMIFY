package org.com.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EarningsResponse(
    @SerialName("totalEarnings")
    val totalEarnings: Double = 0.0,

    @SerialName("pendingEarnings")
    val pendingEarnings: Double = 0.0,

    @SerialName("withdrawnEarnings")
    val withdrawnEarnings: Double = 0.0,

    @SerialName("transactions")
    val transactions: List<EarningTransaction> = emptyList()
)