package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(

    @SerialName("success")
    val success: Boolean = false,

    @SerialName("message")
    val message: String = "",

    @SerialName("user")
    val user: User? = null,

    @SerialName("token")
    val token: String? = null
) {

    fun isAuthenticated(): Boolean {
        return success && !token.isNullOrBlank()
    }

    fun hasUser(): Boolean {
        return user != null
    }

    fun isFailure(): Boolean {
        return !success
    }

    companion object {

        fun empty(): AuthResponse {
            return AuthResponse()
        }
    }
}