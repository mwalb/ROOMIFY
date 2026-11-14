package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.com.model.Shop
import org.com.ui.components.LocationPickerModal

private val BrandNavy = Color(0xFF1A237E)
private val BrandIndigo = Color(0xFF3949AB)
private val BrandBlueLight = Color(0xFFEEF2FF)
private val BrandAmber = Color(0xFFFF9800)
private val BrandSlate = Color(0xFF1E293B)
private val BackgroundSurface = Color(0xFFF8FAFC)
private val BorderColor = Color(0xFFE2E8F0)
private val BrandGreen = Color(0xFF10B981)

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

    val isFormValid = name.isNotBlank() && phone.isNotBlank() && selectedImages.isNotEmpty()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundSurface)) {
        val isWideScreen = maxWidth >= 768.dp
        val horizontalPadding = if (isWideScreen) 48.dp else 20.dp

        Scaffold(
            containerColor = BackgroundSurface,
            topBar = {
                // Branded Hero Top App Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandNavy, BrandIndigo)))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Rudi Nyuma",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ROOMIFY VENDOR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    color = BrandAmber
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "DUKA / KARAKANA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Sajili Duka Lako",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    tonalElevation = 10.dp,
                    shadowElevation = 20.dp,
                    color = Color.White,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(
                            visible = !isFormValid,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val hint = when {
                                name.isBlank() -> "✏ Weka jina la duka"
                                phone.isBlank() -> "📞 Weka namba ya simu ya duka"
                                selectedImages.isEmpty() -> "📷 Weka angalau picha 1 ya duka"
                                else -> "Kamilisha taarifa zote"
                            }
                            Surface(
                                color = Color(0xFFFFF7ED),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = hint,
                                    color = Color(0xFFC2410C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val shop = Shop(
                                    name = name.trim(),
                                    description = description.trim(),
                                    address = address.trim(),
                                    latitude = latitude.toDoubleOrNull() ?: 0.0,
                                    longitude = longitude.toDoubleOrNull() ?: 0.0,
                                    contactPhone = phone.trim(),
                                    contactEmail = email.trim(),
                                    websiteUrl = website.trim()
                                )
                                onSubmit(shop, selectedImages, selectedVideo)
                            },
                            enabled = isFormValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 600.dp)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandNavy,
                                disabledContainerColor = Color(0xFFE2E8F0),
                                disabledContentColor = Color(0xFF94A3B8)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("SAJILI DUKA SASA", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = horizontalPadding, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // Promotional Welcome Banner
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E7FF)),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF5F3FF)))
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(BrandNavy, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Fungua Duka Lako Roomify", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandNavy)
                                Text("Weka bidhaa zako chini ya duka lako ili wateja wakupate kwa urahisi.", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                        }
                    }

                    // Shop Images
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Picha za Duka", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)
                                Spacer(Modifier.width(4.dp))
                                Text("*", color = Color.Red, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                color = if (selectedImages.isNotEmpty()) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "${selectedImages.size} zimechaguliwa",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedImages.isNotEmpty()) Color(0xFF15803D) else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.5.dp, if (selectedImages.isEmpty()) Color(0xFFCBD5E1) else BrandNavy.copy(alpha = 0.5f))
                        ) {
                            if (selectedImages.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize().clickable { imagesPicker.launch() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier.size(46.dp).background(BrandBlueLight, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, null, tint = BrandNavy, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Text("Gusa kuongeza picha za duka au karakana", color = BrandNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Picha ya nje na ndani ya duka", color = Color(0xFF64748B), fontSize = 11.sp)
                                    }
                                }
                            } else {
                                LazyRow(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    items(selectedImages) { file ->
                                        Box(modifier = Modifier.size(105.dp)) {
                                            Surface(
                                                modifier = Modifier.fillMaxSize(),
                                                shape = RoundedCornerShape(14.dp),
                                                color = Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, BorderColor)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Store, null, tint = BrandIndigo, modifier = Modifier.size(32.dp))
                                                }
                                            }
                                            IconButton(
                                                onClick = { selectedImages = selectedImages - file },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 6.dp, y = (-6).dp)
                                                    .size(26.dp)
                                                    .background(Color(0xFFEF4444), CircleShape)
                                                    .padding(3.dp)
                                            ) {
                                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    item {
                                        Surface(
                                            onClick = { imagesPicker.launch() },
                                            modifier = Modifier.size(105.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.4f))
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(Icons.Default.Add, null, tint = BrandNavy, modifier = Modifier.size(24.dp))
                                                Text("Ongeza", fontSize = 11.sp, color = BrandNavy, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Video Demo
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Video ya Duka (Sio lazima)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(58.dp).clickable { videoPicker.launch() },
                            color = if (selectedVideo != null) Color(0xFFDCFCE7) else Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (selectedVideo != null) Color(0xFF22C55E) else BorderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (selectedVideo != null) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                    null,
                                    tint = if (selectedVideo != null) Color(0xFF16A34A) else BrandIndigo,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (selectedVideo != null) "✓ Video ya duka imechaguliwa" else "Pakia video fupi ya duka au karakana",
                                    color = if (selectedVideo != null) Color(0xFF15803D) else Color(0xFF475569),
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedVideo != null) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedVideo != null) {
                                    IconButton(onClick = { selectedVideo = null }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Shop Details
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Taarifa za Duka", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Jina la Duka / Karakana *") },
                            placeholder = { Text("Mfano: Kibo Furniture & Decor") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = { Icon(Icons.Default.Storefront, null, tint = BrandNavy) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Maelezo ya Duka") },
                            placeholder = { Text("Eleza bidhaa unazotengeneza au kuuza, huduma za usafirishaji...") },
                            modifier = Modifier.fillMaxWidth().height(115.dp),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    // Contact Info
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Mawasiliano ya Duka", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Namba ya Simu *") },
                                placeholder = { Text("0712345678") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = BrandNavy) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandNavy,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Barua Pepe") },
                                placeholder = { Text("Sio lazima") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = BrandNavy) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandNavy,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                    }

                    // Location
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Eneo Duka Lilipo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)

                        if (latitude.isNotBlank() && longitude.isNotBlank()) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A))
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(address.ifBlank { "Eneo limechaguliwa" }, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                        if (selectedFullAddress.isNotBlank()) {
                                            Text(selectedFullAddress, fontSize = 11.sp, color = Color(0xFF166534))
                                        }
                                    }
                                    IconButton(onClick = { showMapModal = true }) {
                                        Icon(Icons.Default.EditLocationAlt, null, tint = Color(0xFF15803D))
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { showMapModal = true },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandNavy),
                                border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.35f))
                            ) {
                                Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Chagua Eneo la Duka kwenye Ramani", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
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

                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}
