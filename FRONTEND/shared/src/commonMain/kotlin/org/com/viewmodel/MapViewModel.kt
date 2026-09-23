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

    var filterType by mutableStateOf<String?>(null)
    var filterArea by mutableStateOf<String?>(null)
    var filterMaxPrice by mutableStateOf<Double?>(null)
    var filterStatus by mutableStateOf<String?>(null)

    var shouldFitBounds by mutableStateOf(false)

    var viewedRoomIds by mutableStateOf(setOf<Long>())
        private set

    var savedRoomIds by mutableStateOf(setOf<Long>())
        private set

    val filteredRooms: List<Room>
        get() {
            return rooms.filter { room ->
                val matchesType = filterType.isNullOrBlank() || 
                    room.propertyType?.equals(filterType, ignoreCase = true) == true
                
                val matchesArea = filterArea.isNullOrBlank() || 
                    room.address?.contains(filterArea!!, ignoreCase = true) == true || 
                    room.title?.contains(filterArea!!, ignoreCase = true) == true
                
                val matchesPrice = filterMaxPrice == null || room.price <= filterMaxPrice!!
                
                // Status filtering logic:
                // ALL / null / blank -> All properties for current role
                // AVAILABLE -> ONLY Available
                // PENDING -> ONLY Pending
                // RENTED -> ONLY Rented
                val matchesStatus = when (filterStatus?.uppercase()?.trim()) {
                    "AVAILABLE" -> (room.status ?: "").uppercase().trim() == "AVAILABLE"
                    "PENDING" -> (room.status ?: "").uppercase().trim() == "PENDING"
                    "RENTED" -> (room.status ?: "").uppercase().trim() == "RENTED"
                    else -> true // ALL
                }
                
                matchesType && matchesArea && matchesPrice && matchesStatus
            }
        }

    fun setFilters(type: String?, area: String?, maxPrice: Double?, status: String? = null) {
        filterType = if (type.isNullOrBlank() || type.equals("ALL", ignoreCase = true)) null else type
        filterArea = if (area.isNullOrBlank()) null else area
        filterMaxPrice = maxPrice
        filterStatus = if (status.isNullOrBlank() || status.equals("ALL", ignoreCase = true)) null else status
        
        // Trigger automatic zoom if area is specified
        if (!filterArea.isNullOrBlank()) {
            shouldFitBounds = true
        }
    }

    fun clearFitBounds() {
        shouldFitBounds = false
    }

    fun clearFilters() {
        filterType = null
        filterArea = null
        filterMaxPrice = null
        filterStatus = null
        shouldFitBounds = false
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

                // Keep all rooms
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
        selectedRoom = room
        markRoomAsViewed(room.id)
    }

    fun markRoomAsViewed(roomId: Long?) {
        roomId?.let { id ->
            viewedRoomIds = viewedRoomIds + id
        }
    }

    fun toggleSavedRoom(roomId: Long?) {
        roomId?.let { id ->
            savedRoomIds = if (savedRoomIds.contains(id)) {
                savedRoomIds - id
            } else {
                savedRoomIds + id
            }
        }
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
