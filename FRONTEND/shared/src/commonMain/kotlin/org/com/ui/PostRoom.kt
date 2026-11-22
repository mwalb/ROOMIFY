package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.*
import org.com.i18n.LocalRoomifyStrings
import org.com.network.ApiClient
import org.com.network.RoomifyApi
import org.com.ui.components.AutocompleteTextField
import kotlin.math.roundToInt

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
    val price: String = "", // Used for Single Room rent
    val defaultRent: String = "180000", // Used for Building mode default rent
    val propertyType: String = "Apartment",
    val rooms: String = "1",
    val bathrooms: String = "1",
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
    val floorConfigs: List<FloorConfig> = listOf(FloorConfig(floorNumber = 1, numRooms = 10, startRoomNumber = 101)),
    val roomTemplates: List<RoomTemplate> = listOf(
        RoomTemplate(id = "default", name = "Standard Room", price = "180000", type = "Hostel / Rooms"),
        RoomTemplate(id = "master", name = "Self-Contained / Master", price = "250000", type = "Apartment"),
        RoomTemplate(id = "studio", name = "Bedsitter / Studio", price = "200000", type = "Studio / Bedsitter")
    ),
    val generatedUnits: List<Room> = emptyList(),
    val unitImages: Map<Int, List<KmpFile>> = emptyMap(),
    val selectedUnits: Set<Int> = emptySet(),
    val unitSearchQuery: String = "",
    val unitFilterFloor: Int? = null,
    val unitFilterStatus: String? = null
)

private val propertyTypes = listOf("Apartment", "Hostel / Rooms", "Studio / Bedsitter", "Commercial / Mixed")
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
    onDefaultRentChange: (String) -> Unit = {},
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
    val totalSteps = 4 // Unified 4-step wizard for both Single and Building modes
    val scrollState = rememberScrollState()

    var areaSuggestions by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(Unit) {
        try { areaSuggestions = RoomifyApi.getAreaSuggestions() } catch (e: Exception) {}
    }

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val isExpanded = screenWidth >= 840.dp
        val isMedium = screenWidth in 600.dp..839.dp

        val containerModifier = when {
            isExpanded -> Modifier.widthIn(max = 1080.dp).fillMaxHeight(0.92f).padding(vertical = 24.dp)
            isMedium -> Modifier.widthIn(max = 720.dp).fillMaxHeight(0.94f).padding(vertical = 16.dp)
            else -> Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp)
        }

        val cardShape = when {
            isExpanded -> RoundedCornerShape(20.dp)
            isMedium -> RoundedCornerShape(16.dp)
            else -> RoundedCornerShape(12.dp)
        }

        // Decorative background elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.08f }
        ) {
            repeat(4) { i ->
                Box(
                    modifier = Modifier
                        .size((120 * (i + 1)).dp)
                        .align(if (i % 2 == 0) Alignment.TopStart else Alignment.BottomEnd)
                        .background(Color.White, CircleShape)
                )
            }
        }

        // Main Wizard Card
        Card(
            modifier = containerModifier
                .graphicsLayer {
                    alpha = cardAlpha.value
                    scaleX = cardScale.value
                    scaleY = cardScale.value
                },
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header inside Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryLight)))
                        .padding(vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.postARoom,
                                fontSize = if (isExpanded) 20.sp else 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = if (state.postMode == PostMode.SINGLE) "Single Room Mode" else "Building / Multiple Units Mode",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$currentStep/$totalSteps",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Step Progress Indicator
                StepProgressBar(currentStep = currentStep, totalSteps = totalSteps)

                // Main Step Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(
                            horizontal = if (isExpanded) 32.dp else if (isMedium) 24.dp else 16.dp,
                            vertical = 16.dp
                        )
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { it } + fadeIn(tween(350))).togetherWith(slideOutHorizontally { -it } + fadeOut(tween(350)))
                            } else {
                                (slideInHorizontally { -it } + fadeIn(tween(350))).togetherWith(slideOutHorizontally { it } + fadeOut(tween(350)))
                            }
                        },
                        label = "stepTransition"
                    ) { step ->
                        when (step) {
                            1 -> StepBasicInfo(
                                state = state,
                                isExpanded = isExpanded,
                                onPostModeChange = onPostModeChange,
                                onTitleChange = onTitleChange,
                                onDescriptionChange = onDescriptionChange,
                                onPriceChange = onPriceChange,
                                onDefaultRentChange = onDefaultRentChange,
                                onNumFloorsChange = onNumFloorsChange,
                                onPropertyTypeChange = onPropertyTypeChange
                            )

                            2 -> StepLocation(
                                state = state,
                                isExpanded = isExpanded,
                                onLocationModeChange = onLocationModeChange,
                                onManualAddressChange = onManualAddressChange,
                                onLatitudeChange = onLatitudeChange,
                                onLongitudeChange = onLongitudeChange,
                                onLocationSelected = onLocationSelected,
                                areaSuggestions = areaSuggestions
                            )

                            3 -> if (state.postMode == PostMode.SINGLE) {
                                StepDetails(
                                    state = state,
                                    isExpanded = isExpanded,
                                    onRoomsChange = onRoomsChange,
                                    onBathroomsChange = onBathroomsChange,
                                    onAreaChange = onAreaChange,
                                    onMaxGuestsChange = onMaxGuestsChange,
                                    onToggleAmenity = onToggleAmenity
                                )
                            } else {
                                StepBuildingRoomsAndUnits(
                                    state = state,
                                    isExpanded = isExpanded,
                                    onNumFloorsChange = onNumFloorsChange,
                                    onDefaultRentChange = onDefaultRentChange,
                                    onGenerateUnits = onGenerateUnits,
                                    onToggleUnitSelection = onToggleUnitSelection,
                                    onSelectAllUnits = onSelectAllUnits,
                                    onClearUnitSelection = onClearUnitSelection,
                                    onBulkUpdateUnits = onBulkUpdateUnits,
                                    onDeleteSelectedUnits = onDeleteSelectedUnits,
                                    onUnitImagesSelected = onUnitImagesSelected
                                )
                            }

                            4 -> StepMediaAndContact(
                                state = state,
                                isExpanded = isExpanded,
                                onAddImages = { imagesPicker.launch() },
                                onAddVideo = { videoPicker.launch() },
                                onAddContract = { contractPicker.launch() },
                                onContactPhoneChange = onContactPhoneChange,
                                onContactEmailChange = onContactEmailChange,
                                onRulesChange = onRulesChange
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Action Footer Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp).weight(1f),
                            border = BorderStroke(1.5.dp, PrimaryColor)
                        ) {
                            Text("PREVIOUS", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                    }

                    val hasPhotos = state.images.isNotEmpty() || state.existingImages.isNotEmpty()
                    val hasVideo = state.video != null || !state.existingVideo.isNullOrBlank()

                    val canGoNext = when (state.postMode) {
                        PostMode.SINGLE -> when (currentStep) {
                            1 -> state.title.isNotBlank() && state.price.isNotBlank()
                            2 -> state.latitude.isNotBlank() && state.longitude.isNotBlank() && state.selectedAddress != "No location selected"
                            3 -> state.rooms.isNotBlank()
                            else -> hasPhotos && hasVideo
                        }
                        PostMode.BUILDING -> when (currentStep) {
                            1 -> state.title.isNotBlank()
                            2 -> state.latitude.isNotBlank() && state.longitude.isNotBlank() && state.selectedAddress != "No location selected"
                            3 -> true
                            else -> hasPhotos && hasVideo
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < totalSteps) currentStep++
                            else onSubmit()
                        },
                        enabled = canGoNext,
                        shape = RoundedCornerShape(10.dp),
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

    // Status Overlays
    if (state.isSubmitting) LoadingOverlay(strings.postingYourProperty)
    if (state.errorMessage != null) StatusOverlay(false, state.errorMessage, "Try Again", onErrorDismissed)
    if (state.successMessage != null) StatusOverlay(true, state.successMessage, "Go to Dashboard", onSuccessDismissed)
}

@Composable
private fun StepProgressBar(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 24.dp),
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
                        .size(28.dp)
                        .background(if (isActive) PrimaryColor else Color(0xFFEEEEEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive && !isCurrent && step < currentStep) {
                        Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("$step", color = if (isActive) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (step < currentStep) PrimaryColor else Color(0xFFEEEEEE))
                        .padding(horizontal = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun StepBasicInfo(
    state: PropertyFormState,
    isExpanded: Boolean,
    onPostModeChange: (PostMode) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDefaultRentChange: (String) -> Unit,
    onNumFloorsChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Step 1/4: Basic Information")

        Text("Select Listing Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F2F5), RoundedCornerShape(12.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PostModeOption(
                title = "Single Room",
                subtitle = "Posting one standalone room, studio, or unit.",
                selected = state.postMode == PostMode.SINGLE,
                modifier = Modifier.weight(1f),
                onClick = { onPostModeChange(PostMode.SINGLE) }
            )
            PostModeOption(
                title = "Building / Multiple",
                subtitle = "Posting a building, hostel, or compound with multiple rooms.",
                selected = state.postMode == PostMode.BUILDING,
                modifier = Modifier.weight(1f),
                onClick = { onPostModeChange(PostMode.BUILDING) }
            )
        }

        Spacer(Modifier.height(4.dp))

        ProfessionalTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = if (state.postMode == PostMode.SINGLE) "Property / Room Title *" else "Property / Building Name *",
            placeholder = if (state.postMode == PostMode.SINGLE) "e.g. Modern Studio Apartment" else "e.g. Makumbusho Plaza Complex"
        )

        ProfessionalTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = "Description",
            placeholder = "Describe key features, location highlights, or rules...",
            singleLine = false,
            minHeight = 90.dp
        )

        if (state.postMode == PostMode.SINGLE) {
            if (isExpanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfessionalTextField(
                        value = state.price,
                        onValueChange = onPriceChange,
                        label = "Monthly Rent (TZS) *",
                        modifier = Modifier.weight(1f),
                        placeholder = "e.g. 250000"
                    )
                    Column(Modifier.weight(1f)) {
                        Text("Property Type", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 6.dp))
                        PropertyTypeDropdown(state.propertyType, onPropertyTypeChange)
                    }
                }
            } else {
                ProfessionalTextField(
                    value = state.price,
                    onValueChange = onPriceChange,
                    label = "Monthly Rent (TZS) *",
                    placeholder = "e.g. 250000"
                )
                Column {
                    Text("Property Type", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 6.dp))
                    PropertyTypeDropdown(state.propertyType, onPropertyTypeChange)
                }
            }
        } else {
            // BUILDING MODE
            if (isExpanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfessionalTextField(
                        value = state.numFloors,
                        onValueChange = onNumFloorsChange,
                        label = "Number of Floors (e.g. 1 - 10+)",
                        modifier = Modifier.weight(1f),
                        placeholder = "1"
                    )
                    ProfessionalTextField(
                        value = state.defaultRent,
                        onValueChange = onDefaultRentChange,
                        label = "Default Rent (TZS)",
                        modifier = Modifier.weight(1f),
                        placeholder = "180000"
                    )
                }
            } else {
                ProfessionalTextField(
                    value = state.numFloors,
                    onValueChange = onNumFloorsChange,
                    label = "Number of Floors (e.g. 1 - 10+)",
                    placeholder = "1"
                )
                ProfessionalTextField(
                    value = state.defaultRent,
                    onValueChange = onDefaultRentChange,
                    label = "Default Rent (TZS)",
                    placeholder = "180000"
                )
            }

            Column {
                Text("Building Type", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 6.dp))
                PropertyTypeDropdown(state.propertyType, onPropertyTypeChange)
            }
        }
    }
}

@Composable
private fun PostModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) PrimaryColor else Color.White,
        border = BorderStroke(1.dp, if (selected) PrimaryColor else Color(0xFFDDDDDD))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = Color.White, unselectedColor = Color.Gray)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (selected) Color.White else Color.DarkGray
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 11.sp,
                color = if (selected) Color.White.copy(alpha = 0.85f) else Color.Gray,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun StepLocation(
    state: PropertyFormState,
    isExpanded: Boolean,
    onLocationModeChange: (LocationInputMode) -> Unit,
    onManualAddressChange: (String) -> Unit,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onLocationSelected: (AddressResult) -> Unit,
    areaSuggestions: List<String> = emptyList()
) {
    val showTabs = isMapTabSupported()
    var showMapModal by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Step 2/4: Property Location")

        if (showTabs) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFE9EEF1), CircleShape).padding(4.dp)) {
                LocationModeTab("Map View", state.locationMode == LocationInputMode.MAP, Modifier.weight(1f)) { onLocationModeChange(LocationInputMode.MAP) }
                LocationModeTab("Manual Address", state.locationMode == LocationInputMode.MANUAL, Modifier.weight(1f)) { onLocationModeChange(LocationInputMode.MANUAL) }
            }
        }

        Surface(
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Address & Coordinates", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AutocompleteTextField(
                            value = state.manualAddress,
                            onValueChange = { onManualAddressChange(it) },
                            suggestions = areaSuggestions,
                            label = "Property Address",
                            placeholder = "e.g. Kinondoni, Mikocheni, Sinza...",
                            onSuggestionSelected = { onManualAddressChange(it) },
                            onPlaceSelected = { place ->
                                val fullAddr = if (place.name.contains(",")) place.name
                                else "${place.name}, ${place.subtitle.substringAfterLast(",").trim().ifBlank { "Dar es Salaam" }}"
                                onManualAddressChange(fullAddr)
                                if (place.hasCoordinates && place.lat != 0.0 && place.lng != 0.0) {
                                    onLatitudeChange(place.lat.toString())
                                    onLongitudeChange(place.lng.toString())
                                    onLocationSelected(
                                        AddressResult(
                                            address = fullAddr,
                                            latitude = place.lat,
                                            longitude = place.lng,
                                            formattedAddress = fullAddr
                                        )
                                    )
                                }
                            }
                        )
                    }

                    Button(
                        onClick = { showMapModal = true },
                        modifier = Modifier.padding(top = 6.dp).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Icon(Icons.Default.Place, "Location", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Select Map Pin", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                OutlinedButton(
                    onClick = { showMapModal = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryColor)
                ) {
                    Icon(Icons.Default.MyLocation, "My Location", modifier = Modifier.size(18.dp), tint = PrimaryColor)
                    Spacer(Modifier.width(8.dp))
                    Text("Use My Current Location", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                }

                // Crisp Vector Confirmation Banner
                if (state.latitude.isNotBlank() && state.longitude.isNotBlank()) {
                    val latNum = state.latitude.toDoubleOrNull()
                    val lngNum = state.longitude.toDoubleOrNull()

                    val shortLat = if (latNum != null) ((latNum * 10000.0).roundToInt() / 10000.0).toString() else state.latitude
                    val shortLng = if (lngNum != null) ((lngNum * 10000.0).roundToInt() / 10000.0).toString() else state.longitude

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Confirmed",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Location Confirmed", color = Color(0xFF1B5E20), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = state.manualAddress.ifBlank { state.selectedAddress },
                                color = Color(0xFF2E7D32),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Text(
                                text = "Coordinates: $shortLat, $shortLng",
                                color = Color(0xFF388E3C),
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )

                            Spacer(Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { showMapModal = true },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E7D32))
                            ) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Change Location", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
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
                onManualAddressChange(result.address)
                showMapModal = false
            },
            onDismiss = { showMapModal = false }
        )
    }
}

@Composable
private fun StepDetails(
    state: PropertyFormState,
    isExpanded: Boolean,
    onRoomsChange: (String) -> Unit,
    onBathroomsChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onMaxGuestsChange: (String) -> Unit,
    onToggleAmenity: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Step 3/4: Property Features & Amenities")

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfessionalTextField(value = state.rooms, onValueChange = onRoomsChange, label = "Beds / Rooms", modifier = Modifier.weight(1f), placeholder = "1")
            ProfessionalTextField(value = state.bathrooms, onValueChange = onBathroomsChange, label = "Bathrooms", modifier = Modifier.weight(1f), placeholder = "1")
            ProfessionalTextField(value = state.area, onValueChange = onAreaChange, label = "Area (m²)", modifier = Modifier.weight(1f), placeholder = "35")
        }

        ProfessionalTextField(value = state.maxGuests, onValueChange = onMaxGuestsChange, label = "Max Guests Allowed", placeholder = "2")

        Column {
            Text("Amenities Included", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 10.dp))
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                amenitiesList.forEach { amenity ->
                    AmenityChip(amenity, amenity in state.selectedAmenities) { onToggleAmenity(amenity) }
                }
            }
        }
    }
}

@Composable
private fun StepBuildingRoomsAndUnits(
    state: PropertyFormState,
    isExpanded: Boolean,
    onNumFloorsChange: (String) -> Unit,
    onDefaultRentChange: (String) -> Unit,
    onGenerateUnits: (Int, Int, Int, String) -> Unit,
    onToggleUnitSelection: (Int) -> Unit,
    onSelectAllUnits: () -> Unit,
    onClearUnitSelection: () -> Unit,
    onBulkUpdateUnits: (String?, String?, String?) -> Unit,
    onDeleteSelectedUnits: () -> Unit,
    onUnitImagesSelected: (Int, List<KmpFile>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SectionTitle("Step 3/4: Building Rooms & Unit Generator")

        if (isExpanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Box(modifier = Modifier.weight(0.42f)) {
                    BuildingGeneratorCard(
                        state = state,
                        onGenerate = onGenerateUnits
                    )
                }
                Box(modifier = Modifier.weight(0.58f)) {
                    BuildingReviewUnitsCard(
                        state = state,
                        onToggleUnitSelection = onToggleUnitSelection,
                        onSelectAllUnits = onSelectAllUnits,
                        onClearUnitSelection = onClearUnitSelection,
                        onBulkUpdateUnits = onBulkUpdateUnits,
                        onDeleteSelectedUnits = onDeleteSelectedUnits,
                        onUnitImagesSelected = onUnitImagesSelected
                    )
                }
            }
        } else {
            BuildingGeneratorCard(
                state = state,
                onGenerate = onGenerateUnits
            )
            BuildingReviewUnitsCard(
                state = state,
                onToggleUnitSelection = onToggleUnitSelection,
                onSelectAllUnits = onSelectAllUnits,
                onClearUnitSelection = onClearUnitSelection,
                onBulkUpdateUnits = onBulkUpdateUnits,
                onDeleteSelectedUnits = onDeleteSelectedUnits,
                onUnitImagesSelected = onUnitImagesSelected
            )
        }
    }
}

@Composable
private fun BuildingGeneratorCard(
    state: PropertyFormState,
    onGenerate: (Int, Int, Int, String) -> Unit
) {
    var selectedFloor by remember { mutableStateOf(1) }
    var selectedTemplateId by remember { mutableStateOf(state.roomTemplates.firstOrNull()?.id ?: "default") }
    var startNum by remember { mutableStateOf("101") }
    var roomCount by remember { mutableStateOf("10") }
    var customRent by remember { mutableStateOf(state.defaultRent) }

    Surface(
        color = Color(0xFFF0F4F8),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFD0D9E0))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Room Generator Tool", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryColor)
            }

            // Floor Selection
            Column {
                Text("Target Floor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                val totalFloors = state.numFloors.toIntOrNull() ?: 1
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    items((1..maxOf(1, totalFloors)).toList()) { floor ->
                        FilterChip(
                            selected = selectedFloor == floor,
                            onClick = { selectedFloor = floor },
                            label = { Text("Floor $floor", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Template Selection
            Column {
                Text("Room Template", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                    items(state.roomTemplates) { tmpl ->
                        FilterChip(
                            selected = selectedTemplateId == tmpl.id,
                            onClick = {
                                selectedTemplateId = tmpl.id
                                if (tmpl.price.isNotBlank()) customRent = tmpl.price
                            },
                            label = { Text(tmpl.name, fontSize = 11.sp) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfessionalTextField(
                    value = startNum,
                    onValueChange = { startNum = it },
                    label = "Start Room #",
                    modifier = Modifier.weight(1f),
                    placeholder = "101"
                )
                ProfessionalTextField(
                    value = roomCount,
                    onValueChange = { roomCount = it },
                    label = "How many rooms?",
                    modifier = Modifier.weight(1f),
                    placeholder = "10"
                )
            }

            Button(
                onClick = {
                    val count = roomCount.toIntOrNull() ?: 10
                    val start = startNum.toIntOrNull() ?: 101
                    onGenerate(selectedFloor, start, count, selectedTemplateId)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("GENERATE ROOMS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BuildingReviewUnitsCard(
    state: PropertyFormState,
    onToggleUnitSelection: (Int) -> Unit,
    onSelectAllUnits: () -> Unit,
    onClearUnitSelection: () -> Unit,
    onBulkUpdateUnits: (String?, String?, String?) -> Unit,
    onDeleteSelectedUnits: () -> Unit,
    onUnitImagesSelected: (Int, List<KmpFile>) -> Unit
) {
    var showBulkDialog by remember { mutableStateOf(false) }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Generated Units", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryColor)
                    Text("${state.generatedUnits.size} rooms total • ${state.selectedUnits.size} selected", fontSize = 11.sp, color = Color.Gray)
                }

                Row {
                    TextButton(onClick = onSelectAllUnits) { Text("Select All", fontSize = 11.sp) }
                    TextButton(onClick = onClearUnitSelection) { Text("Clear", fontSize = 11.sp) }
                }
            }

            // Compact Bulk Action Bar
            if (state.selectedUnits.isNotEmpty()) {
                Surface(
                    color = PrimaryColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.selectedUnits.size} selected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showBulkDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Bulk Edit", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = onDeleteSelectedUnits,
                                modifier = Modifier.size(34.dp).background(Color(0xFFFFEBEE), CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (state.generatedUnits.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Apartment, null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No units generated yet", color = Color.Gray, fontSize = 13.sp)
                        Text("Use the generator tool to create rooms", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.generatedUnits) { index, unit ->
                        UnitRow(
                            unit = unit,
                            isSelected = index in state.selectedUnits,
                            images = state.unitImages[index] ?: emptyList(),
                            onToggle = { onToggleUnitSelection(index) },
                            onImagesSelected = { files -> onUnitImagesSelected(index, files) }
                        )
                    }
                }
            }
        }
    }

    if (showBulkDialog) {
        BulkUpdateDialog(
            onDismiss = { showBulkDialog = false },
            onApply = { price, status, type ->
                onBulkUpdateUnits(price, status, type)
                showBulkDialog = false
            }
        )
    }
}

@Composable
private fun UnitRow(
    unit: Room,
    isSelected: Boolean,
    images: List<KmpFile>,
    onToggle: () -> Unit,
    onImagesSelected: (List<KmpFile>) -> Unit
) {
    val imagesPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { onImagesSelected(images + it) }
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryColor.copy(alpha = 0.06f) else Color(0xFFF9FBFD),
        border = BorderStroke(1.dp, if (isSelected) PrimaryColor else Color(0xFFE5E9F0))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp).clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Room ${unit.unitNumber ?: "#"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Floor ${unit.floorNumber ?: 1} • ${unit.propertyType ?: "Room"}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Surface(
                color = if (unit.status.equals("AVAILABLE", ignoreCase = true)) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (unit.status.equals("AVAILABLE", ignoreCase = true)) "Available" else "Occupied",
                    color = if (unit.status.equals("AVAILABLE", ignoreCase = true)) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = "${unit.price.toInt()} TZS",
                fontWeight = FontWeight.Black,
                color = PrimaryColor,
                fontSize = 13.sp
            )

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = { imagesPicker.launch() }, modifier = Modifier.size(32.dp)) {
                BadgedBox(badge = { if (images.isNotEmpty()) Badge { Text("${images.size}") } }) {
                    Icon(Icons.Default.AddAPhoto, "Photos", tint = if (images.isNotEmpty()) PrimaryColor else Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun BulkUpdateDialog(
    onDismiss: () -> Unit,
    onApply: (String?, String?, String?) -> Unit
) {
    var price by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("AVAILABLE") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Edit Selected Units", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("New Rent (TZS) (Optional)") },
                    placeholder = { Text("e.g. 200000") },
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == "AVAILABLE",
                        onClick = { status = "AVAILABLE" },
                        label = { Text("Available") }
                    )
                    FilterChip(
                        selected = status == "OCCUPIED",
                        onClick = { status = "OCCUPIED" },
                        label = { Text("Occupied") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(price.ifBlank { null }, status, null) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Apply to Selected")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StepMediaAndContact(
    state: PropertyFormState,
    isExpanded: Boolean,
    onAddImages: () -> Unit,
    onAddVideo: () -> Unit,
    onAddContract: () -> Unit,
    onContactPhoneChange: (String) -> Unit,
    onContactEmailChange: (String) -> Unit,
    onRulesChange: (String) -> Unit
) {
    val photoCount = state.images.size + state.existingImages.size
    val hasVideo = state.video != null || !state.existingVideo.isNullOrBlank()
    val hasContract = state.contract != null || !state.existingContract.isNullOrBlank()

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        SectionTitle("Step 4/4: Media, Rules & Contact")

        // Photos Slot Card (Min 1)
        MediaUploadSlotCard(
            title = "Property Photos * (Min 1)",
            subtitle = "Upload clear photos of room, bathroom & entrance",
            itemsCount = photoCount,
            isUploading = false,
            icon = Icons.Default.AddPhotoAlternate,
            thumbnailUrls = state.existingImages,
            onPickMedia = onAddImages
        )

        // Video Walkthrough Slot Card (Mandatory)
        Column {
            MediaUploadSlotCard(
                title = "Video Walkthrough * (Required)",
                subtitle = "Short walkthrough of the room, bathroom & entrance",
                itemsCount = if (hasVideo) 1 else 0,
                isUploading = false,
                icon = Icons.Rounded.Videocam,
                onPickMedia = onAddVideo,
                onReplaceMedia = onAddVideo
            )

            if (!hasVideo) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "A property video walkthrough is required before publishing",
                    color = Color(0xFFDC2626),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Contract Slot Card (Optional)
        MediaUploadSlotCard(
            title = "Contract / Rules Doc (Optional)",
            subtitle = "Lease agreement or building rules document",
            itemsCount = if (hasContract) 1 else 0,
            isUploading = false,
            icon = Icons.Default.Description,
            onPickMedia = onAddContract,
            onReplaceMedia = onAddContract
        )

        if (isExpanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ProfessionalTextField(value = state.contactPhone, onValueChange = onContactPhoneChange, label = "Phone Number (+255...)", modifier = Modifier.weight(1f), placeholder = "+255 700 000 000")
                ProfessionalTextField(value = state.contactEmail, onValueChange = onContactEmailChange, label = "Contact Email", modifier = Modifier.weight(1f), placeholder = "owner@example.com")
            }
        } else {
            ProfessionalTextField(value = state.contactPhone, onValueChange = onContactPhoneChange, label = "Phone Number (+255...)", placeholder = "+255 700 000 000")
            ProfessionalTextField(value = state.contactEmail, onValueChange = onContactEmailChange, label = "Contact Email", placeholder = "owner@example.com")
        }

        ProfessionalTextField(value = state.rules, onValueChange = onRulesChange, label = "House Rules & Guidelines", singleLine = false, minHeight = 85.dp)
    }
}

@Composable
private fun MediaUploadSlotCard(
    title: String,
    subtitle: String,
    itemsCount: Int,
    isUploading: Boolean = false,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    thumbnailUrls: List<String> = emptyList(),
    onPickMedia: () -> Unit,
    onReplaceMedia: (() -> Unit)? = null,
    onRemoveMedia: (() -> Unit)? = null
) {
    val isAttached = itemsCount > 0 && !isUploading
    val cardBg = when {
        isUploading -> Color(0xFFEFF6FF)
        isAttached -> Color(0xFFF0FDF4)
        else -> Color(0xFFF8FAFC)
    }
    val cardBorder = when {
        isUploading -> Color(0xFF2563EB)
        isAttached -> Color(0xFF16A34A)
        else -> Color(0xFFCBD5E1)
    }
    val borderWidth = if (isAttached) 1.5.dp else 1.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable {
                if (isAttached && onReplaceMedia != null) onReplaceMedia()
                else onPickMedia()
            },
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(borderWidth, cardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    if (isAttached) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Uploaded",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                if (isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF2563EB)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Uploading...", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                    }
                } else if (isAttached) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Uploaded",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D),
                            softWrap = false
                        )
                    }
                }
            }

            if (isAttached && thumbnailUrls.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$itemsCount photo(s) attached",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
                    )
                    TextButton(onClick = onPickMedia) {
                        Text("+ Add more", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(thumbnailUrls) { url ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            val resolvedUrl = ApiClient.resolveUrl(url)
                            KamelImage(
                                resource = { asyncPainterResource(resolvedUrl) },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else if (isAttached && thumbnailUrls.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Video attached",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16A34A)
                    )
                    Row {
                        if (onReplaceMedia != null) {
                            TextButton(onClick = onReplaceMedia, modifier = Modifier.heightIn(min = 40.dp)) {
                                Text("Replace", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                            }
                        }
                        if (onRemoveMedia != null) {
                            TextButton(onClick = onRemoveMedia, modifier = Modifier.heightIn(min = 40.dp)) {
                                Text("Remove", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfessionalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp? = null
) {
    Column(modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().then(if (minHeight != null) Modifier.height(minHeight) else Modifier.height(56.dp)),
            placeholder = { Text(placeholder, color = Color.Gray, fontSize = 13.sp) },
            shape = RoundedCornerShape(10.dp),
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
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
            modifier = Modifier.menuAnchor().fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(10.dp),
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
    Box(
        modifier = modifier.height(34.dp).clip(CircleShape).background(if (active) Color.White else Color.Transparent).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(title, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) PrimaryColor else Color.Gray)
    }
}

@Composable
private fun AmenityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label, fontSize = 11.sp) }, shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(32.dp),
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryColor.copy(alpha = 0.12f), selectedLabelColor = PrimaryColor)
    )
}

@Composable
private fun LoadingOverlay(msg: String) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryColor)
                Spacer(Modifier.height(12.dp))
                Text(msg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                .fillMaxWidth(0.88f)
                .widthIn(max = 380.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(if (success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                        null,
                        tint = if (success) Color(0xFF4CAF50) else Color.Red,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(if (success) "Awesome!" else "Notice", fontWeight = FontWeight.Black, fontSize = 20.sp, color = if (success) Color(0xFF2E7D32) else Color.Red)
                Spacer(Modifier.height(8.dp))
                Text(msg, textAlign = TextAlign.Center, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (success) Color(0xFF4CAF50) else Color.Red)
                ) {
                    Text(btnText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(mainAxisSpacing: androidx.compose.ui.unit.Dp, crossAxisSpacing: androidx.compose.ui.unit.Dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing), verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)) { content() }
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(5.dp, 20.dp).background(PrimaryColor, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
    }
}
