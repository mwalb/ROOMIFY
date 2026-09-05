package org.com.network

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.com.model.Room

class RoomApi {

    private val client =
        ApiClient.client

    /*
     * =========================================================
     * GET ALL ROOMS
     * =========================================================
     *
     * ApiClient already contains:
     *
     * http://localhost:8080/api
     *
     * Therefore:
     *
     * get("rooms")
     *
     * becomes:
     *
     * http://localhost:8080/api/rooms
     */

    suspend fun getAllRooms(): List<Room> {

        println(
            "RoomApi: GET /api/rooms"
        )

        return client
            .get("rooms")
            .body<List<Room>>()
            .also { rooms ->

                println(
                    "RoomApi: received ${rooms.size} rooms"
                )
            }
    }


    /*
     * =========================================================
     * SEARCH
     * =========================================================
     */

    suspend fun searchRooms(
        query: String
    ): List<Room> {

        println(
            "RoomApi: GET /api/rooms/search?q=$query"
        )

        return client
            .get("rooms/search") {

                parameter(
                    "q",
                    query
                )
            }
            .body<List<Room>>()
    }


    /*
     * =========================================================
     * GET ROOM BY ID
     * =========================================================
     */

    suspend fun getRoomById(
        id: Long
    ): Room? {

        return try {

            client
                .get("rooms/$id")
                .body<Room>()

        } catch (e: Exception) {

            println(
                "RoomApi: failed to fetch room $id: ${e.message}"
            )

            null
        }
    }

    /*
     * =========================================================
     * CREATE ROOM
     * =========================================================
     */

    suspend fun createRoom(
        room: Room
    ): Room? {

        println(
            "RoomApi: POST /api/rooms"
        )

        return try {

            val response =
                client.post("rooms") {
                    setBody(room)
                    contentType(ContentType.Application.Json)
                }

            response.body<Room>()

        } catch (e: Exception) {

            println(
                "RoomApi: failed to create room: ${e.message}"
            )

            null
        }
    }

    /*
     * =========================================================
     * UPDATE ROOM
     * =========================================================
     */

    suspend fun updateRoom(
        id: Long,
        room: Room
    ): Room? {

        println(
            "RoomApi: PUT /api/rooms/$id"
        )

        return try {

            val response =
                client.put("rooms/$id") {
                    setBody(room)
                    contentType(ContentType.Application.Json)
                }

            response.body<Room>()

        } catch (e: Exception) {

            println(
                "RoomApi: failed to update room $id: ${e.message}"
            )

            null
        }
    }

    /*
     * =========================================================
     * DELETE ROOM
     * =========================================================
     */

    suspend fun deleteRoom(
        id: Long
    ): Boolean {

        println(
            "RoomApi: DELETE /api/rooms/$id"
        )

        return try {

            val response = client.delete("rooms/$id")
            response.status.value in 200..299

        } catch (e: Exception) {

            println(
                "RoomApi: failed to delete room $id: ${e.message}"
            )

            false
        }
    }

    /*
     * =========================================================
     * MEDIA UPLOADS
     * =========================================================
     */

    suspend fun uploadImages(roomId: Long, imageBytes: List<ByteArray>): Boolean {
        return try {
            val response = client.post("rooms/$roomId/images") {
                setBody(
                    io.ktor.client.request.forms.MultiPartFormDataContent(
                        io.ktor.client.request.forms.formData {
                            imageBytes.forEachIndexed { index, bytes ->
                                append("images", bytes, io.ktor.http.Headers.build {
                                    append(io.ktor.http.HttpHeaders.ContentType, "image/jpeg")
                                    append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=\"image_$index.jpg\"")
                                })
                            }
                        }
                    )
                )
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("RoomApi: Image upload failed: ${e.message}")
            false
        }
    }

    suspend fun uploadVideo(roomId: Long, videoBytes: ByteArray): Boolean {
        return try {
            val response = client.post("rooms/$roomId/video") {
                setBody(
                    io.ktor.client.request.forms.MultiPartFormDataContent(
                        io.ktor.client.request.forms.formData {
                            append("video", videoBytes, io.ktor.http.Headers.build {
                                append(io.ktor.http.HttpHeaders.ContentType, "video/mp4")
                                append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=\"video.mp4\"")
                            })
                        }
                    )
                )
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("RoomApi: Video upload failed: ${e.message}")
            false
        }
    }

    suspend fun uploadContract(roomId: Long, contractBytes: ByteArray): Boolean {
        return try {
            val response = client.post("rooms/$roomId/contract") {
                setBody(
                    io.ktor.client.request.forms.MultiPartFormDataContent(
                        io.ktor.client.request.forms.formData {
                            append("contract", contractBytes, io.ktor.http.Headers.build {
                                append(io.ktor.http.HttpHeaders.ContentType, "application/pdf")
                                append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=\"contract.pdf\"")
                            })
                        }
                    )
                )
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            println("RoomApi: Contract upload failed: ${e.message}")
            false
        }
    }

    /*
     * =========================================================
     * INTERESTED
     * =========================================================
     */

    suspend fun incrementInterested(roomId: Long): Boolean {
        return try {
            val response = client.post("rooms/$roomId/interested")
            response.status.value in 200..299
        } catch (e: Exception) {
            println("RoomApi: Failed to increment interested count: ${e.message}")
            false
        }
    }
}
