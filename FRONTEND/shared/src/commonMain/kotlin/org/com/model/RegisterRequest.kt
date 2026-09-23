// RegisterRequest.kt
package org.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(

    @SerialName("name")
    val name: String = "",

    @SerialName("email")
    val email: String = "",

    @SerialName("password")
    val password: String = "",

    @SerialName("role")
    val role: String = "",

    @SerialName("businessName")
    val businessName: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("nidaNumber")
    val nidaNumber: String? = null,

    @SerialName("licenseNumber")
    val licenseNumber: String? = null,

    @SerialName("locationArea")
    val locationArea: String? = null,

    @SerialName("verificationStatus")
    val verificationStatus: String? = null,

    // Mtendaji/Mwenyekiti wa Mtaa (Local Authority) fields
    @SerialName("localAuthorityName")
    val localAuthorityName: String? = null,

    @SerialName("localAuthorityPhone")
    val localAuthorityPhone: String? = null,

    @SerialName("localAuthorityArea")
    val localAuthorityArea: String? = null,

    @SerialName("localAuthorityVillage")
    val localAuthorityVillage: String? = null,

    @SerialName("localAuthorityWard")
    val localAuthorityWard: String? = null,

    @SerialName("localAuthorityDistrict")
    val localAuthorityDistrict: String? = null,

    @SerialName("localAuthorityRegion")
    val localAuthorityRegion: String? = null,

    // Legacy fields for backward compatibility
    @SerialName("baloziName")
    val baloziName: String? = null,

    @SerialName("baloziPhone")
    val baloziPhone: String? = null,

    @SerialName("baloziArea")
    val baloziArea: String? = null,

    @SerialName("baloziVillage")
    val baloziVillage: String? = null,

    @SerialName("baloziWard")
    val baloziWard: String? = null,

    @SerialName("baloziDistrict")
    val baloziDistrict: String? = null,

    @SerialName("baloziRegion")
    val baloziRegion: String? = null
) {

    /*
     * ============================================================
     * ROLE HELPERS
     * ============================================================
     */

    fun isAgent(): Boolean =
        role.equals("dalali", ignoreCase = true)

    fun isLandlord(): Boolean =
        role.equals("owner", ignoreCase = true)

    fun isTenant(): Boolean =
        role.equals("tenant", ignoreCase = true)

    /*
     * ============================================================
     * FACTORIES
     * ============================================================
     */

    companion object {

        /*
         * --------------------------------------------------------
         * TENANT
         * --------------------------------------------------------
         */

        fun createTenant(
            name: String,
            email: String,
            password: String,
            phone: String
        ): RegisterRequest {
            return RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = "tenant",
                phone = phone
            )
        }

        /*
         * --------------------------------------------------------
         * LANDLORD / OWNER (with Mtendaji/Mwenyekiti wa Mtaa details)
         * --------------------------------------------------------
         */

        fun createLandlord(
            name: String,
            email: String,
            password: String,
            phone: String
        ): RegisterRequest {
            return RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = "owner",
                phone = phone
            )
        }

        fun createOwner(
            name: String,
            email: String,
            password: String,
            businessName: String,
            phone: String,
            nidaNumber: String,
            localAuthorityName: String,
            localAuthorityPhone: String,
            localAuthorityArea: String,
            localAuthorityVillage: String,
            localAuthorityWard: String = "",
            localAuthorityDistrict: String = "",
            localAuthorityRegion: String = "",
            licenseNumber: String? = null
        ): RegisterRequest {
            return RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = "owner",
                businessName = businessName,
                phone = phone,
                nidaNumber = nidaNumber,
                licenseNumber = if (licenseNumber.isNullOrBlank()) null else licenseNumber,
                localAuthorityName = localAuthorityName,
                localAuthorityPhone = localAuthorityPhone,
                localAuthorityArea = localAuthorityArea,
                localAuthorityVillage = localAuthorityVillage,
                localAuthorityWard = localAuthorityWard,
                localAuthorityDistrict = localAuthorityDistrict,
                localAuthorityRegion = localAuthorityRegion,
                baloziName = localAuthorityName,
                baloziPhone = localAuthorityPhone,
                baloziArea = localAuthorityArea,
                baloziVillage = localAuthorityVillage,
                baloziWard = localAuthorityWard,
                baloziDistrict = localAuthorityDistrict,
                baloziRegion = localAuthorityRegion,
                verificationStatus = "pending"
            )
        }

        /*
         * --------------------------------------------------------
         * AGENT / DALALI
         * --------------------------------------------------------
         */

        fun createAgent(
            name: String,
            email: String,
            password: String,
            businessName: String,
            phone: String,
            nidaNumber: String,
            locationArea: String,
            licenseNumber: String? = null
        ): RegisterRequest {
            return RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = "dalali",
                businessName = businessName,
                phone = phone,
                nidaNumber = nidaNumber,
                licenseNumber = if (licenseNumber.isNullOrBlank()) null else licenseNumber,
                locationArea = locationArea,
                verificationStatus = "pending"
            )
        }

        fun createDalali(
            name: String,
            email: String,
            password: String,
            businessName: String,
            phone: String,
            nidaNumber: String,
            locationArea: String,
            licenseNumber: String? = null
        ): RegisterRequest {
            return createAgent(
                name = name,
                email = email,
                password = password,
                businessName = businessName,
                phone = phone,
                nidaNumber = nidaNumber,
                locationArea = locationArea,
                licenseNumber = licenseNumber
            )
        }
    }
}
