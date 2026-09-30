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

    suspend fun getAreaSuggestions(): List<String> {
        return try {
            ApiClient.get("rooms/areas")
        } catch (e: Exception) {
            emptyList()
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
    // PROPERTIES & BUILDINGS
    // ============================================================

    suspend fun createProperty(property: Property): ApiResponse<Property> {
        return try {
            ApiClient.post("properties", property)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to create property")
        }
    }

    suspend fun getOwnerProperties(ownerId: Long): ApiResponse<List<Property>> {
        return ApiClient.get("properties/owner/$ownerId")
    }

    suspend fun getPropertyById(id: Long): ApiResponse<Property> {
        return ApiClient.get("properties/$id")
    }

    suspend fun getRoomsByProperty(propertyId: Long): ApiResponse<List<Room>> {
        return ApiClient.get("rooms/property/$propertyId")
    }

    // ============================================================
    // SHOPS
    // ============================================================

    suspend fun getAllShops(): ApiResponse<List<Shop>> {
        return ApiClient.get("shops")
    }

    suspend fun getShopById(id: Long): ApiResponse<Shop> {
        return ApiClient.get("shops/$id")
    }

    suspend fun createShop(shop: Shop): ApiResponse<Shop> {
        return try {
            ApiClient.post("shops", shop)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to create shop")
        }
    }

    suspend fun uploadShopImages(shopId: Long, imageBytes: List<ByteArray>): ApiResponse<List<String>> {
        return try {
            val response = ApiClient.client.post("shops/$shopId/images") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            imageBytes.forEachIndexed { index, bytes ->
                                append("images", bytes, Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(HttpHeaders.ContentDisposition, "filename=\"shop_$index.jpg\"")
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

    suspend fun uploadShopVideo(shopId: Long, videoBytes: ByteArray): ApiResponse<String> {
        return try {
            val response = ApiClient.client.post("shops/$shopId/video") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("video", videoBytes, Headers.build {
                                append(HttpHeaders.ContentType, "video/mp4")
                                append(HttpHeaders.ContentDisposition, "filename=\"shop_video.mp4\"")
                            })
                        }
                    )
                )
            }
            response.body()
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Upload failed")
        }
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
    // AI ROOM ARRANGER
    // ============================================================

    suspend fun visualizeRoom(request: AIRoomRequest): ApiResponse<AIRoomResponse> {
        return try {
            ApiClient.post("ai/visualize", request)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "AI Generation failed")
        }
    }

    suspend fun visualizeCustomRoom(request: AIRoomRequest, imageBytes: ByteArray): ApiResponse<AIRoomResponse> {
        return try {
            val response = ApiClient.client.post("ai/visualize-custom") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("request", kotlinx.serialization.json.Json.encodeToString(AIRoomRequest.serializer(), request), Headers.build {
                                append(HttpHeaders.ContentType, "application/json")
                            })
                            append("image", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"room.jpg\"")
                            })
                        }
                    )
                )
            }
            response.body()
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "AI Generation failed")
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

    // ============================================================
    // VERIFICATION, LOCATIONS & NOTIFICATIONS
    // ============================================================

    suspend fun getLocationSuggestions(query: String): List<String> {
        return try {
            ApiClient.get("rooms/locations/suggestions?query=$query")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun subscribeToNotification(propertyId: Long): ApiResponse<Unit> {
        return try {
            ApiClient.post("notifications/subscribe?propertyId=$propertyId", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to subscribe")
        }
    }

    suspend fun checkSubscription(propertyId: Long): ApiResponse<Boolean> {
        return try {
            ApiClient.get("notifications/subscribed?propertyId=$propertyId")
        } catch (e: Exception) {
            ApiResponse(success = false, data = false, message = e.message ?: "Failed to check subscription")
        }
    }

    suspend fun getPendingProperties(): ApiResponse<List<Room>> {
        return try {
            ApiClient.get("rooms/admin/pending")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch pending properties")
        }
    }

    suspend fun verifyProperty(id: Long): ApiResponse<Unit> {
        return try {
            ApiClient.put("rooms/admin/$id/verify", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to verify property")
        }
    }

    suspend fun rejectProperty(id: Long, reason: String): ApiResponse<Unit> {
        return try {
            ApiClient.put("rooms/admin/$id/reject", mapOf("reason" to reason))
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to reject property")
        }
    }

    suspend fun getPendingUsers(): ApiResponse<List<User>> {
        return try {
            ApiClient.get("admin-management/users/pending")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch pending users")
        }
    }

    suspend fun verifyUser(id: Long): ApiResponse<User> {
        return try {
            ApiClient.put("admin-management/users/$id/verify", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to verify user")
        }
    }

    suspend fun rejectUser(id: Long, reason: String): ApiResponse<User> {
        return try {
            ApiClient.put("admin-management/users/$id/reject", mapOf("reason" to reason))
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to reject user")
        }
    }

    suspend fun getAllUsers(): ApiResponse<List<User>> {
        return try {
            ApiClient.get("admin-management/users")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch users")
        }
    }

    suspend fun suspendUser(id: Long): ApiResponse<User> {
        return try {
            ApiClient.put("admin-management/users/$id/suspend", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to suspend user")
        }
    }

    suspend fun unsuspendUser(id: Long): ApiResponse<User> {
        return try {
            ApiClient.put("admin-management/users/$id/unsuspend", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to unsuspend user")
        }
    }

    suspend fun suspendProperty(id: Long): ApiResponse<Unit> {
        return try {
            ApiClient.put("rooms/admin/$id/suspend", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to suspend property")
        }
    }

    suspend fun unsuspendProperty(id: Long): ApiResponse<Unit> {
        return try {
            ApiClient.put("rooms/admin/$id/unsuspend", Unit)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to unsuspend property")
        }
    }

    suspend fun getAllBookingsAdmin(): ApiResponse<List<org.com.model.Booking>> {
        return try {
            ApiClient.get("admin-management/bookings")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch bookings")
        }
    }

    suspend fun getSystemLogs(): ApiResponse<List<org.com.model.SystemLog>> {
        return try {
            ApiClient.get("admin-management/system-logs")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch system logs")
        }
    }

    suspend fun getAdministrators(): ApiResponse<List<User>> {
        return try {
            ApiClient.get("admin-management/administrators")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch administrators")
        }
    }

    suspend fun createAdmin(user: User): ApiResponse<User> {
        return try {
            ApiClient.post("admin-management/administrators", user)
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to create administrator")
        }
    }

    suspend fun getAdminStats(): ApiResponse<AdminStats> {
        return try {
            ApiClient.get("admin-management/stats")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to fetch admin stats")
        }
    }

    suspend fun deleteAdmin(id: Long): ApiResponse<Unit> {
        return try {
            ApiClient.delete("admin-management/administrators/$id")
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Failed to delete administrator")
        }
    }
}