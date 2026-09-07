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

    val filteredRooms: List<Room>
        get() {
            var filtered = rooms
            
            filterType?.let { type ->
                filtered = filtered.filter { it.propertyType?.uppercase() == type.uppercase() }
            }
            
            filterArea?.let { area ->
                filtered = filtered.filter { 
                    it.address?.contains(area, ignoreCase = true) == true || 
                    it.title?.contains(area, ignoreCase = true) == true 
                }
            }
            
            filterMaxPrice?.let { max ->
                filtered = filtered.filter { it.price <= max }
            }
            
            return filtered
        }

    fun setFilters(type: String?, area: String?, maxPrice: Double?) {
        filterType = type
        filterArea = area
        filterMaxPrice = maxPrice
    }

    fun clearFilters() {
        filterType = null
        filterArea = null
        filterMaxPrice = null
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