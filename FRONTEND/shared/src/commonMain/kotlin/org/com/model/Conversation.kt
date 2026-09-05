package org.com.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(

    @SerialName("conversationId")
    val conversationId: Long = 0L,

    @SerialName("otherPartyId")
    val otherPartyId: Long = 0L,

    @SerialName("otherPartyName")
    val otherPartyName: String = "",

    @SerialName("otherPartyRole")
    val otherPartyRole: String = "",

    @SerialName("lastMessage")
    val lastMessage: String = "",

    @SerialName("lastMessageTime")
    val lastMessageTime: String = "",

    @SerialName("unreadCount")
    val unreadCount: Int = 0,

    @SerialName("messages")
    val messages: List<ChatMessage> = emptyList()
) {

    fun hasUnreadMessages(): Boolean = unreadCount > 0

    fun isEmpty(): Boolean = messages.isEmpty()

    fun latestMessage(): ChatMessage? = messages.lastOrNull()

    companion object {
        fun empty() = Conversation()
    }
}