package org.com.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleRegisterRequest(
    @SerialName("idToken")
    val idToken: String,

    @SerialName("role")
    val role: String
)