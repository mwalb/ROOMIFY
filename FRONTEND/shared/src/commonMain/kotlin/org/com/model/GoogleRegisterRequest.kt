package org.com.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleRegisterRequest(
    @SerialName("idToken")
    val idToken: String,

    @SerialName("role")
    val role: String,

    val phone: String? = null,
    val nidaNumber: String? = null,
    val businessName: String? = null,
    val localAuthorityName: String? = null,
    val localAuthorityPhone: String? = null,
    val localAuthorityArea: String? = null,
    val localAuthorityVillage: String? = null,
    val localAuthorityWard: String? = null,
    val localAuthorityDistrict: String? = null,
    val localAuthorityRegion: String? = null,
    val licenseNumber: String? = null,
    val locationArea: String? = null
)