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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.com.model.Shop
import org.com.ui.components.LocationPickerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostShop(
    onBack: () -> Unit,
    onSubmit: (Shop, List<KmpFile>, KmpFile?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    var selectedImages by remember { mutableStateOf(emptyList<KmpFile>()) }
    var selectedVideo by remember { mutableStateOf<KmpFile?>(null) }

    var showMapModal by remember { mutableStateOf(false) }
    var selectedFullAddress by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val imagesPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { selectedImages = selectedImages + it }
    )

    val videoPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Video,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { selectedVideo = it.firstOrNull() }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sajili Duka Lako", fontWeight = FontWeight.Black) },
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
                        val shop = Shop(
                            name = name,
                            description = description,
                            address = address,
                            latitude = latitude.toDoubleOrNull() ?: 0.0,
                            longitude = longitude.toDoubleOrNull() ?: 0.0,
                            contactPhone = phone,
                            contactEmail = email,
                            websiteUrl = website
                        )
                        onSubmit(shop, selectedImages, selectedVideo)
                    },
                    enabled = name.isNotBlank() && phone.isNotBlank() && selectedImages.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Text("SAJILI DUKA", fontWeight = FontWeight.Bold)
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
            Text("Picha za Duka", fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth().height(120.dp).clickable { imagesPicker.launch() },
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                if (selectedImages.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Text("Gusa kuongeza picha za duka", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyRow(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(selectedImages) {
                            Box(Modifier.size(90.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Video Demo
            Text("Video Demo (Optional)", fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp).clickable { videoPicker.launch() },
                color = if (selectedVideo != null) Color(0xFFE8F5E9) else Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (selectedVideo != null) Color(0xFF2E7D32) else Color(0xFFEEEEEE))
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (selectedVideo != null) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                        null,
                        tint = if (selectedVideo != null) Color(0xFF2E7D32) else Color.Gray
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (selectedVideo != null) "Video tayari" else "Ongeza video ya duka",
                        color = if (selectedVideo != null) Color(0xFF2E7D32) else Color.Gray
                    )
                }
            }

            // Form Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Jina la Duka") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Maelezo ya Duka") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = false
            )

            // Contact Information
            Text("Mawasiliano", fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Namba ya Simu") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Phone, null) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Barua Pepe (Email)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            // Location
            Text("Eneo", fontWeight = FontWeight.Bold)

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
                Button(
                    onClick = { showMapModal = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Icon(Icons.Default.Map, null)
                    Spacer(Modifier.width(10.dp))
                    Text("Chagua Eneo kwenye Ramani", fontWeight = FontWeight.Bold)
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
