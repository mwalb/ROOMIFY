package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request used for normal email/password login.
 *
 * Role is deliberately NOT included.
 *
 * The backend determines the user's role from the
 * authenticated Roomify account.
 */
@Serializable
data class LoginRequest(

    @SerialName("email")
    val email: String = "",

    @SerialName("password")
    val password: String = "",
    val role: String
)