package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(

    @SerialName("email")
    val email: String = ""
) {

    fun isValidEmail(): Boolean {
        return email.contains("@") && email.contains(".")
    }
}