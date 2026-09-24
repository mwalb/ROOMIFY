package org.com.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import org.com.model.User
import org.com.model.initials

private val PrimaryColor = Color(0xFF1A237E)
private val BackgroundGray = Color(0xFFF1F5F9)
private val TextDarkGray = Color(0xFF374151)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomifySidebar(
    user: User?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit,
    onClose: () -> Unit,
    onMyLocationClick: (() -> Unit)? = null
) {
    var locationInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("ALL") }
    var budgetInput by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("ALL") }

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

        onSearch(type, area, parsedBudget, status)
        onClose()
    }

    Surface(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header Section
            SidebarHeader()
            
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
                            "My Dashboard",
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
                                label = { Text("Overview", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("saved"); onClose() },
                                label = { Text("Favourites", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("bookings"); onClose() },
                                label = { Text("My Bookings", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("messages"); onClose() },
                                label = { Text("Messages", fontWeight = FontWeight.Bold) },
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
                            "Management",
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
                                label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("postroom"); onClose() },
                                label = { Text("Post a Room", fontWeight = FontWeight.Bold) },
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
                            "Dalali Tools",
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
                                label = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = TextDarkGray
                                )
                            )
                            FilterChip(
                                selected = false,
                                onClick = { onNavigate("postroom"); onClose() },
                                label = { Text("Post a Room", fontWeight = FontWeight.Bold) },
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
                            "Administration",
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
                                label = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
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
                        text = "Nearby me",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryColor
                    )
                }

                Spacer(Modifier.height(20.dp))

                // 4. Preferences Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Preferences",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
                        )

                        // Location Field
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
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
                                            Text("Location", color = Color(0xFF9CA3AF), fontSize = 13.sp)
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

                        Spacer(Modifier.height(16.dp))

                        // What are you looking for?
                        Text(
                            "What are you looking for?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkGray,
                            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                        )

                        val propertyTypes = listOf("ALL", "Room", "Apartment", "Studio", "House", "Office")
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

                        Spacer(Modifier.height(16.dp))

                        // Max Budget Field
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                BasicTextField(
                                    value = budgetInput,
                                    onValueChange = { budgetInput = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    decorationBox = { inner ->
                                        if (budgetInput.isEmpty()) {
                                            Text("Max budget", color = Color(0xFF9CA3AF), fontSize = 13.sp)
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

                        Spacer(Modifier.height(16.dp))

                        // Status Section
                        Text(
                            "Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkGray,
                            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                        )

                        val statusOptions = listOf("All", "Available", "Pending", "Rented")
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

                        Spacer(Modifier.height(20.dp))

                        // APPLY FILTERS Button
                        Button(
                            onClick = { applyFilter() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text(
                                "APPLY FILTERS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
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
                                contentDescription = "Furniture Hub",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Furniture Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryColor
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Furnish Your Space",
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
                        Text("Login / Register", fontWeight = FontWeight.Bold, color = PrimaryColor, fontSize = 15.sp)
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
                            Text(user.initials, color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                            Spacer(Modifier.height(2.dp))
                            Text("Account Settings", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDarkGray)
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
private fun SidebarHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(PrimaryColor, PrimaryColor.copy(alpha = 0.85f))),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(top = 40.dp, bottom = 30.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Roomify",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your living space partner",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
