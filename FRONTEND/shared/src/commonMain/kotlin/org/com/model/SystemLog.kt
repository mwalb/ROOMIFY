package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemLog(
    @SerialName("id")
    val id: Long = 0,

    @SerialName("timestamp")
    val timestamp: String = "",

    @SerialName("level")
    val level: String = "INFO", // INFO, WARN, ERROR, AUDIT

    @SerialName("action")
    val action: String = "",

    @SerialName("userEmail")
    val userEmail: String? = null,

    @SerialName("userRole")
    val userRole: String? = null,

    @SerialName("details")
    val details: String = "",

    @SerialName("ipAddress")
    val ipAddress: String? = null
)
