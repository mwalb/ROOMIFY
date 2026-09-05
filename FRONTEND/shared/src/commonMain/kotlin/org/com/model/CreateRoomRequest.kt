package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(

    @SerialName("title")
    val title: String = "",

    @SerialName("description")
    val description: String = "",

    @SerialName("price")
    val price: Double = 0.0,

    @SerialName("latitude")
    val latitude: Double = 0.0,

    @SerialName("longitude")
    val longitude: Double = 0.0,

    @SerialName("address")
    val address: String = "",

    @SerialName("propertyType")
    val propertyType: String = "",

    @SerialName("contactPhone")
    val contactPhone: String = "",

    @SerialName("contactEmail")
    val contactEmail: String = "",

    @SerialName("ownerName")
    val ownerName: String = "",

    @SerialName("amenities")
    val amenities: List<String> = emptyList(),

    @SerialName("roomsCount")
    val roomsCount: Int = 0,

    @SerialName("bathroomsCount")
    val bathroomsCount: Int = 0,

    @SerialName("area")
    val area: Double = 0.0,

    @SerialName("rules")
    val rules: List<String> = emptyList(),

    @SerialName("postedBy")
    val postedBy: Long? = null,

    @SerialName("hasVideo")
    val hasVideo: Boolean = false,

    @SerialName("hasContract")
    val hasContract: Boolean = false,

    @SerialName("available")
    val available: Boolean = true,

    @SerialName("status")
    val status: String = "PENDING"
) {

    // ------------------------------------------------------------
    // VALIDATION HELPERS
    // ------------------------------------------------------------

    fun isValid(): Boolean {
        return title.isNotBlank() &&
                description.isNotBlank() &&
                price > 0 &&
                address.isNotBlank() &&
                contactPhone.isNotBlank() &&
                ownerName.isNotBlank()
    }

    fun hasLocation(): Boolean {
        return latitude != 0.0 || longitude != 0.0
    }

    fun hasAmenities(): Boolean = amenities.isNotEmpty()

    fun hasRules(): Boolean = rules.isNotEmpty()

    companion object {
        fun empty() = CreateRoomRequest()
    }
}
