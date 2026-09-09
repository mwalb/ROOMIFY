package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

import org.com.viewmodel.MapDetail

@Composable
fun DiscoveryDashboard(
    initialMapDetail: MapDetail = MapDetail.STANDARD,
    onSearch: (type: String?, area: String?, maxPrice: Double?, status: String?, detail: MapDetail) -> Unit
) {
    // Discovery Dashboard for property filtering
    var areaQuery by remember { mutableStateOf("") }
    var priceQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedMapDetail by remember { mutableStateOf(initialMapDetail) }
    var detailMenuExpanded by remember { mutableStateOf(false) }
    
    val categories = listOf(
        DiscoveryCategory("Room", Icons.Default.Bed, "Room", Color(0xFFE91E63)),
        DiscoveryCategory("Apartment", Icons.Default.Apartment, "Apartment", Color(0xFF2196F3)),
        DiscoveryCategory("Studio", Icons.Default.HomeWork, "Studio", Color(0xFF4CAF50))
    )

    val statuses = listOf(
        StatusOption("All", null, Color.Gray),
        StatusOption("Available", "AVAILABLE", Color(0xFF2E7D32)),
        StatusOption("Pending", "PENDING", Color(0xFFF9A825)),
        StatusOption("Rented", "RENTED", Color(0xFFC62828))
    )

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF3949AB))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Welcome to Roomify",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Find your perfect living space",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // COMBINED SEARCH CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Search filters", fontWeight = FontWeight.Black, color = Color(0xFF1A237E), fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    // Area Input
                    OutlinedTextField(
                        value = areaQuery,
                        onValueChange = { areaQuery = it },
                        label = { Text("Location / Area", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("e.g. Upanga, Mbezi, City Center") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1A237E)) },
                        trailingIcon = { if(areaQuery.isNotEmpty()) IconButton(onClick = { areaQuery = "" }) { Icon(Icons.Default.Clear, null) } },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A237E),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    Spacer(Modifier.height(14.dp))
                    
                    // Price Input
                    OutlinedTextField(
                        value = priceQuery,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) priceQuery = it },
                        label = { Text("Maximum Budget (TZS)", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("Enter max price") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = { Icon(Icons.Default.Payments, null, tint = Color(0xFF4CAF50)) },
                        trailingIcon = { if(priceQuery.isNotEmpty()) IconButton(onClick = { priceQuery = "" }) { Icon(Icons.Default.Clear, null) } },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A237E),
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    Spacer(Modifier.height(14.dp))

                    // Status Selection
                    Text("PROPERTY STATUS", fontWeight = FontWeight.Black, color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statuses.forEach { option ->
                            val isSelected = selectedStatus == option.value
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStatus = option.value },
                                label = { Text(option.label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = option.color.copy(alpha = 0.2f),
                                    selectedLabelColor = option.color
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    // Category Selection inside the card for clarity
                    Text("PROPERTY TYPE", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { category ->
                            val isSelected = selectedType == category.type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = if (isSelected) null else category.type },
                                label = { Text(category.name, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(category.icon, null, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = category.color.copy(alpha = 0.2f),
                                    selectedLabelColor = category.color,
                                    selectedLeadingIconColor = category.color
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Map Visibility Selection
                    Text("MAP VISIBILITY", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            onClick = { detailMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedMapDetail.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A237E)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF1A237E))
                            }
                        }
                        
                        DropdownMenu(
                            expanded = detailMenuExpanded,
                            onDismissRequest = { detailMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                        ) {
                            MapDetail.entries.forEach { detail ->
                                DropdownMenuItem(
                                    text = { Text(detail.name.lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedMapDetail = detail
                                        detailMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            onSearch(selectedType, areaQuery.ifBlank { null }, priceQuery.toDoubleOrNull(), selectedStatus, selectedMapDetail) 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                    ) {
                        Icon(Icons.Default.Map, null)
                        Spacer(Modifier.width(10.dp))
                        Text("SHOW MATCHING ROOMS", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // POPULAR AREAS
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Popular Locations", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("Mbezi Beach", "Upanga", "Kijitonyama", "Masaki", "Posta").forEach { area ->
                        Surface(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .clickable { 
                                    areaQuery = area
                                    onSearch(selectedType, area, priceQuery.toDoubleOrNull(), selectedStatus, selectedMapDetail)
                                },
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Text(
                                area, 
                                color = Color.White, 
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            // QUICK EXPLORE
            TextButton(
                onClick = { 
                    selectedType = null
                    areaQuery = ""
                    priceQuery = ""
                    selectedStatus = null
                    onSearch(null, null, null, null, selectedMapDetail) 
                },
                modifier = Modifier.alpha(0.8f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("View All Properties", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("View all properties on the map", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

private data class StatusOption(
    val label: String,
    val value: String?,
    val color: Color
)

private data class DiscoveryCategory(
    val name: String,
    val icon: ImageVector,
    val type: String,
    val color: Color
)
