package org.com.model



import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
) {
    fun hasData(): Boolean = data != null

    fun isFailure(): Boolean = !success

    companion object {
        fun <T> success(data: T, message: String = "Success") =
            ApiResponse(true, message, data)

        fun <T> failure(message: String) =
            ApiResponse<T>(false, message, null)
    }
}