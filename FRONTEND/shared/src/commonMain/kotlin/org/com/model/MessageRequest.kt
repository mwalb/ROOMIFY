package org.com.model



import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MessageRequest(
    @JsonNames("message")
    val message: String = "",

    @JsonNames("subject")
    val subject: String = ""
) {

}