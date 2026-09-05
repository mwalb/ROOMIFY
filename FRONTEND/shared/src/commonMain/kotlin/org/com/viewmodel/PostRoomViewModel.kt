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
        if (state.title.isBlank() || state.price.isBlank() || state.contactPhone.isBlank()) {
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
                    val roomId = result.id!!
                    
                    // 1. Upload Images
                    if (state.images.isNotEmpty()) {
                        val imageBytes = state.images.map { it.readByteArray(context) }
                        roomApi.uploadImages(roomId, imageBytes)
                    }
                    
                    // 2. Upload Video
                    state.video?.let {
                        roomApi.uploadVideo(roomId, it.readByteArray(context))
                    }
                    
                    // 3. Upload Contract
                    state.contract?.let {
                        roomApi.uploadContract(roomId, it.readByteArray(context))
                    }

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = if (state.isEditing) "Your property has been updated successfully!" else "Your property \"${state.title}\" has been posted successfully and is now visible on the map!"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Failed to save room. Please try again."
                    )
                    onError("Failed to save room. Please try again.")
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
