package org.com.network

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import org.com.model.*

object RoomifyApi {

    // ============================================================
    // AUTH - FIXED: Removed "/api/" prefix (already in ApiClient)
    // ============================================================

    suspend fun register(request: RegisterRequest): AuthResponse {
        return ApiClient.post("auth/register", request)  // ✅ Fixed
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val response: AuthResponse = ApiClient.post("auth/login", request)  // ✅ Fixed

        response.token?.let { token ->
            ApiClient.setToken(token)
        }

        return response
    }

    suspend fun googleLogin(request: GoogleLoginRequest): AuthResponse {
        return ApiClient.post("auth/google", request)  // ✅ Fixed
    }

    suspend fun googleRegister(idToken: String, role: String): AuthResponse {
        val request = GoogleRegisterRequest(idToken = idToken, role = role)
        val response: AuthResponse = ApiClient.post("auth/google/register", request)  // ✅ Fixed

        response.token?.let { token ->
            ApiClient.setToken(token)
        }

        return response
    }

    suspend fun guestLogin(): AuthResponse {
        return ApiClient.post("auth/guest", Unit)  // ✅ Fixed
    }

    suspend fun logout(): AuthResponse {
        return ApiClient.post("auth/logout", Unit)  // ✅ Fixed
    }

    suspend fun getCurrentUser(): AuthResponse {
        return ApiClient.get("auth/me")  // ✅ Fixed
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): AuthResponse {
        return ApiClient.post("auth/forgot-password", request)  // ✅ Fixed
    }

    suspend fun testToken(): AuthResponse {
        return ApiClient.get("auth/test-token")  // ✅ Fixed
    }

    // ============================================================
    // ROOMS - FIXED: Removed "/api/" prefix
    // ============================================================

    suspend fun getAllRooms(): List<Room> {
        return ApiClient.get("rooms")  // ✅ Fixed (was /api/rooms)
    }

    suspend fun getRoomById(id: Long): Room {
        return ApiClient.get("rooms/$id")  // ✅ Fixed
    }

    suspend fun createRoom(request: CreateRoomRequest): Room {
        return ApiClient.post("rooms", request)  // ✅ Fixed
    }

    suspend fun updateRoom(id: Long, room: Room): Room {
        return ApiClient.put("rooms/$id", room)  // ✅ Fixed
    }

    suspend fun deleteRoom(id: Long): ApiResponse<Unit> {
        return ApiClient.delete("rooms/$id")  // ✅ Fixed
    }

    suspend fun getRoomsByOwner(ownerId: Long): List<Room> {
        return ApiClient.get("rooms/owner/$ownerId")  // ✅ Fixed
    }

    suspend fun getPropertyTypes(): List<String> {
        return try {
            ApiClient.get("rooms/types")
        } catch (e: Exception) {
            listOf("Room", "Apartment", "Studio", "House", "Office")
        }
    }

    // ============================================================
    // BOOKINGS
    // ============================================================

    suspend fun getUserBookings(userId: Long): ApiResponse<List<BookingResponse>> {
        return ApiClient.get("bookings/user/$userId")  // ✅ Fixed
    }

    suspend fun getOwnerBookings(ownerId: Long): ApiResponse<List<BookingResponse>> {
        return ApiClient.get("bookings/owner/$ownerId")  // ✅ Fixed
    }

    suspend fun createBooking(booking: BookingRequest): ApiResponse<BookingResponse> {
        return ApiClient.post("bookings", booking)  // ✅ Fixed
    }

    // ============================================================
    // FAVORITES
    // ============================================================

    suspend fun isFavorite(userId: Long, roomId: Long): ApiResponse<Boolean> {
        return ApiClient.get("favorites/$userId/$roomId")  // ✅ Fixed
    }

    suspend fun toggleFavorite(userId: Long, roomId: Long): ApiResponse<Boolean> {
        return ApiClient.post("favorites/$userId/$roomId/toggle", Unit)  // ✅ Fixed
    }

    suspend fun getUserFavorites(userId: Long): ApiResponse<List<Room>> {
        return ApiClient.get("favorites/$userId")  // ✅ Fixed
    }

    // ============================================================
    // USERS
    // ============================================================

    suspend fun getUserProfile(): ApiResponse<User> {
        return ApiClient.get("users/profile")  // ✅ Fixed
    }

    suspend fun updateUserProfile(user: User): ApiResponse<User> {
        return ApiClient.put("users/profile", user)  // ✅ Fixed
    }

    suspend fun getUserById(id: Long): ApiResponse<User> {
        return ApiClient.get("users/$id")  // ✅ Fixed
    }

    // ============================================================
    // FURNITURE
    // ============================================================

    suspend fun getAllFurniture(): ApiResponse<List<Furniture>> {
        return ApiClient.get("furniture")
    }

    suspend fun createFurniture(furniture: Furniture): ApiResponse<Furniture> {
        return ApiClient.post("furniture", furniture)
    }

    suspend fun uploadFurnitureImages(furnitureId: Long, imageBytes: List<ByteArray>): ApiResponse<List<String>> {
        return try {
            val response = ApiClient.client.post("furniture/$furnitureId/images") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            imageBytes.forEachIndexed { index, bytes ->
                                append("images", bytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"furniture_$index.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            response.body()
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Upload failed")
        }
    }

    suspend fun uploadProfileImage(imageBytes: ByteArray): ApiResponse<String> {
        return try {
            val response = ApiClient.client.post("users/profile/image") {
                // Let Ktor's MultiPartFormDataContent set the Content-Type with boundary
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("image", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"profile.jpg\"")
                            })
                        }
                    )
                )
            }
            response.body()
        } catch (e: Exception) {
            println("RoomifyApi: Profile image upload error: ${e.message}")
            ApiResponse(success = false, message = e.message ?: "Upload failed")
        }
    }

    // ============================================================
    // CHAT
    // ============================================================

    suspend fun getConversations(userId: Long): ApiResponse<List<Conversation>> {
        return ApiClient.get("chat/conversations/$userId")
    }

    suspend fun getChatHistory(user1: Long, user2: Long, roomId: Long? = null): ApiResponse<List<ChatMessage>> {
        val query = if (roomId != null) "?user1=$user1&user2=$user2&roomId=$roomId" else "?user1=$user1&user2=$user2"
        return ApiClient.get("chat/history$query")
    }

    suspend fun sendMessage(message: ChatMessage): ApiResponse<ChatMessage> {
        return ApiClient.post("chat/send", message)
    }

    // ... rest of functions with same fix (remove "/api/" prefix)
}