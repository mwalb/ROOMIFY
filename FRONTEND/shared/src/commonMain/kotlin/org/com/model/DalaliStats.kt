package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class DalaliStats(

    @SerialName("totalListings")
    val totalListings: Int = 0,

    @SerialName("activeListings")
    val activeListings: Int = 0,

    @SerialName("pendingListings")
    val pendingListings: Int = 0,

    @SerialName("rentedListings")
    val rentedListings: Int = 0,

    @SerialName("totalCommission")
    val totalCommission: Double = 0.0,

    @SerialName("monthlyCommission")
    val monthlyCommission: Double = 0.0,

    @SerialName("totalViews")
    val totalViews: Int = 0,

    @SerialName("totalInterested")
    val totalInterested: Int = 0,

    @SerialName("averageRating")
    val averageRating: Float = 0f,

    @SerialName("verificationStatus")
    val verificationStatus: String = "UNVERIFIED"
) {

    // ------------------------------------------------------------
    // FORMATTING HELPERS
    // ------------------------------------------------------------

    fun getFormattedTotalCommission(): String {
        val rounded = (totalCommission * 100).roundToInt() / 100.0
        return "TSh $rounded"
    }

    fun getFormattedMonthlyCommission(): String {
        val rounded = (monthlyCommission * 100).roundToInt() / 100.0
        return "TSh $rounded"
    }

    fun getFormattedRating(): String {
        val rounded = (averageRating * 10).roundToInt() / 10f
        return "$rounded ⭐"
    }

    // ------------------------------------------------------------
    // BUSINESS HELPERS
    // ------------------------------------------------------------

    fun activePercentage(): Int {
        return if (totalListings == 0) 0
        else (activeListings * 100) / totalListings
    }

    fun rentedPercentage(): Int {
        return if (totalListings == 0) 0
        else (rentedListings * 100) / totalListings
    }

    fun isVerified(): Boolean = verificationStatus == "VERIFIED"

    fun isTopRated(): Boolean = averageRating >= 4.5f

    fun hasHighActivity(): Boolean = activeListings >= 10

    fun hasPendingApprovals(): Boolean = pendingListings > 0

    companion object {
        fun empty() = DalaliStats()
    }
}