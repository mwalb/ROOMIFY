package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.com.i18n.LocalRoomifyStrings
import org.com.network.ApiClient

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)

enum class LocationInputMode { MAP, MANUAL }
enum class PostMode { SINGLE, BUILDING }

data class FloorConfig(
    val floorNumber: Int = 1,
    val numRooms: Int = 0,
    val startRoomNumber: Int = 1,
    val prefix: String = ""
)

data class RoomTemplate(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val type: String = "Apartment",
    val amenities: Set<String> = emptySet(),
    val rooms: String = "1",
    val baths: String = "1",
    val area: String = ""
)

data class PropertyFormState(
    val postMode: PostMode = PostMode.SINGLE,
    val roomId: Long? = null,
    val isEditing: Boolean = false,
    val ownerName: String = "",
    val locationMode: LocationInputMode = if (isMapTabSupported()) LocationInputMode.MAP else LocationInputMode.MANUAL,
    val manualAddress: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val selectedAddress: String = "No location selected",
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val propertyType: String = "Apartment",
    val rooms: String = "",
    val bathrooms: String = "",
    val area: String = "",
    val maxGuests: String = "1",
    val selectedAmenities: Set<String> = emptySet(),
    val rules: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val images: List<KmpFile> = emptyList(),
    val existingImages: List<String> = emptyList(),
    val video: KmpFile? = null,
    val existingVideo: String? = null,
    val contract: KmpFile? = null,
    val existingContract: String? = null,
    val videoSelected: Boolean = false,
    val contractSelected: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Building Specific Fields
    val numFloors: String = "1",
    val floorConfigs: List<FloorConfig> = emptyList(),
    val roomTemplates: List<RoomTemplate> = listOf(RoomTemplate(id = "default", name = "Standard Room")),
    val generatedUnits: List<org.com.model.Room> = emptyList(),
    val unitImages: Map<Int, List<KmpFile>> = emptyMap(), // index in generatedUnits -> list of images
    val selectedUnits: Set<Int> = emptySet(), // Indices of generatedUnits
    val unitSearchQuery: String = "",
    val unitFilterFloor: Int? = null,
    val unitFilterStatus: String? = null
)

private val propertyTypes = listOf("Apartment", "House", "Studio", "Room", "Office")
private val amenitiesList = listOf("WiFi", "Parking", "Air Conditioning", "Furnished", "Water Included", "Electricity Included", "Security", "Balcony", "Pool", "Gym")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRoom(
    state: PropertyFormState,
    onPostModeChange: (PostMode) -> Unit,
    onNumFloorsChange: (String) -> Unit,
    onUpdateFloorConfig: (Int, (FloorConfig) -> FloorConfig) -> Unit,
    onAddRoomTemplate: () -> Unit,
    onUpdateRoomTemplate: (String, (RoomTemplate) -> RoomTemplate) -> Unit,
    onGenerateUnits: (Int, Int, Int, String) -> Unit,
    onToggleUnitSelection: (Int) -> Unit,
    onSelectAllUnits: () -> Unit,
    onClearUnitSelection: () -> Unit,
    onBulkUpdateUnits: (String?, String?, String?) -> Unit,
    onDeleteSelectedUnits: () -> Unit,
    onUnitImagesSelected: (Int, List<KmpFile>) -> Unit,
    onLocationModeChange: (LocationInputMode) -> Unit,
    onManualAddressChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onRoomsChange: (String) -> Unit,
    onBathroomsChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onMaxGuestsChange: (String) -> Unit,
    onToggleAmenity: (String) -> Unit,
    onRulesChange: (String) -> Unit,
    onContactPhoneChange: (String) -> Unit,
    onContactEmailChange: (String) -> Unit,
    onLocationSelected: (AddressResult) -> Unit,
    onImagesSelected: (List<KmpFile>) -> Unit,
    onVideoSelected: (KmpFile) -> Unit,
    onContractSelected: (KmpFile) -> Unit,
    onSubmit: () -> Unit,
    onSuccessDismissed: () -> Unit,
    onErrorDismissed: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalRoomifyStrings.current
    var currentStep by remember { mutableStateOf(1) }
    val totalSteps = if (state.postMode == PostMode.SINGLE) 4 else 6
    val scrollState = rememberScrollState()

    // Animation for card entry
    val cardAlpha = remember { Animatable(0f) }
    val cardScale = remember { Animatable(0.9f) }

    LaunchedEffect(Unit) {
        cardAlpha.animateTo(1f, tween(500))
        cardScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    val imagesPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { onImagesSelected(it) }
    )

    val videoPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Video,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files -> files.firstOrNull()?.let { onVideoSelected(it) } }
    )

    val contractPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Pdf,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files -> files.firstOrNull()?.let { onContractSelected(it) } }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.1f }
        ) {
            repeat(5) { i ->
                Box(
                    modifier = Modifier
                        .size((100 * (i + 1)).dp)
                        .align(if (i % 2 == 0) Alignment.TopStart else Alignment.BottomEnd)
                        .offset(x = if (i % 2 == 0) (-50).dp else 50.dp, y = if (i % 2 == 0) (-50).dp else 50.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }

        // One large main Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 500.dp)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp)
                .graphicsLayer {
                    alpha = cardAlpha.value
                    scaleX = cardScale.value
                    scaleY = cardScale.value
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header inside the Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryLight)))
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = strings.postARoom,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$currentStep/$totalSteps", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Progress Indicator
                StepProgressBar(currentStep = currentStep, totalSteps = totalSteps)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { it } + fadeIn(tween(400))).togetherWith(slideOutHorizontally { -it } + fadeOut(tween(400)))
                            } else {
                                (slideInHorizontally { -it } + fadeIn(tween(400))).togetherWith(slideOutHorizontally { it } + fadeOut(tween(400)))
                            }
                        },
                        label = "stepTransition"
                    ) { step ->
                        if (state.postMode == PostMode.SINGLE) {
                            when (step) {
                                1 -> StepBasicInfo(state, onPostModeChange, onTitleChange, onDescriptionChange, onPriceChange, onPropertyTypeChange)
                                2 -> StepDetails(state, onRoomsChange, onBathroomsChange, onAreaChange, onMaxGuestsChange, onToggleAmenity)
                                3 -> StepLocation(state, onLocationModeChange, onManualAddressChange, onLatitudeChange, onLongitudeChange, onLocationSelected)
                                4 -> StepMediaAndContact(state, { imagesPicker.launch() }, { videoPicker.launch() }, { contractPicker.launch() }, onContactPhoneChange, onContactEmailChange, onRulesChange)
                            }
                        } else {
                            when (step) {
                                1 -> StepBasicInfo(state, onPostModeChange, onTitleChange, onDescriptionChange, onPriceChange, onPropertyTypeChange)
                                2 -> StepLocation(state, onLocationModeChange, onManualAddressChange, onLatitudeChange, onLongitudeChange, onLocationSelected)
                                3 -> StepFloorsAndTemplates(state, onNumFloorsChange, onUpdateFloorConfig, onAddRoomTemplate, onUpdateRoomTemplate)
                                4 -> StepBulkGeneration(state, onGenerateUnits)
                                5 -> StepReviewUnits(state, onToggleUnitSelection, onSelectAllUnits, onClearUnitSelection, onBulkUpdateUnits, onDeleteSelectedUnits, onUnitImagesSelected)
                                6 -> StepMediaAndContact(state, { imagesPicker.launch() }, { videoPicker.launch() }, { contractPicker.launch() }, onContactPhoneChange, onContactEmailChange, onRulesChange)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // Action Footer inside the Card
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp).weight(1f),
                            border = BorderStroke(1.dp, PrimaryColor)
                        ) {
                            Text("PREVIOUS", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    
                    val canGoNext = when (state.postMode) {
                        PostMode.SINGLE -> when (currentStep) {
                            1 -> state.title.isNotBlank() && state.price.isNotBlank()
                            2 -> state.rooms.isNotBlank() && state.area.isNotBlank()
                            3 -> state.latitude.isNotBlank() && state.longitude.isNotBlank() && state.selectedAddress != "No location selected"
                            else -> true
                        }
                        PostMode.BUILDING -> when (currentStep) {
                            1 -> state.title.isNotBlank()
                            2 -> state.latitude.isNotBlank() && state.longitude.isNotBlank() && state.selectedAddress != "No location selected"
                            3 -> state.numFloors.isNotBlank() && state.numFloors.toIntOrNull()?.let { it > 0 } == true
                            4 -> true // Generation is optional or can be skipped
                            5 -> state.generatedUnits.isNotEmpty()
                            else -> true
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) currentStep++
                            else onSubmit()
                        },
                        enabled = canGoNext,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp).weight(if (currentStep > 1) 2f else 1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text(
                            text = if (currentStep < totalSteps) "NEXT STEP" else "POST PROPERTY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }

    // Overlays
    if (state.isSubmitting) LoadingOverlay(strings.postingYourProperty)
    if (state.errorMessage != null) StatusOverlay(false, state.errorMessage, "Try Again", onErrorDismissed)
    if (state.successMessage != null) StatusOverlay(true, state.successMessage, "Go to Dashboard", onSuccessDismissed)
}

@Composable
private fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isActive = step <= currentStep
            val isCurrent = step == currentStep
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(if (isActive) PrimaryColor else Color(0xFFEEEEEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive && !isCurrent && step < currentStep) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("$step", color = if (isActive) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier.weight(1f).height(2.dp).background(if (step < currentStep) PrimaryColor else Color(0xFFEEEEEE)).padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StepBasicInfo(state: PropertyFormState, onPostModeChange: (PostMode) -> Unit, onTitleChange: (String) -> Unit, onDescriptionChange: (String) -> Unit, onPriceChange: (String) -> Unit, onPropertyTypeChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("What are you posting?")
        
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F2F5), RoundedCornerShape(12.dp)).padding(4.dp)) {
            PostModeOption("Single Room", state.postMode == PostMode.SINGLE, Modifier.weight(1f)) { onPostModeChange(PostMode.SINGLE) }
            PostModeOption("Building / Multiple", state.postMode == PostMode.BUILDING, Modifier.weight(1f)) { onPostModeChange(PostMode.BUILDING) }
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle(if (state.postMode == PostMode.SINGLE) "General Information" else "Property Information")
        ProfessionalTextField(value = state.title, onValueChange = onTitleChange, label = if (state.postMode == PostMode.SINGLE) "Property Title" else "Building/Complex Name", placeholder = "e.g. Modern Apartment")
        ProfessionalTextField(value = state.description, onValueChange = onDescriptionChange, label = "Description", placeholder = "Describe your property...", singleLine = false, minHeight = 100.dp)
        
        if (state.postMode == PostMode.SINGLE) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfessionalTextField(value = state.price, onValueChange = onPriceChange, label = "Price (TZS)", modifier = Modifier.weight(1f), placeholder = "0.00")
                Column(Modifier.weight(1f)) {
                    Text("Type", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                    PropertyTypeDropdown(state.propertyType, onPropertyTypeChange)
                }
            }
        }
    }
}

@Composable
private fun PostModeOption(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PrimaryColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun StepDetails(state: PropertyFormState, onRoomsChange: (String) -> Unit, onBathroomsChange: (String) -> Unit, onAreaChange: (String) -> Unit, onMaxGuestsChange: (String) -> Unit, onToggleAmenity: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Property Details")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfessionalTextField(value = state.rooms, onValueChange = onRoomsChange, label = "Beds", modifier = Modifier.weight(1f), placeholder = "0")
            ProfessionalTextField(value = state.bathrooms, onValueChange = onBathroomsChange, label = "Baths", modifier = Modifier.weight(1f), placeholder = "0")
            ProfessionalTextField(value = state.area, onValueChange = onAreaChange, label = "Area (m²)", modifier = Modifier.weight(1f), placeholder = "0")
        }
        
        ProfessionalTextField(value = state.maxGuests, onValueChange = onMaxGuestsChange, label = "Max Guests Allowed", placeholder = "1")
        
        Column {
            Text("Amenities", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                amenitiesList.forEach { amenity ->
                    AmenityChip(amenity, amenity in state.selectedAmenities) { onToggleAmenity(amenity) }
                }
            }
        }
    }
}

@Composable
private fun StepLocation(
    state: PropertyFormState, 
    onLocationModeChange: (LocationInputMode) -> Unit, 
    onManualAddressChange: (String) -> Unit, 
    onLatitudeChange: (String) -> Unit, 
    onLongitudeChange: (String) -> Unit,
    onLocationSelected: (AddressResult) -> Unit
) {
    val showTabs = isMapTabSupported()
    var showMapModal by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Location")
        
        if (showTabs) {
            // Navigation Tabs (Android only)
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE9EEF1), CircleShape).padding(4.dp)) {
                LocationModeTab("Map View", state.locationMode == LocationInputMode.MAP, Modifier.weight(1f)) { onLocationModeChange(LocationInputMode.MAP) }
                LocationModeTab("Manual", state.locationMode == LocationInputMode.MANUAL, Modifier.weight(1f)) { onLocationModeChange(LocationInputMode.MANUAL) }
            }
        }

        Surface(color = Color(0xFFF8F9FA), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Column(Modifier.padding(if (showTabs && state.locationMode == LocationInputMode.MAP) 0.dp else 16.dp)) {
                if (showTabs && state.locationMode == LocationInputMode.MAP) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        PlatformLocationMap(
                            latitude = state.latitude,
                            longitude = state.longitude,
                            modifier = Modifier.fillMaxSize(),
                            onLocationSelected = { result ->
                                onLatitudeChange(result.latitude.toString())
                                onLongitudeChange(result.longitude.toString())
                                onLocationSelected(result)
                            }
                        )
                    }
                } else {
                    // Manual / Web Location Entry
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Property Address", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.manualAddress,
                                onValueChange = { 
                                    onManualAddressChange(it)
                                },
                                placeholder = { Text("e.g. Mbezi Beach, Dar es Salaam", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryColor)
                            )
                            
                            Button(
                                onClick = { showMapModal = true },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Select Location", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Use My Current Location
                        OutlinedButton(
                            onClick = {
                                // For now, we'll open map modal instead of auto-geocoding here to ensure reliability
                                showMapModal = true
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PrimaryColor)
                        ) {
                            Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Use My Current Location", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Success Confirmation
                        if (state.latitude.isNotBlank() && state.longitude.isNotBlank()) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("✓ Location selected", color = Color(0xFF2E7D32), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Spacer(Modifier.height(8.dp))
                                    
                                    // Known name (Real name)
                                    val displayName = if (state.manualAddress.contains("Location") || state.manualAddress.isBlank()) {
                                        "Property Location Pinned"
                                    } else {
                                        state.manualAddress
                                    }
                                    
                                    Text(
                                        text = displayName,
                                        color = Color(0xFF1B5E20),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(start = 28.dp)
                                    )
                                    
                                    Text(
                                        text = state.selectedAddress, // selectedAddress holds the "full address"
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                                    )
                                    
                                    Spacer(Modifier.height(8.dp))
                                    
                                    val displayLat = state.latitude
                                    val displayLng = state.longitude
                                    
                                    Text(
                                        text = "Latitude: $displayLat\nLongitude: $displayLng",
                                        color = Color(0xFF2E7D32).copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(start = 28.dp)
                                    )
                                    
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = { showMapModal = true },
                                        modifier = Modifier.padding(start = 28.dp).height(38.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                                    ) {
                                        Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Change Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                             Text("No exact location selected", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showMapModal) {
        org.com.ui.components.LocationPickerModal(
            currentLat = state.latitude,
            currentLng = state.longitude,
            initialSearch = state.manualAddress,
            onConfirmed = { result ->
                onLocationSelected(result)
                onLatitudeChange(result.latitude.toString())
                onLongitudeChange(result.longitude.toString())
                onManualAddressChange(result.address) // Use known name
                showMapModal = false
            },
            onDismiss = { showMapModal = false }
        )
    }
}

@Composable
private fun StepMediaAndContact(
    state: PropertyFormState, 
    onAddImages: () -> Unit, 
    onAddVideo: () -> Unit,
    onAddContract: () -> Unit,
    onContactPhoneChange: (String) -> Unit, 
    onContactEmailChange: (String) -> Unit, 
    onRulesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Media & Contact")
        
        Column {
            Text("Photos", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(90.dp).clickable { onAddImages() },
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                if (state.images.isEmpty() && state.existingImages.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = PrimaryColor, modifier = Modifier.size(24.dp))
                        Text("Add Photos", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    LazyRow(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.existingImages) { url ->
                            Box(Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))) {
                                val fullUrl = ApiClient.resolveUrl(url)
                                KamelImage(
                                    resource = { asyncPainterResource(fullUrl) },
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onLoading = { _: Float -> 
                                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryColor)
                                        }
                                    },
                                    onFailure = {
                                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Error, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                )
                            }
                        }
                        items(state.images) { _ -> 
                            Box(Modifier.size(64.dp).background(Color(0xFFF0F2F5), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MediaUploadBox(
                title = "Video", 
                isSelected = state.video != null || !state.existingVideo.isNullOrBlank(), 
                icon = Icons.Default.PlayCircle, 
                modifier = Modifier.weight(1f), 
                onClick = onAddVideo
            )
            MediaUploadBox(
                title = "Contract", 
                isSelected = state.contract != null || !state.existingContract.isNullOrBlank(), 
                icon = Icons.Default.Description, 
                modifier = Modifier.weight(1f), 
                onClick = onAddContract
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfessionalTextField(value = state.contactPhone, onValueChange = onContactPhoneChange, label = "Phone", placeholder = "+255...")
            ProfessionalTextField(value = state.contactEmail, onValueChange = onContactEmailChange, label = "Email")
            ProfessionalTextField(value = state.rules, onValueChange = onRulesChange, label = "House Rules", singleLine = false, minHeight = 80.dp)
        }
    }
}

@Composable
private fun MediaUploadBox(title: String, isSelected: Boolean, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(64.dp).clickable { onClick() },
        color = if (isSelected) PrimaryColor.copy(alpha = 0.05f) else Color(0xFFF8F9FA),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryColor else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = if (isSelected) Icons.Default.CheckCircle else icon, null, tint = if (isSelected) Color(0xFF4CAF50) else Color.Gray, modifier = Modifier.size(18.dp))
            @Suppress("DEPRECATION")
            Spacer(Modifier.width(8.dp))
            Text(text = if (isSelected) "Done" else title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF4CAF50) else Color.Gray)
        }
    }
}

@Composable
private fun ProfessionalTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier, placeholder: String = "", singleLine: Boolean = true, minHeight: androidx.compose.ui.unit.Dp? = null) {
    Column(modifier) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().then(if (minHeight != null) Modifier.height(minHeight) else Modifier.height(64.dp)),
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyTypeDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            propertyTypes.forEach { type -> DropdownMenuItem(text = { Text(type, fontSize = 14.sp) }, onClick = { onSelected(type); expanded = false }) }
        }
    }
}

@Composable
private fun LocationModeTab(title: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.height(34.dp).clip(CircleShape).background(if (active) Color.White else Color.Transparent).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(title, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) PrimaryColor else Color.Gray)
    }
}

@Composable
private fun AmenityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label, fontSize = 11.sp) }, shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp),
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryColor.copy(alpha = 0.1f), selectedLabelColor = PrimaryColor)
    )
}

@Composable
private fun LoadingOverlay(msg: String) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryColor)
                Spacer(Modifier.height(12.dp))
                Text(msg, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatusOverlay(success: Boolean, msg: String, btnText: String, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { onDismiss() }, contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .widthIn(max = 350.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                        null,
                        tint = if (success) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(if (success) "Phhantastic!" else "Oops!", fontWeight = FontWeight.Black, fontSize = 22.sp, color = if (success) Color(0xFF2E7D32) else Color.Red)
                Spacer(Modifier.height(12.dp))
                Text(msg, textAlign = TextAlign.Center, color = Color.Gray, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (success) Color(0xFF4CAF50) else Color.Red)
                ) {
                    Text(btnText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun StepFloorsAndTemplates(
    state: PropertyFormState,
    onNumFloorsChange: (String) -> Unit,
    onUpdateFloorConfig: (Int, (FloorConfig) -> FloorConfig) -> Unit,
    onAddRoomTemplate: () -> Unit,
    onUpdateRoomTemplate: (String, (RoomTemplate) -> RoomTemplate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Building Structure")
        ProfessionalTextField(value = state.numFloors, onValueChange = onNumFloorsChange, label = "Number of Floors", placeholder = "e.g. 5")
        
        state.floorConfigs.forEach { config ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8F9FA),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Floor ${config.floorNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = config.numRooms.toString(),
                            onValueChange = { val n = it.toIntOrNull() ?: 0; onUpdateFloorConfig(config.floorNumber) { it.copy(numRooms = n) } },
                            label = { Text("Rooms", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = config.startRoomNumber.toString(),
                            onValueChange = { val n = it.toIntOrNull() ?: 0; onUpdateFloorConfig(config.floorNumber) { it.copy(startRoomNumber = n) } },
                            label = { Text("Start #", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle("Room Templates")
        state.roomTemplates.forEach { template ->
            TemplateEditCard(template) { updated -> onUpdateRoomTemplate(template.id, { updated }) }
        }
        
        TextButton(onClick = onAddRoomTemplate) {
            Icon(Icons.Default.Add, null)
            Text("ADD ANOTHER TEMPLATE")
        }
    }
}

@Composable
private fun TemplateEditCard(template: RoomTemplate, onUpdate: (RoomTemplate) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (expanded) PrimaryColor else Color(0xFFEEEEEE))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dashboard, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(template.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }
            
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                ProfessionalTextField(value = template.name, onValueChange = { onUpdate(template.copy(name = it)) }, label = "Template Name")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfessionalTextField(value = template.price, onValueChange = { onUpdate(template.copy(price = it)) }, label = "Price", modifier = Modifier.weight(1f))
                    ProfessionalTextField(value = template.area, onValueChange = { onUpdate(template.copy(area = it)) }, label = "Area", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StepBulkGeneration(
    state: PropertyFormState,
    onGenerate: (Int, Int, Int, String) -> Unit
) {
    var selectedFloor by remember { mutableStateOf(state.floorConfigs.firstOrNull()?.floorNumber ?: 1) }
    var selectedTemplate by remember { mutableStateOf(state.roomTemplates.firstOrNull()?.id ?: "") }
    var startNum by remember { mutableStateOf("101") }
    var count by remember { mutableStateOf("10") }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Bulk Room Generation")
        
        Surface(color = Color(0xFFF0F2F5), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Generator Tool", fontWeight = FontWeight.Bold, color = PrimaryColor)
                
                // Floor Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Target Floor", modifier = Modifier.width(100.dp), fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.floorConfigs) { config ->
                            FilterChip(
                                selected = selectedFloor == config.floorNumber,
                                onClick = { selectedFloor = config.floorNumber },
                                label = { Text("F${config.floorNumber}") }
                            )
                        }
                    }
                }

                // Template Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Template", modifier = Modifier.width(100.dp), fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.roomTemplates) { tmpl ->
                            FilterChip(
                                selected = selectedTemplate == tmpl.id,
                                onClick = { selectedTemplate = tmpl.id },
                                label = { Text(tmpl.name) }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfessionalTextField(value = startNum, onValueChange = { startNum = it }, label = "Start Room #", modifier = Modifier.weight(1f))
                    ProfessionalTextField(value = count, onValueChange = { count = it }, label = "How many rooms?", modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = { onGenerate(selectedFloor, startNum.toIntOrNull() ?: 1, count.toIntOrNull() ?: 0, selectedTemplate) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(Modifier.width(8.dp))
                    Text("GENERATE ROOMS")
                }
            }
        }

        if (state.generatedUnits.isNotEmpty()) {
            Text("${state.generatedUnits.size} rooms generated so far", fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
private fun StepReviewUnits(
    state: PropertyFormState,
    onToggleSelection: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onBulkUpdate: (String?, String?, String?) -> Unit,
    onDeleteSelected: () -> Unit,
    onUnitImagesSelected: (Int, List<KmpFile>) -> Unit
) {
    var showBulkActions by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Review & Edit Units")
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${state.selectedUnits.size} selected", fontSize = 12.sp, color = Color.Gray)
            Row {
                TextButton(onClick = onSelectAll) { Text("Select All") }
                TextButton(onClick = onClearSelection) { Text("Clear") }
            }
        }

        if (state.selectedUnits.isNotEmpty()) {
            Surface(
                color = PrimaryColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f))
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { showBulkActions = true }) { Icon(Icons.Default.Edit, null, tint = PrimaryColor) }
                    IconButton(onClick = onDeleteSelected) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(state.generatedUnits) { index, unit ->
                UnitReviewRow(
                    unit = unit,
                    isSelected = index in state.selectedUnits,
                    images = state.unitImages[index] ?: emptyList(),
                    onToggle = { onToggleSelection(index) },
                    onAddImages = { files -> onUnitImagesSelected(index, files) }
                )
            }
        }
    }

    if (showBulkActions) {
        BulkUpdateDialog(
            onDismiss = { showBulkActions = false },
            onApply = { price, status, type ->
                onBulkUpdate(price, status, type)
                showBulkActions = false
            }
        )
    }
}

@Composable
private fun UnitReviewRow(
    unit: org.com.model.Room, 
    isSelected: Boolean, 
    images: List<KmpFile>,
    onToggle: () -> Unit,
    onAddImages: (List<KmpFile>) -> Unit
) {
    val imagesPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { onAddImages(images + it) }
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryColor.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) PrimaryColor else Color(0xFFEEEEEE))
    ) {
        Column {
            Row(
                Modifier.padding(12.dp).clickable { onToggle() }, 
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(unit.unitNumber ?: "No #", fontWeight = FontWeight.Bold)
                    Text("Floor ${unit.floorNumber}", fontSize = 11.sp, color = Color.Gray)
                }
                
                Text("TZS ${unit.price.toInt()}", fontWeight = FontWeight.Black, color = PrimaryColor, fontSize = 13.sp)
                
                Spacer(Modifier.width(12.dp))
                
                IconButton(onClick = { imagesPicker.launch() }) {
                    BadgedBox(badge = { if (images.isNotEmpty()) Badge { Text("${images.size}") } }) {
                        Icon(Icons.Default.AddAPhoto, null, tint = if (images.isNotEmpty()) PrimaryColor else Color.Gray)
                    }
                }
            }
            
            if (images.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { image ->
                        Box(Modifier.size(40.dp)) {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(Color(0xFFEEEEEE)), 
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                            }
                            
                            // Simple remove button for unit images
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(14.dp)
                                    .background(Color.Red, CircleShape)
                                    .clickable { onAddImages(images - image) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkUpdateDialog(onDismiss: () -> Unit, onApply: (String?, String?, String?) -> Unit) {
    var price by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("AVAILABLE") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Update Selected") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("New Price") })
                // Add more fields if needed
            }
        },
        confirmButton = {
            Button(onClick = { onApply(price.ifBlank { null }, status, null) }) { Text("Apply Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(5.dp, 20.dp).background(PrimaryColor, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(mainAxisSpacing: androidx.compose.ui.unit.Dp, crossAxisSpacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing), verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)) { content() }
}
