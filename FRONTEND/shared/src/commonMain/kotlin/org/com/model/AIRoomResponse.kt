package org.com.model

import kotlinx.serialization.Serializable

@Serializable
data class AIRoomResponse(
    val generatedImageUrl: String? = null,
    val recommendation: String? = null,
    val style: String? = null
)
