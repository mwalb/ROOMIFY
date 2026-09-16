package org.com.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.com.auth.AuthManager
import org.com.auth.AuthState
import org.com.model.Room
import org.com.network.RoomApi
import org.com.ui.AddressResult
import org.com.ui.LocationInputMode
import org.com.ui.PropertyFormState
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.core.PlatformContext

import org.com.network.RoomifyApi
import org.com.model.Property

class PostRoomViewModel(
    private val roomApi: RoomApi,
    private val authManager: AuthManager,
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(PropertyFormState())
    val uiState: StateFlow<PropertyFormState> = _uiState.asStateFlow()

    init {
        // Pre-fill owner name if logged in
        val state = authManager.authState.value
        if (state is AuthState.Authenticated) {
            _uiState.value = _uiState.value.copy(ownerName = state.user.name)
        }
    }

    fun startEditing(room: Room) {
        _uiState.value = PropertyFormState(
            roomId = room.id,
            isEditing = true,
            ownerName = room.ownerName ?: "",
            latitude = room.latitude.toString(),
            longitude = room.longitude.toString(),
            selectedAddress = room.address ?: "",
            manualAddress = room.address ?: "",
            title = room.title ?: "",
            description = room.description ?: "",
            price = room.price.toString(),
            propertyType = room.propertyType ?: "",
            rooms = room.roomsCount.toString(),
            bathrooms = room.bathroomsCount.toString(),
            area = room.area.toString(),
            maxGuests = room.maxGuests.toString(),
            selectedAmenities = room.amenities.toSet(),
            rules = room.rules.firstOrNull() ?: "",
            contactPhone = room.contactPhone ?: "",
            contactEmail = room.contactEmail ?: "",
            existingImages = room.images,
            existingVideo = room.videoUrl,
            existingContract = room.contractUrl,
            videoSelected = room.hasVideo,
            contractSelected = room.hasContract
        )
    }

    fun onOwnerNameChange(name: String) {
        _uiState.value = _uiState.value.copy(ownerName = name)
    }

    fun onPostModeChange(mode: org.com.ui.PostMode) {
        _uiState.value = _uiState.value.copy(postMode = mode)
    }

    fun onNumFloorsChange(num: String) {
        val n = num.toIntOrNull() ?: 1
        val currentConfigs = _uiState.value.floorConfigs
        val newConfigs = (1..n).map { floorNum ->
            currentConfigs.find { it.floorNumber == floorNum } ?: org.com.ui.FloorConfig(floorNumber = floorNum)
        }
        _uiState.value = _uiState.value.copy(numFloors = num, floorConfigs = newConfigs)
    }

    fun updateFloorConfig(floorNum: Int, update: (org.com.ui.FloorConfig) -> org.com.ui.FloorConfig) {
        val updated = _uiState.value.floorConfigs.map {
            if (it.floorNumber == floorNum) update(it) else it
        }
        _uiState.value = _uiState.value.copy(floorConfigs = updated)
    }

    fun updateRoomTemplate(id: String, update: (org.com.ui.RoomTemplate) -> org.com.ui.RoomTemplate) {
        val updated = _uiState.value.roomTemplates.map {
            if (it.id == id) update(it) else it
        }
        _uiState.value = _uiState.value.copy(roomTemplates = updated)
    }

    fun addRoomTemplate() {
        val id = "tmpl_" + org.com.currentTimeMillis()
        val newList = _uiState.value.roomTemplates + org.com.ui.RoomTemplate(id = id, name = "New Template")
        _uiState.value = _uiState.value.copy(roomTemplates = newList)
    }

    fun generateUnits(floorNum: Int, startNum: Int, count: Int, templateId: String) {
        val template = _uiState.value.roomTemplates.find { it.id == templateId } ?: return
        val currentUnits = _uiState.value.generatedUnits.toMutableList()

        for (i in 0 until count) {
            val unitNum = startNum + i
            val title = "${_uiState.value.title} - $unitNum"
            val room = Room(
                title = title,
                price = template.price.toDoubleOrNull() ?: 0.0,
                propertyType = template.type,
                roomsCount = template.rooms.toIntOrNull() ?: 1,
                bathroomsCount = template.baths.toIntOrNull() ?: 1,
                area = template.area.toDoubleOrNull() ?: 0.0,
                amenities = template.amenities.toList(),
                floorNumber = floorNum,
                unitNumber = unitNum.toString(),
                status = "AVAILABLE"
            )
            currentUnits.add(room)
        }
        _uiState.value = _uiState.value.copy(generatedUnits = currentUnits)
    }

    fun toggleUnitSelection(index: Int) {
        val current = _uiState.value.selectedUnits
        val updated = if (index in current) current - index else current + index
        _uiState.value = _uiState.value.copy(selectedUnits = updated)
    }

    fun selectAllUnits() {
        _uiState.value = _uiState.value.copy(selectedUnits = _uiState.value.generatedUnits.indices.toSet())
    }

    fun clearUnitSelection() {
        _uiState.value = _uiState.value.copy(selectedUnits = emptySet())
    }

    fun bulkUpdateUnits(price: String?, status: String?, type: String?) {
        val selected = _uiState.value.selectedUnits
        val updated = _uiState.value.generatedUnits.mapIndexed { index, room ->
            if (index in selected) {
                room.copy(
                    price = price?.toDoubleOrNull() ?: room.price,
                    status = status ?: room.status,
                    propertyType = type ?: room.propertyType
                )
            } else room
        }
        _uiState.value = _uiState.value.copy(generatedUnits = updated)
    }

    fun deleteSelectedUnits() {
        val selected = _uiState.value.selectedUnits
        val remaining = _uiState.value.generatedUnits.filterIndexed { index, _ -> index !in selected }
        _uiState.value = _uiState.value.copy(generatedUnits = remaining, selectedUnits = emptySet())
    }

    fun onUnitImagesSelected(index: Int, files: List<KmpFile>) {
        val current = _uiState.value.unitImages.toMutableMap()
        current[index] = files
        _uiState.value = _uiState.value.copy(unitImages = current)
    }

    fun onLocationModeChange(mode: LocationInputMode) {
        _uiState.value = _uiState.value.copy(locationMode = mode)
    }

    fun onManualAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(manualAddress = address)
    }

    fun onLatitudeChange(lat: String) {
        _uiState.value = _uiState.value.copy(latitude = lat)
    }

    fun onLongitudeChange(lng: String) {
        _uiState.value = _uiState.value.copy(longitude = lng)
    }

    fun onTitleChange(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(price = price)
    }

    fun onPropertyTypeChange(type: String) {
        _uiState.value = _uiState.value.copy(propertyType = type)
    }

    fun onRoomsChange(rooms: String) {
        _uiState.value = _uiState.value.copy(rooms = rooms)
    }

    fun onBathroomsChange(bathrooms: String) {
        _uiState.value = _uiState.value.copy(bathrooms = bathrooms)
    }

    fun onAreaChange(area: String) {
        _uiState.value = _uiState.value.copy(area = area)
    }

    fun onMaxGuestsChange(maxGuests: String) {
        _uiState.value = _uiState.value.copy(maxGuests = maxGuests)
    }

    fun onToggleAmenity(amenity: String) {
        val current = _uiState.value.selectedAmenities
        val updated = if (amenity in current) current - amenity else current + amenity
        _uiState.value = _uiState.value.copy(selectedAmenities = updated)
    }

    fun onRulesChange(rules: String) {
        _uiState.value = _uiState.value.copy(rules = rules)
    }

    fun onContactPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(contactPhone = phone)
    }

    fun onContactEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(contactEmail = email)
    }

    fun onLocationSelected(result: AddressResult) {
        _uiState.value = _uiState.value.copy(
            selectedAddress = result.formattedAddress,
            latitude = result.latitude.toString(),
            longitude = result.longitude.toString(),
            manualAddress = result.address
        )
    }

    fun onImagesSelected(files: List<KmpFile>) {
        _uiState.value = _uiState.value.copy(images = files)
    }

    fun onVideoSelected(file: KmpFile) {
        _uiState.value = _uiState.value.copy(video = file, videoSelected = true)
    }

    fun onContractSelected(file: KmpFile) {
        _uiState.value = _uiState.value.copy(contract = file, contractSelected = true)
    }

    fun onAddVideo() {
        _uiState.value = _uiState.value.copy(videoSelected = true)
    }

    fun onAddContract() {
        _uiState.value = _uiState.value.copy(contractSelected = true)
    }

    fun reset() {
        _uiState.value = PropertyFormState()
        // Re-fill owner name if logged in
        val state = authManager.authState.value
        if (state is AuthState.Authenticated) {
            _uiState.value = _uiState.value.copy(ownerName = state.user.name)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun submit(context: PlatformContext, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        
        // Basic validation
        if (state.title.isBlank() || (state.postMode == org.com.ui.PostMode.SINGLE && state.price.isBlank()) || state.contactPhone.isBlank()) {
            onError("Please fill in all required fields (Title, Price, Phone)")
            return
        }

        val auth = authManager.authState.value
        if (auth !is AuthState.Authenticated) {
            onError("You must be logged in to post a property")
            return
        }

        _uiState.value = _uiState.value.copy(isSubmitting = true)

        scope.launch {
            try {
                if (state.postMode == org.com.ui.PostMode.SINGLE) {
                    val room = Room(
                        id = state.roomId,
                        title = state.title,
                        description = state.description,
                        price = state.price.toDoubleOrNull() ?: 0.0,
                        propertyType = state.propertyType,
                        latitude = state.latitude.toDoubleOrNull() ?: 0.0,
                        longitude = state.longitude.toDoubleOrNull() ?: 0.0,
                        address = state.selectedAddress,
                        postedBy = auth.user.id,
                        ownerName = state.ownerName,
                        contactPhone = state.contactPhone,
                        contactEmail = state.contactEmail,
                        roomsCount = state.rooms.toIntOrNull() ?: 1,
                        bathroomsCount = state.bathrooms.toIntOrNull() ?: 1,
                        area = state.area.toDoubleOrNull() ?: 0.0,
                        maxGuests = state.maxGuests.toIntOrNull() ?: 1,
                        amenities = state.selectedAmenities.toList(),
                        rules = listOf(state.rules),
                        images = state.existingImages,
                        videoUrl = state.existingVideo,
                        contractUrl = state.existingContract,
                        hasVideo = state.videoSelected,
                        hasContract = state.contractSelected,
                        status = "AVAILABLE"
                    )

                    val result = if (state.isEditing && state.roomId != null) {
                        roomApi.updateRoom(state.roomId, room)
                    } else {
                        roomApi.createRoom(room)
                    }

                    if (result != null && result.id != null) {
                        handleMediaUploads(result.id!!, context)
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            successMessage = "Your property \"${state.title}\" has been posted successfully!"
                        )
                    }
                } else {
                    // BUILDING MODE
                    val property = Property(
                        title = state.title,
                        description = state.description,
                        address = state.selectedAddress,
                        latitude = state.latitude.toDoubleOrNull() ?: 0.0,
                        longitude = state.longitude.toDoubleOrNull() ?: 0.0,
                        ownerId = auth.user.id,
                        ownerName = state.ownerName,
                        contactPhone = state.contactPhone,
                        contactEmail = state.contactEmail,
                        propertyType = "BUILDING",
                        hasVideo = state.videoSelected,
                        hasContract = state.contractSelected,
                        units = state.generatedUnits.map { u ->
                            u.copy(
                                postedBy = auth.user.id,
                                ownerName = state.ownerName,
                                contactPhone = state.contactPhone,
                                contactEmail = state.contactEmail,
                                address = state.selectedAddress,
                                latitude = state.latitude.toDoubleOrNull() ?: 0.0,
                                longitude = state.longitude.toDoubleOrNull() ?: 0.0
                            )
                        }
                    )

                    val result = RoomifyApi.createProperty(property)
                    if (result.success && result.data != null) {
                        val savedProperty = result.data!!
                        
                        // Upload Unit Images if any
                        savedProperty.units.forEachIndexed { index, savedUnit ->
                            val unitImages = state.unitImages[index]
                            if (!unitImages.isNullOrEmpty() && savedUnit.id != null) {
                                val imageBytes = unitImages.map { it.readByteArray(context) }
                                roomApi.uploadImages(savedUnit.id!!, imageBytes)
                            }
                        }

                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            successMessage = "Building \"${state.title}\" with ${state.generatedUnits.size} units has been created!"
                        )
                    } else {
                        throw Exception(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "An unexpected error occurred"
                )
                onError(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private suspend fun handleMediaUploads(roomId: Long, context: PlatformContext) {
        val state = _uiState.value
        if (state.images.isNotEmpty()) {
            val imageBytes = state.images.map { it.readByteArray(context) }
            roomApi.uploadImages(roomId, imageBytes)
        }
        state.video?.let { roomApi.uploadVideo(roomId, it.readByteArray(context)) }
        state.contract?.let { roomApi.uploadContract(roomId, it.readByteArray(context)) }
    }

    fun deleteRoom(roomId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.value = _uiState.value.copy(isSubmitting = true)
        scope.launch {
            try {
                val success = roomApi.deleteRoom(roomId)
                if (success) {
                    _uiState.value = _uiState.value.copy(isSubmitting = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = "Failed to delete room")
                    onError("Failed to delete room")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = e.message)
                onError(e.message ?: "An error occurred")
            }
        }
    }
}
