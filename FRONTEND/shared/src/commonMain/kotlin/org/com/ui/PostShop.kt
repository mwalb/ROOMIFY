package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.com.model.Shop
import org.com.ui.components.LocationPickerModal

private val BrandNavy = Color(0xFF142170)
private val BrandIndigo = Color(0xFF283593)
private val BrandBlueLight = Color(0xFFEEF2FF)
private val BrandAmber = Color(0xFFFFB300)
private val BrandSlate = Color(0xFF0F172A)
private val BackgroundSurface = Color(0xFFF8FAFC)
private val BorderColor = Color(0xFFE2E8F0)

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
    var isSubmitting by remember { mutableStateOf(false) }

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
        val isCompact = maxWidth < 600.dp
        val isExpanded = maxWidth >= 840.dp
        val horizontalPadding = if (isExpanded) 24.dp else 16.dp

        Scaffold(
            containerColor = BackgroundSurface,
            topBar = {
                // Branded Navy Gradient Top Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandNavy, BrandIndigo)))
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 1040.dp)
                            .align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Rudi Nyuma",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = BrandAmber,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ROOMIFY VENDOR",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp,
                                        color = BrandNavy,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
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
                                fontSize = if (isCompact) 16.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    color = Color.White,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 680.dp)
                                .padding(horizontal = horizontalPadding, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    if (!isSubmitting) {
                                        isSubmitting = true
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
                                    }
                                },
                                enabled = isFormValid && !isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .pointerHoverIcon(PointerIcon.Hand),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandNavy,
                                    disabledContainerColor = Color(0xFFE2E8F0),
                                    disabledContentColor = Color(0xFF94A3B8)
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                if (isSubmitting) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text("INASAJILI DUKA...", fontWeight = FontWeight.Bold, fontSize = 14.sp, softWrap = false)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Storefront, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("SAJILI DUKA SASA", fontWeight = FontWeight.Bold, fontSize = 14.sp, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1040.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = horizontalPadding, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Promotional Welcome Header Card
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC)))
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(BrandNavy, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Storefront, null, tint = BrandAmber, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Fungua Duka Lako Roomify", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandNavy)
                                Text("Weka bidhaa zako chini ya duka lako ili wateja wakupate kwa urahisi.", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                        }
                    }

                    if (isExpanded) {
                        // Expanded 2-Column Split Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Column (weight 0.45f): Shop Images & Video
                            Column(
                                modifier = Modifier.weight(0.45f),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                ShopMediaSection(
                                    selectedImages = selectedImages,
                                    selectedVideo = selectedVideo,
                                    onPickImages = { imagesPicker.launch() },
                                    onPickVideo = { videoPicker.launch() },
                                    onRemoveImage = { file -> selectedImages = selectedImages - file },
                                    onRemoveVideo = { selectedVideo = null }
                                )
                            }

                            // Right Column (weight 0.55f): Shop Info & Contacts
                            Column(
                                modifier = Modifier.weight(0.55f),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                ShopDetailsSection(
                                    name = name,
                                    onNameChange = { name = it },
                                    description = description,
                                    onDescriptionChange = { description = it },
                                    phone = phone,
                                    onPhoneChange = { phone = it },
                                    email = email,
                                    onEmailChange = { email = it },
                                    address = address,
                                    onAddressChange = { address = it },
                                    latitude = latitude,
                                    longitude = longitude,
                                    selectedFullAddress = selectedFullAddress,
                                    onOpenMap = { showMapModal = true }
                                )
                            }
                        }
                    } else {
                        // Compact / Medium Single-Column Layout
                        ShopMediaSection(
                            selectedImages = selectedImages,
                            selectedVideo = selectedVideo,
                            onPickImages = { imagesPicker.launch() },
                            onPickVideo = { videoPicker.launch() },
                            onRemoveImage = { file -> selectedImages = selectedImages - file },
                            onRemoveVideo = { selectedVideo = null }
                        )

                        ShopDetailsSection(
                            name = name,
                            onNameChange = { name = it },
                            description = description,
                            onDescriptionChange = { description = it },
                            phone = phone,
                            onPhoneChange = { phone = it },
                            email = email,
                            onEmailChange = { email = it },
                            address = address,
                            onAddressChange = { address = it },
                            latitude = latitude,
                            longitude = longitude,
                            selectedFullAddress = selectedFullAddress,
                            onOpenMap = { showMapModal = true }
                        )
                    }

                    Spacer(Modifier.height(40.dp))
                }
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
}

@Composable
private fun ShopMediaSection(
    selectedImages: List<KmpFile>,
    selectedVideo: KmpFile?,
    onPickImages: () -> Unit,
    onPickVideo: () -> Unit,
    onRemoveImage: (KmpFile) -> Unit,
    onRemoveVideo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Shop Photos (Dropzone Card)
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
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        softWrap = false
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (selectedImages.isEmpty()) Color(0xFFCBD5E1) else BrandNavy.copy(alpha = 0.5f)
                ),
                shadowElevation = 1.dp
            ) {
                if (selectedImages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onPickImages() }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(BrandBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.AddPhotoAlternate, null, tint = BrandNavy, modifier = Modifier.size(26.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Gusa kuongeza picha za duka au karakana", color = BrandNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text("Picha ya nje na ndani ya duka", color = Color(0xFF64748B), fontSize = 11.sp)
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(12.dp)) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(selectedImages) { file ->
                                Box(modifier = Modifier.size(105.dp)) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Rounded.Store, null, tint = BrandNavy, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                    IconButton(
                                        onClick = { onRemoveImage(file) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(24.dp)
                                            .background(Color(0xFFEF4444), CircleShape)
                                    ) {
                                        Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }

                            item {
                                Surface(
                                    onClick = onPickImages,
                                    modifier = Modifier
                                        .size(105.dp)
                                        .pointerHoverIcon(PointerIcon.Hand),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.4f))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Rounded.Add, null, tint = BrandNavy, modifier = Modifier.size(24.dp))
                                        Text("Ongeza", fontSize = 11.sp, color = BrandNavy, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedImages.isEmpty()) {
                Text(
                    text = "⚠️ Weka angalau picha 1 ya duka lako",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Compact Video Section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Video ya Duka (Sio lazima)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable { onPickVideo() }
                    .pointerHoverIcon(PointerIcon.Hand),
                color = if (selectedVideo != null) Color(0xFFDCFCE7) else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (selectedVideo != null) Color(0xFF22C55E) else BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (selectedVideo != null) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
                        null,
                        tint = if (selectedVideo != null) Color(0xFF16A34A) else BrandNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (selectedVideo != null) "✓ Video ya duka imechaguliwa" else "Pakia video fupi ya duka au karakana",
                        color = if (selectedVideo != null) Color(0xFF15803D) else Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = if (selectedVideo != null) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedVideo != null) {
                        IconButton(onClick = onRemoveVideo, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Rounded.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopDetailsSection(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    latitude: String,
    longitude: String,
    selectedFullAddress: String,
    onOpenMap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Taarifa za Duka
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Taarifa za Duka", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Jina la Duka / Karakana *") },
                    placeholder = { Text("Mfano: Kibo Furniture & Decor") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Storefront, null, tint = BrandNavy) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandNavy,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                if (name.isBlank()) {
                    Text(
                        text = "⚠️ Jina la duka au karakana linahitajika",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Maelezo ya Duka") },
                placeholder = { Text("Eleza bidhaa unazotengeneza au kuuza, huduma za usafirishaji...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = false,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandNavy,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // Contact Info (Phone, WhatsApp, Email)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Mawasiliano ya Duka", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Namba ya Simu / WhatsApp *") },
                    placeholder = { Text("0712345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Phone, null, tint = BrandNavy) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandNavy,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                if (phone.isBlank()) {
                    Text(
                        text = "⚠️ Namba ya simu / WhatsApp inahitajika",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Barua Pepe") },
                placeholder = { Text("Sio lazima") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Rounded.Email, null, tint = BrandNavy) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandNavy,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        // Location Section (Eneo / Mtaa)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Eneo Duka Lilipo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandSlate)

            if (latitude.isNotBlank() && longitude.isNotBlank()) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color(0xFF16A34A))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(address.ifBlank { "Eneo limechaguliwa" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF14532D))
                            if (selectedFullAddress.isNotBlank()) {
                                Text(selectedFullAddress, fontSize = 11.sp, color = Color(0xFF166534))
                            }
                        }
                        IconButton(onClick = onOpenMap) {
                            Icon(Icons.Rounded.EditLocationAlt, null, tint = Color(0xFF15803D))
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Eneo / Mtaa (Mfano: Magomeni, Dar es Salaam)") },
                    placeholder = { Text("Ingiza mtaa au eneo la duka") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, null, tint = BrandNavy) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandNavy,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Button(
                    onClick = onOpenMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .pointerHoverIcon(PointerIcon.Hand),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandNavy),
                    border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.35f))
                ) {
                    Icon(Icons.Rounded.Map, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Chagua Eneo la Duka kwenye Ramani", fontWeight = FontWeight.Bold, fontSize = 13.sp, softWrap = false)
                }
            }
        }
    }
}
