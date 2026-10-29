package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("roomId")
    val roomId: Long = 0L,

    @SerialName("userId")
    val userId: Long = 0L,

    @SerialName("userName")
    val userName: String = "",

    @SerialName("rating")
    val rating: Int = 5,

    @SerialName("comment")
    val comment: String = "",

    @SerialName("createdAt")
    val createdAt: String? = null
)
