package org.com.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.com.model.Room
import org.com.network.RoomApi

class RoomViewModel(
    private val roomApi: RoomApi,
    private val scope: CoroutineScope
) {

    var rooms by mutableStateOf<List<Room>>(emptyList())
        private set

    var selectedRoom by mutableStateOf<Room?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var filterType by mutableStateOf<String?>(null)
    var filterArea by mutableStateOf<String?>(null)
    var filterMaxPrice by mutableStateOf<Double?>(null)
    var filterStatus by mutableStateOf<String?>(null)

    var viewedRoomIds by mutableStateOf(setOf<Long>())
        private set

    var savedRoomIds by mutableStateOf(setOf<Long>())
        private set

    val filteredRooms: List<Room>
        get() {
            return rooms.filter { room ->
                val matchesType = filterType == null || room.propertyType?.uppercase() == filterType?.uppercase()
                
                val matchesArea = filterArea == null || 
                    room.address?.contains(filterArea!!, ignoreCase = true) == true || 
                    room.title?.contains(filterArea!!, ignoreCase = true) == true
                
                val matchesPrice = filterMaxPrice == null || room.price <= filterMaxPrice!!
                
                val matchesStatus = when (filterStatus?.uppercase()) {
                    "AVAILABLE" -> room.status.uppercase() == "AVAILABLE" || room.status.uppercase() == "RENTED"
                    "PENDING" -> room.status.uppercase() == "PENDING" || room.status.uppercase() == "RENTED"
                    "RENTED" -> room.status.uppercase() == "RENTED"
                    else -> true
                }
                
                matchesType && matchesArea && matchesPrice && matchesStatus
            }
        }

    fun setFilters(type: String?, area: String?, maxPrice: Double?, status: String? = null) {
        filterType = type
        filterArea = area
        filterMaxPrice = maxPrice
        filterStatus = status
    }

    fun clearFilters() {
        filterType = null
        filterArea = null
        filterMaxPrice = null
        filterStatus = null
    }

    fun loadRooms() {
        scope.launch {
            isLoading = true
            error = null
            try {
                rooms = roomApi.getAllRooms()
                if (rooms.isEmpty()) {
                    error = "No rooms are currently available."
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load rooms"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectRoom(room: Room) {
        selectedRoom = room
        markRoomAsViewed(room.id)
    }

    fun markRoomAsViewed(roomId: Long?) {
        roomId?.let { id -> viewedRoomIds = viewedRoomIds + id }
    }

    fun toggleSavedRoom(roomId: Long?) {
        roomId?.let { id ->
            savedRoomIds = if (savedRoomIds.contains(id)) savedRoomIds - id else savedRoomIds + id
        }
    }

    fun clearSelectedRoom() {
        selectedRoom = null
    }

    fun refreshRooms() {
        loadRooms()
    }
}
