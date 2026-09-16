package org.com.model

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val address: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val ownerId: Long? = null,
    val ownerName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val propertyType: String = "SINGLE_ROOM",
    val hasVideo: Boolean = false,
    val hasContract: Boolean = false,
    val videoUrl: String? = null,
    val contractUrl: String? = null,
    val images: List<String> = emptyList(),
    val units: List<Room> = emptyList()
)
