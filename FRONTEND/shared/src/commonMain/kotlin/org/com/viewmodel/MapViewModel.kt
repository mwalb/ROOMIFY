package org.com.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.com.model.Room
import org.com.network.RoomApi

class MapViewModel(
    private val roomApi: RoomApi,
    private val scope: CoroutineScope
) {

    var rooms by mutableStateOf<List<Room>>(
        emptyList()
    )
        private set

    var selectedRoom by mutableStateOf<Room?>(
        null
    )
        private set

    var isLoading by mutableStateOf(
        false
    )
        private set

    var error by mutableStateOf<String?>(
        null
    )
        private set


    /*
     * =========================================================
     * LOAD ROOMS
     * =========================================================
     */

    fun loadRooms() {

        println(
            "MapViewModel: loadRooms() STARTED"
        )

        scope.launch {

            isLoading = true
            error = null

            try {

                println(
                    "MapViewModel: requesting /rooms"
                )

                val result =
                    roomApi.getAllRooms()

                // Filter out rented rooms for the map
                rooms = result.filter { it.status.uppercase() != "RENTED" }

                println(
                    "MapViewModel: received ${result.size} rooms"
                )

                if (result.isEmpty()) {

                    error =
                        "No rooms are currently available."
                }

            } catch (e: Exception) {

                println(
                    "MapViewModel: FAILED"
                )

                println(
                    "MapViewModel: ${e.message}"
                )

                error =
                    e.message
                        ?: "Failed to load rooms"

            } finally {

                isLoading = false

                println(
                    "MapViewModel: loadRooms() FINISHED"
                )
            }
        }
    }


    /*
     * =========================================================
     * SELECT ROOM
     * =========================================================
     */

    fun selectRoom(
        room: Room
    ) {

        selectedRoom =
            room
    }


    /*
     * =========================================================
     * CLEAR SELECTION
     * =========================================================
     */

    fun clearSelectedRoom() {

        selectedRoom =
            null
    }


    /*
     * =========================================================
     * REFRESH
     * =========================================================
     */

    fun refreshRooms() {

        loadRooms()
    }
}