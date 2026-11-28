package org.com.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.com.i18n.LocalRoomifyStrings
import org.com.model.User
import org.com.model.initials
import org.com.network.ApiClient
import org.com.network.RoomifyApi

private val PrimaryColor = Color(0xFF1A237E)
private val BackgroundGray = Color(0xFFF1F5F9)
private val TextDarkGray = Color(0xFF374151)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomifySidebar(
    user: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Boolean,
    onClose: () -> Unit,
    onMyLocationClick: (() -> Unit)? = null
) {
    var locationInput by remember { mutableStateOf("") }
    var locationSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalRoomifyStrings.current

    LaunchedEffect(locationInput) {
        if (locationInput.isNotBlank()) {
            try {
                locationSuggestions = RoomifyApi.getLocationSuggestions(locationInput)
                showSuggestions = locationSuggestions.isNotEmpty()
            } catch (e: Exception) {
                locationSuggestions = emptyList()
                showSuggestions = false
            }
        } else {
            locationSuggestions = emptyList()
            showSuggestions = false
        }
    }
    var selectedType by remember { mutableStateOf("ALL") }
    var budgetInput by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var showNoResults by remember { mutableStateOf(false) }

    var isLocationOpen by remember { mutableStateOf(true) }
    var isTypeOpen by remember { mutableStateOf(true) }
    var isBudgetOpen by remember { mutableStateOf(true) }
    var isAmenitiesOpen by remember { mutableStateOf(true) }
    var isStatusOpen by remember { mutableStateOf(true) }
    var selectedAmenities by remember { mutableStateOf<Set<String>>(emptySet()) }

    val applyFilter = {
        val area = locationInput.trim().ifBlank { null }
        val type = if (selectedType.uppercase() == "ALL") null else selectedType

        val cleanBudget = budgetInput.replace("TZS", "", ignoreCase = true)
            .replace(",", "")
            .trim()
        val parsedBudget = when {
            cleanBudget.lowercase().endsWith("k") -> {
                cleanBudget.dropLast(1).toDoubleOrNull()?.let { it * 1_000.0 }
            }
            cleanBudget.lowercase().endsWith("m") -> {
                cleanBudget.dropLast(1).toDoubleOrNull()?.let { it * 1_000_000.0 }
            }
            else -> cleanBudget.toDoubleOrNull()
        }

        val status = if (selectedStatus.uppercase() == "ALL") null else selectedStatus

        val hasMatches = onSearch(type, area, parsedBudget, status)
        if (hasMatches) {
            showNoResults = false
            onClose()
        } else {
            showNoResults = true
        }
    }

    val clearFilter = {
        locationInput = ""
        selectedType = "ALL"
        budgetInput = ""
        selectedStatus = "ALL"
        selectedAmenities = emptySet()
        showNoResults = false
        onSearch(null, null, null, null)
    }

    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        color = org.com.ui.theme.RoomifyColors.Background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().roomifyAmbientBackground(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header Section with Language Switcher
            SidebarHeader(onClose = onClose)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Role-Specific Navigation Section matching Web
                val upperRole = (user?.role ?: "").uppercase()
                when (upperRole) {
                    "TENANT" -> {
                        Text(
                            strings.myDashboard,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 12.dp).align(Alignment.Start)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("tenant"); onClose() },
                                label = { Text(strings.overview, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("saved"); onClose() },
                                label = { Text(strings.favourites, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("bookings"); onClose() },
                                label = { Text(strings.myBookings, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("messages"); onClose() },
                                label = { Text(strings.messages, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    "OWNER" -> {
                        Text(
                            strings.management,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 12.dp).align(Alignment.Start)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("ownerdashboard"); onClose() },
                                label = { Text(strings.dashboard, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("postroom"); onClose() },
                                label = { Text(strings.postARoom, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    "DALALI" -> {
                        Text(
                            strings.dalaliTools,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 12.dp).align(Alignment.Start)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("dalalidashboard"); onClose() },
                                label = { Text(strings.dashboard, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("postroom"); onClose() },
                                label = { Text(strings.postARoom, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                    "ADMIN", "SUPER_ADMIN" -> {
                        Text(
                            strings.administration,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 12.dp).align(Alignment.Start)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("admindashboard"); onClose() },
                                label = { Text(strings.adminPanel, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // 3. Nearby Me Button
                OutlinedButton(
                    onClick = {
                        onMyLocationClick?.invoke()
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, PrimaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                ) {
                    Text(
                        text = strings.nearbyMe,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryColor
                    )
                }

                Spacer(Modifier.height(20.dp))

                // 4. Dropdown Cards (Drop Format & Attractive Badge UI)
                // Card 1: Location & Popular Areas
                SidebarDropCard(
                    title = strings.location,
                    icon = "📍",
                    badgeText = locationInput.ifBlank { strings.all },
                    isOpen = isLocationOpen,
                    onToggle = { isLocationOpen = !isLocationOpen }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                BasicTextField(
                                    value = locationInput,
                                    onValueChange = { locationInput = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        if (locationInput.isEmpty()) {
                                            Text("${strings.location} (e.g. Mikocheni, Sinza, UDSM...)", color = Color(0xFF9CA3AF), fontSize = 13.sp)
                                        }
                                        inner()
                                    }
                                )
                                if (locationInput.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { locationInput = "" }
                                    )
                                }
                            }
                        }

                        if (showSuggestions && locationSuggestions.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 4.dp,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                    items(locationSuggestions) { suggestion ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    locationInput = suggestion
                                                    showSuggestions = false
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocationOn, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(suggestion, fontSize = 13.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Popular Areas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        val popularAreas = listOf("Mikocheni", "Sinza", "Kinondoni", "Mwenge", "Masaki", "UDSM")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            popularAreas.forEach { area ->
                                val isSelected = locationInput.equals(area, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { locationInput = if (isSelected) "" else area },
                                    label = { Text(area, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = TextDarkGray
                                    )
                                )
                            }
                        }
                    }
                }

                // Card 2: Property Type
                SidebarDropCard(
                    title = strings.whatAreYouLookingFor,
                    icon = "🏠",
                    badgeText = selectedType,
                    isOpen = isTypeOpen,
                    onToggle = { isTypeOpen = !isTypeOpen }
                ) {
                    val propertyTypes = listOf(strings.all, "Room", "Apartment", "Studio", "House", "Office")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        propertyTypes.forEach { type ->
                            val isSelected = selectedType.equals(type, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                label = { Text(type, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                        }
                    }
                }

                // Card 3: Budget Range
                SidebarDropCard(
                    title = strings.maxBudget,
                    icon = "💰",
                    badgeText = budgetInput.ifBlank { strings.all },
                    isOpen = isBudgetOpen,
                    onToggle = { isBudgetOpen = !isBudgetOpen }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val quickBudgets = listOf(
                            strings.all to "",
                            "< 100k" to "100000",
                            "100k - 250k" to "250000",
                            "300k - 600k" to "600000",
                            "700k+" to "1000000"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickBudgets.forEach { (label, value) ->
                                val isSelected = if (value.isEmpty()) budgetInput.isEmpty() else budgetInput.replace(",", "") == value
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { budgetInput = value },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = TextDarkGray
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEEF2FF),
                                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                                ) {
                                    Text("TZS", color = PrimaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                BasicTextField(
                                    value = budgetInput,
                                    onValueChange = { budgetInput = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        if (budgetInput.isEmpty()) {
                                            Text(strings.maxBudget, color = Color(0xFF9CA3AF), fontSize = 13.sp)
                                        }
                                        inner()
                                    }
                                )
                                if (budgetInput.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { budgetInput = "" }
                                    )
                                }
                            }
                        }
                    }
                }

                // Card 4: Amenities & Features (Matching Image 2 Badge UI)
                SidebarDropCard(
                    title = strings.amenitiesAndFeatures,
                    icon = "✨",
                    badgeText = if (selectedAmenities.isEmpty()) strings.all else "${selectedAmenities.size} selected",
                    isOpen = isAmenitiesOpen,
                    onToggle = { isAmenitiesOpen = !isAmenitiesOpen }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val amenitiesList = listOf(
                            "Maji ya DAWASA (24/7 Flow)",
                            "LUKU ya Kujitegemea",
                            "Simtank",
                            "WiFi",
                            "Parking ya Gari",
                            "Air Conditioning (AC)",
                            "Furnished",
                            "Security",
                            "Balcony"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            amenitiesList.forEach { amenity ->
                                val isSelected = selectedAmenities.contains(amenity)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedAmenities = if (isSelected) selectedAmenities - amenity else selectedAmenities + amenity
                                    },
                                    leadingIcon = {
                                        Text(if (isSelected) "✔" else "○", fontSize = 11.sp, color = if (isSelected) Color.White else Color(0xFF10B981))
                                    },
                                    label = { Text(amenity, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryColor,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = TextDarkGray
                                    )
                                )
                            }
                        }
                    }
                }

                // Card 5: Status Section
                SidebarDropCard(
                    title = strings.status,
                    icon = "📊",
                    badgeText = selectedStatus,
                    isOpen = isStatusOpen,
                    onToggle = { isStatusOpen = !isStatusOpen }
                ) {
                    val statusOptions = listOf(strings.all, strings.available, strings.pending, strings.rented)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statusOptions.forEach { status ->
                            val isSelected = selectedStatus.equals(status, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStatus = status },
                                label = { Text(status, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // APPLY FILTER & CLEAR FILTER Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { applyFilter() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text(
                            strings.applyFilter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { clearFilter() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, PrimaryColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryColor)
                    ) {
                        Text(
                            strings.clearFilter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                if (showNoResults) {
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                strings.noPropertiesFound,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B),
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                strings.tryChangingFilters,
                                color = Color(0xFF7F1D1D),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { clearFilter() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(strings.clearFilters, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 5. Furniture Hub Banner at the Bottom
                Surface(
                    onClick = { onNavigate("furniture_choice"); onClose() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFE8EAF6),
                    border = BorderStroke(1.5.dp, PrimaryColor.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Chair,
                                contentDescription = strings.furnitureHub,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                strings.furnitureHub,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryColor
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                strings.furnishYourSpace,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF283593)
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = PrimaryColor
                        )
                    }
                }
            }

            // Footer (Login / Register / Profile)
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (user == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("login"); onClose() }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(strings.loginRegister, fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 15.sp)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("profile"); onClose() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user.profileImage.isNullOrBlank()) {
                                val avatarUrl = ApiClient.resolveUrl(user.profileImage)
                                key(avatarUrl) {
                                    KamelImage(
                                        resource = { asyncPainterResource(avatarUrl) },
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        onFailure = {
                                            Text(user.initials, color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    )
                                }
                            } else {
                                Text(user.initials, color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                            Spacer(Modifier.height(2.dp))
                            Text(strings.accountSettings, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDarkGray)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarHeader(onClose: () -> Unit) {
    val strings = LocalRoomifyStrings.current
    val localizationManager = org.com.i18n.LocalLocalizationManager.current
    val currentLang = localizationManager.currentLanguage

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.85f))),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 36.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HomeWork,
                        contentDescription = "Roomify Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Roomify",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = strings.yourLivingSpacePartner,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(14.dp))

            // Interactive Language Selector Toggle (Swahili / English)
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isSwahili = currentLang == org.com.i18n.Language.SWAHILI
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { localizationManager.changeLanguage(org.com.i18n.Language.SWAHILI) },
                        color = if (isSwahili) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🇹🇿", fontSize = 12.sp)
                            Text(
                                "Kiswahili",
                                fontSize = 11.sp,
                                fontWeight = if (isSwahili) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSwahili) PrimaryColor else Color.White
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { localizationManager.changeLanguage(org.com.i18n.Language.ENGLISH) },
                        color = if (!isSwahili) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🇬🇧", fontSize = 12.sp)
                            Text(
                                "English",
                                fontSize = 11.sp,
                                fontWeight = if (!isSwahili) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSwahili) PrimaryColor else Color.White
                            )
                        }
                    }
                }
            }
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun SidebarDropCard(
    title: String,
    icon: String,
    badgeText: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 14.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEEF2FF),
                    border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                ) {
                    Text(
                        badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Icon(
                    if (isOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )
            }
            if (isOpen) {
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                    content()
                }
            }
        }
    }
}

