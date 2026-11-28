package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Booking
import org.com.model.Room
import org.com.network.RoomifyApi
import org.com.ui.components.roomifyAmbientBackground
import org.com.ui.theme.RoomifyColors

private val PrimaryColor = Color(0xFF142170)
private val PrimaryLight = Color(0xFF1A237E)
private val SuccessColor = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantScreen(
    user: org.com.model.User,
    bookings: List<Booking> = emptyList(),
    allRooms: List<Room> = emptyList(),
    recentlyViewed: List<Room> = emptyList(),
    savedRooms: List<Room> = emptyList(),
    isRefreshing: Boolean = false,
    onExploreRooms: () -> Unit,
    onViewProperty: (Room) -> Unit = {},
    onViewAll: () -> Unit = {},
    onLogout: () -> Unit = {},
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit = { _, _, _, _ -> },
    onNavigate: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var isSidebarVisible by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBudgetSlang by remember { mutableStateOf<String?>(null) }
    var selectedFeatureFilter by remember { mutableStateOf<String?>(null) }
    var showCustomFilterInputs by remember { mutableStateOf(false) }
    var customMinPrice by remember { mutableStateOf("") }
    var customMaxPrice by remember { mutableStateOf("") }
    var customRequirement by remember { mutableStateOf("") }

    // Dynamic backend suggestions & metadata
    var backendLocationSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var backendPropertyTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedPropertyType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val types = RoomifyApi.getPropertyTypes()
            if (types.isNotEmpty()) {
                backendPropertyTypes = types
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            try {
                val suggestions = RoomifyApi.getLocationSuggestions(q)
                if (suggestions.isNotEmpty()) {
                    backendLocationSuggestions = suggestions
                }
            } catch (e: Exception) {
                // ignore
            }
        } else {
            backendLocationSuggestions = emptyList()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight)))
    ) {
        val width = maxWidth
        val isMobile = width < 600.dp
        val isExpanded = width >= 840.dp
        var mobileDrawerOpen by remember { mutableStateOf(false) }

        val sidebarContent = @Composable {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (isMobile) 280.dp else 260.dp),
                color = PrimaryColor,
                shape = if (isMobile) RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp) else RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("ROOMIFY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    Text("Customer Panel", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (isMobile) {
                                IconButton(onClick = { mobileDrawerOpen = false }) {
                                    Icon(Icons.Default.Close, "Funga Menu", tint = Color.White)
                                }
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TenantSidebarNavItem("Dashboard", Icons.Default.Dashboard, true) { 
                                if (isMobile) mobileDrawerOpen = false
                            }
                            TenantSidebarNavItem("Explore Properties", Icons.Rounded.Explore, false) { 
                                onExploreRooms() 
                                if (isMobile) mobileDrawerOpen = false
                            }
                            TenantSidebarNavItem("My Bookings", Icons.AutoMirrored.Filled.ListAlt, false) { 
                                onNavigate("bookings") 
                                if (isMobile) mobileDrawerOpen = false
                            }
                            TenantSidebarNavItem("Saved Places", Icons.Default.Favorite, false) { 
                                onNavigate("saved") 
                                if (isMobile) mobileDrawerOpen = false
                            }
                            TenantSidebarNavItem("Messages", Icons.Default.Email, false) { 
                                onNavigate("messages") 
                                if (isMobile) mobileDrawerOpen = false
                            }
                            TenantSidebarNavItem("Profile", Icons.Default.Person, false) { 
                                onNavigate("profile") 
                                if (isMobile) mobileDrawerOpen = false
                            }
                        }
                    }

                    // Profile & Logout Footer
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("profile") },
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    user.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(user.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Logged In", color = SuccessColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = onLogout, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Main Dashboard Outer Card
        Card(
            modifier = if (isMobile) Modifier.fillMaxSize() else Modifier
                .fillMaxWidth(if (isExpanded) 0.98f else 0.96f)
                .fillMaxHeight(0.96f)
                .align(Alignment.Center)
                .padding(vertical = 8.dp),
            shape = if (isMobile) RoundedCornerShape(0.dp) else RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = RoomifyColors.Background),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isMobile) 0.dp else 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Persistent Desktop Sidebar
                if (!isMobile) {
                    AnimatedVisibility(
                        visible = isSidebarVisible,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        sidebarContent()
                    }
                }

                // Main Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .roomifyAmbientBackground()
                ) {
                    // Top Bar with Menu Toggle & Search Button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isMobile) 16.dp else 24.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isMobile) mobileDrawerOpen = !mobileDrawerOpen
                                            else isSidebarVisible = !isSidebarVisible
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(PrimaryColor.copy(alpha = 0.08f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Menu, "Toggle Menu", tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(
                                            "Welcome back, ${user.name}!",
                                            fontSize = if (isMobile) 15.sp else 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.WavingHand,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(if (isMobile) 18.dp else 22.dp)
                                        )
                                    }
                                }

                                if (!isMobile) {
                                    Button(
                                        onClick = onExploreRooms,
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Search Places", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }

                            if (isMobile) {
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = onExploreRooms,
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Explore & Search Places", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Body Content Container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 1200.dp)
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                                .padding(
                                    horizontal = if (isMobile) 16.dp else 24.dp,
                                    vertical = if (isMobile) 16.dp else 24.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(if (isMobile) 16.dp else 20.dp)
                        ) {
                            // Instant Place Search Bar with Suggestions on 1st letter
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search places (e.g. Sinza, Mikocheni, Masaki, Mwenge...)", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = PrimaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                singleLine = true
                            )

                            // Quick Reload of Places: Instant display on very first letter (Local + Backend)
                            if (searchQuery.isNotBlank()) {
                                val combinedPlaceSuggestions = remember(searchQuery, backendLocationSuggestions) {
                                    val q = searchQuery.trim().lowercase()
                                    val localSuggestions = PlaceSuggestionEngine.suggest(q).primary.map { it.name }
                                    val list = LinkedHashSet<String>()
                                    (backendLocationSuggestions + localSuggestions).filter { it.lowercase().startsWith(q) }.forEach { list.add(it) }
                                    (backendLocationSuggestions + localSuggestions).filter { item ->
                                        item.lowercase().split(Regex("[\\s,()\\-]+")).any { it.startsWith(q) }
                                    }.forEach { list.add(it) }
                                    (backendLocationSuggestions + localSuggestions).filter { it.lowercase().contains(q) }.forEach { list.add(it) }
                                    list.toList()
                                }

                                if (combinedPlaceSuggestions.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f)),
                                        shadowElevation = 3.dp
                                    ) {
                                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Default.LocationOn, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
                                                    Text(
                                                        "Maeneo yenye herufi '${searchQuery}' (${combinedPlaceSuggestions.size}):",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryColor
                                                    )
                                                }
                                                Text(
                                                    "Futa",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Red,
                                                    modifier = Modifier.clickable { searchQuery = "" }
                                                )
                                            }

                                            LazyRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                items(combinedPlaceSuggestions.take(20)) { placeName ->
                                                    val isSelected = searchQuery.equals(placeName, ignoreCase = true)
                                                    Surface(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                searchQuery = placeName
                                                                onSearch(null, placeName, null, null)
                                                            },
                                                        color = if (isSelected) PrimaryColor else Color(0xFFEEF2FF),
                                                        border = BorderStroke(1.dp, if (isSelected) PrimaryColor else Color(0xFFC7D2FE))
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.LocationOn,
                                                                null,
                                                                tint = if (isSelected) Color.White else PrimaryColor,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = placeName,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) Color.White else PrimaryColor
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Tanzania Quick TZS Budget Slang Filter Chips
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("BAJETI YA HARAKA (TZS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp)
                                    if (selectedBudgetSlang != null || selectedFeatureFilter != null || customMinPrice.isNotBlank() || customMaxPrice.isNotBlank() || customRequirement.isNotBlank() || selectedPropertyType != null) {
                                        Text(
                                            "Weka Upya (Reset All)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryColor,
                                            modifier = Modifier.clickable {
                                                selectedBudgetSlang = null
                                                selectedFeatureFilter = null
                                                customMinPrice = ""
                                                customMaxPrice = ""
                                                customRequirement = ""
                                                selectedPropertyType = null
                                            }
                                        )
                                    }
                                }

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val budgetOptions = listOf(
                                        null to "Zote",
                                        "LAKI_1" to "Chini ya Laki 1",
                                        "LAKI_1_2" to "Laki 1 - 2.5",
                                        "LAKI_3_6" to "Laki 3 - 6",
                                        "LAKI_7" to "Laki 7+"
                                    )
                                    items(budgetOptions) { (key, label) ->
                                        val isSelected = selectedBudgetSlang == key
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) PrimaryColor else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) PrimaryColor else Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable {
                                                selectedBudgetSlang = if (isSelected) null else key
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFF334155),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                // Tanzania Feature Quick Filters (Road, Flood Safe, Campus, Flexible Advance)
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val featureOptions = listOf(
                                        "LAMI" to "🛣️ Lami Tu",
                                        "FLOOD_SAFE" to "🛡️ Salama Mafuriko",
                                        "CAMPUS" to "🎓 Karibu na Chuo",
                                        "MWEZI_1" to "📅 Mwezi kwa Mwezi"
                                    )
                                    items(featureOptions) { (key, label) ->
                                        val isSelected = selectedFeatureFilter == key
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1)),
                                            modifier = Modifier.clickable {
                                                selectedFeatureFilter = if (isSelected) null else key
                                            }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Freeform Custom Input Room: Type & Fill Whatever You Want (Custom Price & Requirements)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showCustomFilterInputs = !showCustomFilterInputs },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.Tune, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                            Column {
                                                Text("Weka Vigezo Vyako Mwenyewe (Type Custom Requirements)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                                Text("Weka kiwango chako cha kodi au sifa unazotaka (Tiles, Jiko, Fensi, AC...)", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }
                                        Icon(
                                            if (showCustomFilterInputs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            null,
                                            tint = PrimaryColor
                                        )
                                    }

                                    if (showCustomFilterInputs) {
                                        HorizontalDivider(color = Color(0xFFF1F5F9))

                                        // Custom Min & Max Price Fields
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedTextField(
                                                value = customMinPrice,
                                                onValueChange = { customMinPrice = it.filter { ch -> ch.isDigit() } },
                                                label = { Text("Kodi ya Chini (TZS)", fontSize = 11.sp) },
                                                placeholder = { Text("mfano 80000", fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = customMaxPrice,
                                                onValueChange = { customMaxPrice = it.filter { ch -> ch.isDigit() } },
                                                label = { Text("Kodi ya Juu (TZS)", fontSize = 11.sp) },
                                                placeholder = { Text("mfano 350000", fontSize = 11.sp) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        // Custom Freeform Requirement / Amenity text field
                                        OutlinedTextField(
                                            value = customRequirement,
                                            onValueChange = { customRequirement = it },
                                            label = { Text("Sifa au Vitu Unavyotaka (Custom Feature)", fontSize = 11.sp) },
                                            placeholder = { Text("mfano: Tiles, Choo ndani, DAWASA, Gypsum, Ghorofani...", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        // Dynamic Property Types from Backend API
                                        if (backendPropertyTypes.isNotEmpty()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text("Aina ya Nyumba (Fetched from Backend):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    items(backendPropertyTypes) { type ->
                                                        val isSel = selectedPropertyType == type
                                                        FilterChip(
                                                            selected = isSel,
                                                            onClick = { selectedPropertyType = if (isSel) null else type },
                                                            label = { Text(type, fontSize = 11.sp) }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (customMinPrice.isNotBlank() || customMaxPrice.isNotBlank() || customRequirement.isNotBlank() || selectedPropertyType != null) {
                                            TextButton(
                                                onClick = {
                                                    customMinPrice = ""
                                                    customMaxPrice = ""
                                                    customRequirement = ""
                                                    selectedPropertyType = null
                                                },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Futa Vigezo vya Ziada (Clear)", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // High-Value Action Cards & Discovery Hero Section
                            DashboardActionsSection(
                                isExpanded = isExpanded,
                                isMobile = isMobile,
                                onExploreRooms = onExploreRooms,
                                onNavigate = onNavigate
                            )

                            // RECOMMENDED FOR YOU (Hot reloaded by search place, budget slang & custom typed filters)
                            val q = searchQuery.trim().lowercase()
                            val minP = customMinPrice.toDoubleOrNull()
                            val maxP = customMaxPrice.toDoubleOrNull()
                            val req = customRequirement.trim().lowercase()

                            val recommendedRooms = allRooms.filter { room ->
                                val matchesPlace = q.isBlank() ||
                                    room.displayLocation.lowercase().contains(q) ||
                                    (room.title?.lowercase()?.contains(q) == true) ||
                                    (room.address?.lowercase()?.contains(q) == true) ||
                                    (room.neighbourhoodPlaces.any { it.lowercase().contains(q) }) ||
                                    (room.nearestCampus?.lowercase()?.contains(q) == true)

                                val matchesBudget = when (selectedBudgetSlang) {
                                    "LAKI_1" -> room.price <= 100000.0
                                    "LAKI_1_2" -> room.price in 100001.0..250000.0
                                    "LAKI_3_6" -> room.price in 250001.0..600000.0
                                    "LAKI_7" -> room.price > 600000.0
                                    else -> true
                                }

                                val matchesCustomMin = minP == null || room.price >= minP
                                val matchesCustomMax = maxP == null || room.price <= maxP

                                val matchesFeature = when (selectedFeatureFilter) {
                                    "LAMI" -> room.roadCondition.equals("LAMI", ignoreCase = true)
                                    "FLOOD_SAFE" -> room.isFloodSafe
                                    "CAMPUS" -> !room.nearestCampus.isNullOrBlank()
                                    "MWEZI_1" -> room.rentAdvanceMonths <= 1
                                    else -> true
                                }

                                val matchesType = selectedPropertyType == null ||
                                    room.propertyType.equals(selectedPropertyType, ignoreCase = true)

                                val matchesCustomReq = req.isBlank() ||
                                    (room.title?.lowercase()?.contains(req) == true) ||
                                    (room.description?.lowercase()?.contains(req) == true) ||
                                    (room.amenities.any { it.lowercase().contains(req) }) ||
                                    (room.rules.any { it.lowercase().contains(req) })

                                room.status == "AVAILABLE" && matchesPlace && matchesBudget && matchesCustomMin && matchesCustomMax && matchesFeature && matchesType && matchesCustomReq
                            }

                            if (recommendedRooms.isNotEmpty()) {
                                DashboardSectionWithAction(if (q.isNotBlank()) "Matching Places (${recommendedRooms.size})" else "Recommended For You", null) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 8.dp)
                                    ) {
                                        items(recommendedRooms.take(8)) { room ->
                                            TenantPropertyCard(room) { onViewProperty(room) }
                                        }
                                    }
                                }
                            }

                            // RECENTLY VIEWED
                            if (recentlyViewed.isNotEmpty()) {
                                DashboardSectionWithAction("Recently Viewed", null) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        contentPadding = PaddingValues(bottom = 8.dp)
                                    ) {
                                        items(recentlyViewed) { room ->
                                            TenantPropertyCard(room) { onViewProperty(room) }
                                        }
                                    }
                                }
                            }

                            // MY BOOKINGS PREVIEW
                            if (bookings.isNotEmpty()) {
                                DashboardSectionWithAction("My Bookings", "View All", { onNavigate("bookings") }) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        bookings.take(3).forEach { booking ->
                                            val room = allRooms.find { it.id == booking.roomId }
                                            BookingStatusCardCompactPreview(booking, room) {
                                                room?.let { onViewProperty(it) }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mobile Navigation Drawer Overlay
        if (isMobile && mobileDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { mobileDrawerOpen = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .clickable(enabled = false) {}
                ) {
                    sidebarContent()
                }
            }
        }
    }
}

@Composable
private fun TenantSidebarNavItem(title: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (active) Color.White.copy(alpha = 0.18f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (active) Color.White else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                color = if (active) Color.White else Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (active) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardActionsSection(
    isExpanded: Boolean,
    isMobile: Boolean,
    onExploreRooms: () -> Unit,
    onNavigate: (String) -> Unit
) {
    if (isExpanded) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Primary Discovery Hero Card
            PrimaryDiscoveryHeroCard(onExploreRooms = onExploreRooms)

            // 3 Secondary High-Value Action Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCardItem(
                    title = "Saved Places",
                    subtitle = "Saved rooms & wishlist",
                    icon = Icons.Default.Favorite,
                    iconBgColor = Color(0xFFFCE7F3),
                    iconTintColor = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("saved") }
                )
                ActionCardItem(
                    title = "My Bookings",
                    subtitle = "Booking requests & status",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    iconBgColor = Color(0xFFFEF3C7),
                    iconTintColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("bookings") }
                )
                ActionCardItem(
                    title = "Messages",
                    subtitle = "Conversations with owners/dalalis",
                    icon = Icons.Default.Email,
                    iconBgColor = Color(0xFFE0E7FF),
                    iconTintColor = PrimaryColor,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("messages") }
                )
            }
        }
    } else {
        // Compact / Medium Layout: Stacked
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryDiscoveryHeroCard(onExploreRooms = onExploreRooms)

            if (isMobile) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCardItem(
                        title = "Saved Places",
                        subtitle = "Saved rooms & wishlist",
                        icon = Icons.Default.Favorite,
                        iconBgColor = Color(0xFFFCE7F3),
                        iconTintColor = Color(0xFFEC4899),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate("saved") }
                    )
                    ActionCardItem(
                        title = "My Bookings",
                        subtitle = "Booking requests & status",
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        iconBgColor = Color(0xFFFEF3C7),
                        iconTintColor = Color(0xFFD97706),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate("bookings") }
                    )
                    ActionCardItem(
                        title = "Messages",
                        subtitle = "Conversations with owners/dalalis",
                        icon = Icons.Default.Email,
                        iconBgColor = Color(0xFFE0E7FF),
                        iconTintColor = PrimaryColor,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate("messages") }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionCardItem(
                        title = "Saved Places",
                        subtitle = "Saved rooms & wishlist",
                        icon = Icons.Default.Favorite,
                        iconBgColor = Color(0xFFFCE7F3),
                        iconTintColor = Color(0xFFEC4899),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("saved") }
                    )
                    ActionCardItem(
                        title = "My Bookings",
                        subtitle = "Booking requests & status",
                        icon = Icons.AutoMirrored.Filled.ListAlt,
                        iconBgColor = Color(0xFFFEF3C7),
                        iconTintColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("bookings") }
                    )
                    ActionCardItem(
                        title = "Messages",
                        subtitle = "Conversations with owners/dalalis",
                        icon = Icons.Default.Email,
                        iconBgColor = Color(0xFFE0E7FF),
                        iconTintColor = PrimaryColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("messages") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryDiscoveryHeroCard(onExploreRooms: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onExploreRooms() },
        shape = RoundedCornerShape(20.dp),
        color = PrimaryColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Explore & Filter Properties",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Browse rooms, filter by price/area, or view on map",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Explore",
                        tint = PrimaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCardItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconBgColor,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTintColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TenantPropertyCard(room: Room, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box {
                KamelImage(
                    resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(130.dp),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PrimaryColor)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Error, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                        }
                    }
                )

                if (room.propertyId != null) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.CorporateFare, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                            Text("COMPLEX", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        room.propertyType ?: "Room",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(room.title ?: "Property", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(room.displayLocation, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(room.formattedPrice, fontSize = 14.sp, color = PrimaryColor, fontWeight = FontWeight.Black)
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Miezi ${room.rentAdvanceMonths}",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (room.isFloodSafe) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "🛡️ Salama",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                    if (!room.nearestCampus.isNullOrBlank()) {
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "🎓 ${room.nearestCampus}",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                room.roadConditionLabel,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionWithAction(title: String, actionLabel: String?, onActionClick: () -> Unit = {}, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            if (actionLabel != null) {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel, fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 14.sp)
                }
            }
        }
        content()
    }
}

@Composable
private fun BookingStatusCardCompactPreview(booking: Booking, room: Room?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(12.dp))) {
                KamelImage(
                    resource = { asyncPainterResource(room?.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { _: Float ->
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = PrimaryColor)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Error, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(booking.roomTitle ?: "Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                val statusColor = when(booking.status) {
                    "ACCEPTED" -> SuccessColor
                    "PENDING" -> Color(0xFFF9A825)
                    else -> Color.Red
                }
                Text(booking.status, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
        }
    }
}
