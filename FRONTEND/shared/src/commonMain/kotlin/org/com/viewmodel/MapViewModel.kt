package org.com.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.com.model.Room
import org.com.network.RoomApi

enum class MapDetail {
    MINIMAL,
    STANDARD,
    DETAILED
}

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

    var filterType by mutableStateOf<String?>(null)
    var filterArea by mutableStateOf<String?>(null)
    var filterMaxPrice by mutableStateOf<Double?>(null)
    var filterStatus by mutableStateOf<String?>(null)
    var mapDetail by mutableStateOf(MapDetail.STANDARD)

    val filteredRooms: List<Room>
        get() {
            return rooms.filter { room ->
                val matchesType = filterType == null || room.propertyType?.uppercase() == filterType?.uppercase()
                
                val matchesArea = filterArea == null || 
                    room.address?.contains(filterArea!!, ignoreCase = true) == true || 
                    room.title?.contains(filterArea!!, ignoreCase = true) == true
                
                val matchesPrice = filterMaxPrice == null || room.price <= filterMaxPrice!!
                
                // Status filtering logic:
                // If filterStatus is null (All), show everything.
                // If filterStatus is "AVAILABLE", show AVAILABLE + RENTED.
                // If filterStatus is "PENDING", show PENDING + RENTED.
                // If filterStatus is "RENTED", show RENTED.
                // Rented properties are ALWAYS visible if they match other criteria.
                val matchesStatus = when (filterStatus?.uppercase()) {
                    "AVAILABLE" -> room.status.uppercase() == "AVAILABLE" || room.status.uppercase() == "RENTED"
                    "PENDING" -> room.status.uppercase() == "PENDING" || room.status.uppercase() == "RENTED"
                    "RENTED" -> room.status.uppercase() == "RENTED"
                    else -> true // ALL
                }
                
                matchesType && matchesArea && matchesPrice && matchesStatus
            }
        }

    fun setFilters(type: String?, area: String?, maxPrice: Double?, status: String? = null, detail: MapDetail = MapDetail.STANDARD) {
        filterType = type
        filterArea = area
        filterMaxPrice = maxPrice
        filterStatus = status
        mapDetail = detail
    }

    fun clearFilters() {
        filterType = null
        filterArea = null
        filterMaxPrice = null
        filterStatus = null
        mapDetail = MapDetail.STANDARD
    }


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

                // Keep all rooms including RENTED
                rooms = result

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