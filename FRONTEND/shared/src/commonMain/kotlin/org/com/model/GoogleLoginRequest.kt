package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request used when an existing Roomify user
 * signs in with Google.
 *
 * Role is NOT included.
 *
 * The backend determines the user's Roomify role
 * from the account associated with the Google identity.
 */
@Serializable
data class GoogleLoginRequest(

    @SerialName("idToken")
    val idToken: String = ""
)