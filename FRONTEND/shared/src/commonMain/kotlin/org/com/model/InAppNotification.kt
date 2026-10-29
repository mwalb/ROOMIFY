package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InAppNotification(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("userId")
    val userId: Long = 0L,

    @SerialName("title")
    val title: String = "",

    @SerialName("message")
    val message: String = "",

    @SerialName("type")
    val type: String = "SYSTEM",

    @SerialName("referenceId")
    val referenceId: Long? = null,

    @SerialName("isRead")
    val isRead: Boolean = false,

    @SerialName("createdAt")
    val createdAt: String? = null
)
