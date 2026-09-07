package org.com.model

import kotlinx.serialization.Serializable
import org.com.network.ApiClient

@Serializable
data class Room(
    val id: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val propertyType: String? = null,
    val price: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String? = null,
    val postedBy: Long? = null,
    val ownerName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val roomsCount: Int = 0,
    val bathroomsCount: Int = 0,
    val area: Double = 0.0,
    val status: String = "AVAILABLE",
    val isAvailable: Boolean = true,
    val bookingsCount: Int = 0,
    val viewCount: Int = 0,
    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val rules: List<String> = emptyList(),
    val dalaliName: String? = null,
    val commission: Double? = null,
    val featured: Boolean = false,
    val promoted: Boolean = false,
    val hasVideo: Boolean = false,
    val hasContract: Boolean = false,
    val videoUrl: String? = null,
    val contractUrl: String? = null
) {
    val formattedPrice: String
        get() = if (price > 0) {
            val formatted = price.toLong().toString()
                .reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()
            "TZS $formatted/month"
        } else "Price on request"

    val propertySummary: String
        get() = buildString {
            if (roomsCount > 0) append("$roomsCount bed")
            if (bathroomsCount > 0) {
                if (isNotEmpty()) append(" • ")
                append("$bathroomsCount bath")
            }
            if (area > 0) {
                if (isNotEmpty()) append(" • ")
                append("${area.toInt()} m²")
            }
        }

    val firstImageUrl: String?
        get() = images.firstOrNull()?.let { getFullUrl(it) }

    val fullImageUrls: List<String>
        get() = images.map { getFullUrl(it) }

    fun getFullUrl(path: String): String {
        if (path.isEmpty()) return ""
        
        // Fix potential localhost issues from database
        val fixedPath = if (path.contains("localhost:8080")) {
            path.replace("http://localhost:8080", "")
                .replace("localhost:8080", "")
        } else path

        val baseUrl = ApiClient.MEDIA_BASE_URL
        val fullUrl = if (fixedPath.startsWith("http")) fixedPath
                      else if (fixedPath.startsWith("/")) "$baseUrl$fixedPath" 
                      else "$baseUrl/$fixedPath"
                      
        // Add cache buster
        return if (fullUrl.contains("?")) "$fullUrl&cb=${org.com.currentTimeMillis()}"
               else "$fullUrl?cb=${org.com.currentTimeMillis()}"
    }

    val locationSummary: String
        get() = address?.split(",")?.firstOrNull()?.trim() ?: "Location not specified"
}