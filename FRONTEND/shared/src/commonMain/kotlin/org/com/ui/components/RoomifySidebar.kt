package org.com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.User
import org.com.network.ApiClient
import org.com.network.RoomifyApi

private val PrimaryColor = Color(0xFF1A237E)
private val BackgroundGray = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomifySidebar(
    user: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit,
    onClose: () -> Unit
) {
    var areaQuery by remember { mutableStateOf("") }
    var priceQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    
    var propertyTypes by remember { mutableStateOf(listOf("Room", "Apartment", "Studio", "House", "Office")) }
    var areaSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var filtersExpanded by remember { mutableStateOf(false) }

    val statuses = listOf(
        StatusOption("All Statuses", null, Color.Gray),
        StatusOption("Available", "AVAILABLE", Color(0xFF2E7D32)),
        StatusOption("Pending", "PENDING", Color(0xFFF9A825)),
        StatusOption("Rented", "RENTED", Color(0xFFC62828))
    )

    LaunchedEffect(Unit) {
        try {
            val fetchedTypes = RoomifyApi.getPropertyTypes()
            if (fetchedTypes.isNotEmpty()) propertyTypes = fetchedTypes
            areaSuggestions = RoomifyApi.getAreaSuggestions()
        } catch (e: Exception) {
            println("RoomifySidebar: Error fetching metadata: ${e.message}")
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerTonalElevation = 0.dp,
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Header Section
            SidebarHeader(user, onNavigate, onClose)
            
            Column(modifier = Modifier.padding(16.dp)) {
                
                // 2. Quick Action Grid (2x2)
                Text(
                    "Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Rentals",
                        subtitle = "Find a room",
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("map"); onClose() }
                    )
                    QuickActionCard(
                        title = "Furniture",
                        subtitle = "Shop hub",
                        icon = Icons.Default.Chair,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("furniture_dashboard"); onClose() }
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Post",
                        subtitle = "List room",
                        icon = Icons.Default.AddBusiness,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("postroom"); onClose() }
                    )
                    QuickActionCard(
                        title = "Bookings",
                        subtitle = "My activity",
                        icon = Icons.Default.EventNote,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("bookings"); onClose() }
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                // 3. Search Bar
                Text(
                    "Search",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                AutocompleteTextField(
                    value = areaQuery,
                    onValueChange = { areaQuery = it },
                    suggestions = areaSuggestions,
                    label = "Where to?",
                    placeholder = "Enter area...",
                    icon = Icons.Default.Search,
                    onSuggestionSelected = { 
                        areaQuery = it
                        onSearch(selectedType, it, priceQuery.toDoubleOrNull(), selectedStatus)
                        onClose()
                    }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // 4. Collapsible Filters
                FilterSection(
                    expanded = filtersExpanded,
                    onToggle = { filtersExpanded = !filtersExpanded },
                    priceQuery = priceQuery,
                    onPriceChange = { priceQuery = it },
                    selectedType = selectedType,
                    onTypeChange = { selectedType = it },
                    selectedStatus = selectedStatus,
                    onStatusChange = { selectedStatus = it },
                    propertyTypes = propertyTypes,
                    statuses = statuses,
                    onApply = {
                        onSearch(selectedType, areaQuery.ifBlank { null }, priceQuery.toDoubleOrNull(), selectedStatus)
                        onClose()
                    }
                )
                
                Spacer(Modifier.height(24.dp))
                
                // 5. Recent History / Favorites
                Text(
                    "Recently Viewed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                RecentHistoryItem("UDSM", "Magufuli Hostels", Icons.Default.History) { 
                    areaQuery = "UDSM"
                    onSearch(null, "UDSM", null, null)
                    onClose()
                }
                RecentHistoryItem("Hubert Kairuki", "University area", Icons.Default.History) {
                    areaQuery = "Hubert Kairuki"
                    onSearch(null, "Hubert Kairuki", null, null)
                    onClose()
                }

                Spacer(Modifier.height(24.dp))

                // 6. View All Properties (from DiscoveryDashboard)
                Surface(
                    onClick = {
                        selectedType = null
                        areaQuery = ""
                        priceQuery = ""
                        selectedStatus = null
                        onSearch(null, null, null, null)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryColor.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("View All Properties", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Explore everything on the map", color = Color.Gray, fontSize = 11.sp)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Logout
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
                SidebarItem(
                    label = "Logout",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    color = Color.Red,
                    onClick = onLogout
                )
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SidebarHeader(user: User?, onNavigate: (String) -> Unit, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.8f))))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            if (user != null) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onNavigate("profile"); onClose() }
                ) {
                    if (!user.profileImage.isNullOrBlank()) {
                        KamelImage(
                            resource = { asyncPainterResource(ApiClient.resolveUrl(user.profileImage!!)) },
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(user.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text(user.role, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            } else {
                Text("Roomify", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
                Text("Your living space partner", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            }
        }
        
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White)
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = BackgroundGray,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SidebarItem(label: String, icon: ImageVector, color: Color = Color.Black, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RecentHistoryItem(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Color.LightGray.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
private fun FilterSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    priceQuery: String,
    onPriceChange: (String) -> Unit,
    selectedType: String?,
    onTypeChange: (String?) -> Unit,
    selectedStatus: String?,
    onStatusChange: (String?) -> Unit,
    propertyTypes: List<String>,
    statuses: List<StatusOption>,
    onApply: () -> Unit
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (expanded) BackgroundGray else Color.Transparent)
            .border(1.dp, if (expanded) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FilterList, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Advanced Filters", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ExpandMore, null, modifier = Modifier.rotate(rotation), tint = Color.Gray)
        }
        
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = priceQuery,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) onPriceChange(it) },
                    label = { Text("Max Budget", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp)) },
                    singleLine = true
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Status Selector (Horizontal chips)
                Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    statuses.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status.value,
                            onClick = { onStatusChange(status.value) },
                            label = { Text(status.label, fontSize = 11.sp) }
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                
                // Type Selector
                Text("Property Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { onTypeChange(null) },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                    propertyTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = onApply,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Apply Filters", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class StatusOption(
    val label: String,
    val value: String?,
    val color: Color
)
