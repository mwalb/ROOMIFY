package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.com.network.ApiClient

@Serializable
data class Shop(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("name")
    val name: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("address")
    val address: String? = null,
    @SerialName("latitude")
    val latitude: Double = 0.0,
    @SerialName("longitude")
    val longitude: Double = 0.0,
    @SerialName("contactPhone")
    val contactPhone: String? = null,
    @SerialName("contactEmail")
    val contactEmail: String? = null,
    @SerialName("websiteUrl")
    val websiteUrl: String? = null,
    @SerialName("ownerId")
    val ownerId: Long? = null,
    @SerialName("images")
    val images: List<String> = emptyList(),
    @SerialName("logoUrl")
    val logoUrl: String? = null,
    @SerialName("videoUrl")
    val videoUrl: String? = null,
    @SerialName("hasVideo")
    val hasVideo: Boolean = false,
    @SerialName("createdAt")
    val createdAt: String? = null
) {
    val firstImageUrl: String?
        get() = images.firstOrNull()?.let { getFullUrl(it) }

    val fullImageUrls: List<String>
        get() = images.map { getFullUrl(it) }

    fun getFullUrl(path: String): String = ApiClient.resolveUrl(path)
}
