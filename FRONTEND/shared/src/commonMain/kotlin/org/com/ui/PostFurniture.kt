package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.com.model.Furniture
import org.com.ui.AddressResult
import org.com.ui.components.LocationPickerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFurniture(
    onBack: () -> Unit,
    onSubmit: (Furniture, List<KmpFile>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Bed") }
    var condition by remember { mutableStateOf("NEW") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf(emptyList<KmpFile>()) }
    
    var showMapModal by remember { mutableStateOf(false) }
    var selectedFullAddress by remember { mutableStateOf("") }
    
    // Contact Info (for guest posters)
    var sellerName by remember { mutableStateOf("") }
    var sellerPhone by remember { mutableStateOf("") }
    var sellerEmail by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val categories = listOf("Bed", "Sofa", "Table", "Electronics", "Decor", "Other")

    val imagesPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { newFiles -> 
            // Append new files to existing list to allow incremental adding
            selectedImages = selectedImages + newFiles 
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Posta Bidhaa Yako", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 16.dp, color = Color.White) {
                Button(
                    onClick = {
                        val furniture = Furniture(
                            title = title,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = category,
                            condition = condition,
                            address = address,
                            latitude = latitude.toDoubleOrNull() ?: 0.0,
                            longitude = longitude.toDoubleOrNull() ?: 0.0,
                            ownerName = sellerName,
                            contactPhone = sellerPhone,
                            contactEmail = sellerEmail,
                            status = "AVAILABLE"
                        )
                        onSubmit(furniture, selectedImages)
                    },
                    enabled = title.isNotBlank() && price.isNotBlank() && selectedImages.isNotEmpty() && sellerName.isNotBlank() && sellerPhone.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                ) {
                    Text("POSTA BIDHAA", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Media Selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Picha za Bidhaa", fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text("${selectedImages.size} selected", fontSize = 12.sp, color = Color.Gray)
            }
            
            Surface(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                if (selectedImages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().clickable { imagesPicker.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color(0xFF6200EE), modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Gusa kuongeza picha", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(selectedImages) { file ->
                            Box(modifier = Modifier.size(110.dp)) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFEEEEEE)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                        // On mobile/web, KmpFile can't always be turned into a painter easily without async
                                        // For now, we show a placeholder icon but the logic for "alot of pictures" is there.
                                    }
                                }
                                
                                // Remove button
                                IconButton(
                                    onClick = { selectedImages = selectedImages - file },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 8.dp, y = (-8).dp)
                                        .size(28.dp)
                                        .background(Color.Red, CircleShape)
                                        .padding(4.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        
                        item {
                            Surface(
                                onClick = { imagesPicker.launch() },
                                modifier = Modifier.size(110.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(2.dp, Color(0xFF6200EE).copy(alpha = 0.2f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color(0xFF6200EE))
                                    Text("Add More", fontSize = 11.sp, color = Color(0xFF6200EE), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Form Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Jina la Bidhaa") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Bei (TZS)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Aina") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; expanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Maelezo zaidi") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = false
            )

            // Contact Information
            Text("Taarifa za Muuzaji", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = sellerName,
                onValueChange = { sellerName = it },
                label = { Text("Jina Kamili") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sellerPhone,
                    onValueChange = { sellerPhone = it },
                    label = { Text("Namba ya Simu") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Phone, null) }
                )
                OutlinedTextField(
                    value = sellerEmail,
                    onValueChange = { sellerEmail = it },
                    label = { Text("Email (Sio lazima)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Email, null) }
                )
            }

            // Location
            Text("Eneo la Bidhaa", fontWeight = FontWeight.Bold)
            
            if (latitude.isNotBlank() && longitude.isNotBlank()) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.2f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(address.ifBlank { "Location Selected" }, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            if (selectedFullAddress.isNotBlank()) {
                                Text(selectedFullAddress, fontSize = 11.sp, color = Color(0xFF2E7D32))
                            }
                        }
                        IconButton(onClick = { showMapModal = true }) {
                            Icon(Icons.Default.EditLocationAlt, null, tint = Color(0xFF1B5E20))
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Mtaa / Eneo (Andika hapa)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) }
                )

                Button(
                    onClick = { showMapModal = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E), contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Map, null)
                    Spacer(Modifier.width(10.dp))
                    Text("Chagua kwenye Ramani (Recommended)", fontWeight = FontWeight.Bold)
                }
            }

            if (showMapModal) {
                LocationPickerModal(
                    currentLat = latitude,
                    currentLng = longitude,
                    initialSearch = address,
                    onConfirmed = { result ->
                        latitude = result.latitude.toString()
                        longitude = result.longitude.toString()
                        address = result.address
                        selectedFullAddress = result.formattedAddress
                        showMapModal = false
                    },
                    onDismiss = { showMapModal = false }
                )
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}
