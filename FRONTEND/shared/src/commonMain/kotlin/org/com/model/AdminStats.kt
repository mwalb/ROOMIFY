package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminStats(
    @SerialName("totalUsers") val totalUsers: Long = 0,
    @SerialName("tenants") val tenants: Long = 0,
    @SerialName("owners") val owners: Long = 0,
    @SerialName("dalalis") val dalalis: Long = 0,
    @SerialName("admins") val admins: Long = 0,
    @SerialName("suspendedUsers") val suspendedUsers: Long = 0,
    @SerialName("activeUsers") val activeUsers: Long = 0,
    @SerialName("totalProperties") val totalProperties: Long = 0,
    @SerialName("pendingProperties") val pendingProperties: Long = 0,
    @SerialName("verifiedProperties") val verifiedProperties: Long = 0,
    @SerialName("rejectedProperties") val rejectedProperties: Long = 0,
    @SerialName("suspendedProperties") val suspendedProperties: Long = 0,
    @SerialName("availableProperties") val availableProperties: Long = 0,
    @SerialName("rentedProperties") val rentedProperties: Long = 0,
    @SerialName("totalBookings") val totalBookings: Long = 0
)
