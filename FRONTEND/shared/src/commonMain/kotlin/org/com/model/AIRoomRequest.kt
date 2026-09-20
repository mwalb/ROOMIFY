package org.com.model

import kotlinx.serialization.Serializable

@Serializable
data class AIRoomRequest(
    val sourceImageUrl: String,
    val furniture: List<String>,
    val style: String,
    val roomWidth: Double? = null,
    val roomLength: Double? = null,
    val ceilingHeight: Double? = null,
    val additionalInstructions: String? = null
)
