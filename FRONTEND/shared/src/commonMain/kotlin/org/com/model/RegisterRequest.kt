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

    @SerialName("licenseNumber")
    val licenseNumber: String? = null,

    @SerialName("locationArea")
    val locationArea: String? = null,

    @SerialName("verificationStatus")
    val verificationStatus: String? = null,

    // Balozi (Local Government Representative) fields
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
         * LANDLORD / OWNER (with Balozi details)
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
            baloziName: String,
            baloziPhone: String,
            baloziArea: String,
            baloziVillage: String,
            baloziWard: String = "",
            baloziDistrict: String = "",
            baloziRegion: String = ""
        ): RegisterRequest {
            return RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = "owner",
                businessName = businessName,
                phone = phone,
                baloziName = baloziName,
                baloziPhone = baloziPhone,
                baloziArea = baloziArea,
                baloziVillage = baloziVillage,
                baloziWard = baloziWard,
                baloziDistrict = baloziDistrict,
                baloziRegion = baloziRegion,
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
            licenseNumber: String,
            locationArea: String
        ): RegisterRequest {
            return RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = "dalali",
                businessName = businessName,
                phone = phone,
                licenseNumber = licenseNumber,
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
            licenseNumber: String,
            locationArea: String
        ): RegisterRequest {
            return createAgent(
                name = name,
                email = email,
                password = password,
                businessName = businessName,
                phone = phone,
                licenseNumber = licenseNumber,
                locationArea = locationArea
            )
        }
    }
}