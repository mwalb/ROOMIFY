package org.com.network

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.com.model.ApiResponse
import org.com.model.Booking

class BookingApi {
    private val client = ApiClient.client

    suspend fun createBooking(booking: Booking): ApiResponse<Booking>? {
        return try {
            client.post("bookings") {
                setBody(booking)
                contentType(ContentType.Application.Json)
            }.body<ApiResponse<Booking>>()
        } catch (e: Exception) {
            println("BookingApi: Create booking failed: ${e.message}")
            null
        }
    }

    suspend fun getUserBookings(userId: Long): List<Booking> {
        return try {
            val response = client.get("bookings/user/$userId").body<ApiResponse<List<Booking>>>()
            response.data ?: emptyList()
        } catch (e: Exception) {
            println("BookingApi: Get user bookings failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getOwnerBookings(ownerId: Long): List<Booking> {
        return try {
            val response = client.get("bookings/owner/$ownerId").body<ApiResponse<List<Booking>>>()
            response.data ?: emptyList()
        } catch (e: Exception) {
            println("BookingApi: Get owner bookings failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun acceptBooking(bookingId: Long): Boolean {
        return try {
            val response = client.put("bookings/$bookingId/accept").body<ApiResponse<Booking>>()
            response.success
        } catch (e: Exception) {
            false
        }
    }

    suspend fun rejectBooking(bookingId: Long): Boolean {
        return try {
            val response = client.put("bookings/$bookingId/reject").body<ApiResponse<Booking>>()
            response.success
        } catch (e: Exception) {
            false
        }
    }
}
