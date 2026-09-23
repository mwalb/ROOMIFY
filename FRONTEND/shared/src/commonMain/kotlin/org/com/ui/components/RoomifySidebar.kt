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

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerTonalElevation = 0.dp,
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Header Section (No X close button)
            SidebarHeader()
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // 2. Explore Roomify
                Text(
                    "Explore Roomify",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExploreActionCard(
                        title = "Rentals",
                        subtitle = "Find a room",
                        icon = Icons.Default.Home,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("map"); onClose() }
                    )
                    ExploreActionCard(
                        title = "Furniture",
                        subtitle = "Furnish Your Space",
                        icon = Icons.Default.Chair,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("furniture_choice"); onClose() }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Prominent Furniture Hub Banner
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
                
                Spacer(Modifier.height(28.dp))
                
                // 3. Nearby Me Button at the top area of sidebar (clearly visible without scrolling)
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
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Preferences",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Location Field (placeholder "Location", no heading above)
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
                            modifier = Modifier.padding(bottom = 8.dp)
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

                        // Max Budget Field (placeholder "Max budget", no heading above)
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

                        // Status Section inside Preferences card (directly below Max Budget)
                        Text(
                            "Status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDarkGray,
                            modifier = Modifier.padding(bottom = 8.dp)
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
            }

            // Footer (Login / Register / Profile)
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
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
            .padding(top = 40.dp, bottom = 30.dp, start = 24.dp, end = 24.dp)
    ) {
        Column {
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

@Composable
private fun ExploreActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        color = BackgroundGray,
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDarkGray)
            }
        }
    }
}
