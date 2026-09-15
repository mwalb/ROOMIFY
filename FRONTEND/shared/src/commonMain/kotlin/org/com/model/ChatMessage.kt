package org.com.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(

    @SerialName("messageId")
    val messageId: Long = 0L,

    @SerialName("senderId")
    val senderId: Long = 0L,

    @SerialName("receiverId")
    val receiverId: Long = 0L,

    @SerialName("content")

    val content: String = "",

    @SerialName("timestamp")
    val timestamp: String = "",

    @SerialName("isRead")
    val isRead: Boolean = false,

    @SerialName("roomId")
    val roomId: Long? = null,

    @SerialName("roomTitle")
    val roomTitle: String? = null
)