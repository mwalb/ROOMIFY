package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import org.com.network.ApiClient

@Serializable
data class Furniture(
    @SerialName("id")
    val id: Long? = null,
    @SerialName("title")
    val title: String = "",
    @SerialName("description")
    val description: String = "",
    @SerialName("price")
    val price: Double = 0.0,
    @SerialName("category")
    val category: String = "",
    @SerialName("conditionStatus")
    val condition: String = "", // NEW, USED
    @SerialName("postedBy")
    val postedBy: Long? = null,
    @SerialName("ownerName")
    val ownerName: String? = null,
    @SerialName("contactPhone")
    val contactPhone: String? = null,
    @SerialName("contactEmail")
    val contactEmail: String? = null,
    @SerialName("latitude")
    val latitude: Double = 0.0,
    @SerialName("longitude")
    val longitude: Double = 0.0,
    @SerialName("address")
    val address: String? = null,
    @SerialName("images")
    val images: List<String> = emptyList(),
    @SerialName("shopId")
    val shopId: Long? = null,
    @SerialName("videoUrl")
    val videoUrl: String? = null,
    @SerialName("hasVideo")
    val hasVideo: Boolean = false,
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("status")
    val status: String = "AVAILABLE" // AVAILABLE, SOLD
) {
    val formattedPrice: String
        get() = "TZS ${price.toLong().toString().reversed().chunked(3).joinToString(",").reversed()}"

    val firstImageUrl: String?
        get() = images.firstOrNull()?.let { getFullUrl(it) }

    val fullImageUrls: List<String>
        get() = images.map { getFullUrl(it) }

    fun getFullUrl(path: String): String = ApiClient.resolveUrl(path)
}
