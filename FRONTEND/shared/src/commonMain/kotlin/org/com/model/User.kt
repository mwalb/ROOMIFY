package org.com.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id")
    val id: Long = 0,

    @SerialName("email")
    val email: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("role")
    val role: String = "",

    @SerialName("emailVerified")
    val emailVerified: Boolean = false,

    // ========== DALALI-SPECIFIC FIELDS ==========
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

    @SerialName("rating")
    val rating: Float = 0f,

    @SerialName("totalTransactions")
    val totalTransactions: Int = 0,

    @SerialName("joinedDate")
    val joinedDate: String? = null,

    @SerialName("profileImage")
    val profileImage: String? = null,
)

/** Roles a User can have. */
enum class UserRole {
    TENANT, OWNER, DALALI, ADMIN, UNKNOWN
}

enum class VerificationStatus {
    VERIFIED, PENDING, UNVERIFIED
}

// ========== HELPER PROPERTIES / FUNCTIONS ==========

val User.userRole: UserRole
    get() = when {
        role.equals("tenant", ignoreCase = true) -> UserRole.TENANT
        role.equals("owner", ignoreCase = true) -> UserRole.OWNER
        role.equals("dalali", ignoreCase = true) -> UserRole.DALALI
        role.equals("admin", ignoreCase = true) -> UserRole.ADMIN
        else -> UserRole.UNKNOWN
    }

fun User.isTenant(): Boolean = userRole == UserRole.TENANT
fun User.isOwner(): Boolean = userRole == UserRole.OWNER
fun User.isDalali(): Boolean = userRole == UserRole.DALALI
fun User.isAdmin(): Boolean = userRole == UserRole.ADMIN

val User.verification: VerificationStatus
    get() = when {
        verificationStatus.equals("VERIFIED", ignoreCase = true) -> VerificationStatus.VERIFIED
        verificationStatus.equals("PENDING", ignoreCase = true) -> VerificationStatus.PENDING
        else -> VerificationStatus.UNVERIFIED
    }

fun User.isVerified(): Boolean = verification == VerificationStatus.VERIFIED
fun User.isPending(): Boolean = verification == VerificationStatus.PENDING

val User.displayName: String
    get() = if (!businessName.isNullOrEmpty() && isOwner()) businessName else name

val User.initials: String
    get() {
        if (name.isEmpty()) return "U"
        val parts = name.split(" ").filter { it.isNotEmpty() }
        return when {
            parts.isEmpty() -> "U"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
        }
    }


val User.verificationBadge: String
    get() = when (verification) {
        VerificationStatus.VERIFIED -> "✓ Verified"
        VerificationStatus.PENDING -> "⏳ Pending"
        VerificationStatus.UNVERIFIED -> "⚠️ Unverified"
    }