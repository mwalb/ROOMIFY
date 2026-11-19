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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.calf.io.KmpFile
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.com.model.Furniture
import org.com.model.Shop
import org.com.network.RoomifyApi
import org.com.ui.components.AutocompleteTextField
import org.com.ui.components.LocationPickerModal

private val BrandNavy = Color(0xFF142170)
private val BrandIndigo = Color(0xFF283593)
private val BrandBlueLight = Color(0xFFEEF2FF)
private val BrandAmber = Color(0xFFFFB300)
private val BrandSlate = Color(0xFF0F172A)
private val BackgroundSurface = Color(0xFFF8FAFC)
private val BorderColor = Color(0xFFE2E8F0)

private data class FurnitureCategoryVector(
    val name: String,
    val icon: ImageVector,
    val fullName: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostFurniture(
    shops: List<Shop> = emptyList(),
    onBack: () -> Unit,
    onSubmit: (Furniture, List<KmpFile>, KmpFile?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("MPYA") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf(emptyList<KmpFile>()) }
    var selectedVideo by remember { mutableStateOf<KmpFile?>(null) }

    var selectedShop by remember { mutableStateOf<Shop?>(null) }
    var isPostingAsShop by remember { mutableStateOf(false) }

    var showMapModal by remember { mutableStateOf(false) }
    var selectedFullAddress by remember { mutableStateOf("") }

    // Contact Info (for traders / posters)
    var sellerName by remember { mutableStateOf("") }
    var sellerPhone by remember { mutableStateOf("") }
    var sellerEmail by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val categories = listOf(
        FurnitureCategoryVector("Samani", Icons.Rounded.Chair, "Samani (Furniture)"),
        FurnitureCategoryVector("Mapambo", Icons.Rounded.CropOriginal, "Mapambo (Home Decor)"),
        FurnitureCategoryVector("Maua & Mimea", Icons.Rounded.LocalFlorist, "Maua na Mimea (Flowers & Plants)"),
        FurnitureCategoryVector("Taa & Mwanga", Icons.Rounded.Lightbulb, "Taa na Mwanga (Lighting)"),
        FurnitureCategoryVector("Vifaa vya Ndani", Icons.Rounded.Countertops, "Vifaa vya Nyumbani (Home Appliances)"),
        FurnitureCategoryVector("Huduma za Ndani", Icons.Rounded.Handyman, "Huduma za Ndani (Interior Services)"),
        FurnitureCategoryVector("Nyingine", Icons.Rounded.Category, "Nyingine (Other)")
    )

    val conditions = listOf(
        Triple("MPYA", "Mpya Kabisa", Icons.Rounded.AutoAwesome),
        Triple("ZILIZOTUMIKA", "Imetumika", Icons.Rounded.Autorenew),
        Triple("Custom / Made to Order", "Inatengenezwa (Custom)", Icons.Rounded.Brush)
    )

    var areaSuggestions by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(Unit) {
        try { areaSuggestions = RoomifyApi.getAreaSuggestions() } catch (_: Exception) {}
    }

    val imagesPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Multiple,
        onResult = { newFiles ->
            selectedImages = selectedImages + newFiles
        }
    )

    val videoPicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Video,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { selectedVideo = it.firstOrNull() }
    )

    val effectiveShop = if (isPostingAsShop) selectedShop else null

    val isFormValid = title.isNotBlank() &&
            price.isNotBlank() &&
            category.isNotBlank() &&
            selectedImages.isNotEmpty() &&
            (effectiveShop != null || (sellerName.isNotBlank() && sellerPhone.isNotBlank()))

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(BackgroundSurface)) {
        val isCompact = maxWidth < 600.dp
        val isExpanded = maxWidth >= 840.dp
        val containerPadding = if (isExpanded) 24.dp else 16.dp

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
                            .widthIn(max = 1080.dp)
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
                                        text = "ROOMIFY MARKET",
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
                                        text = "SAMANI & HUDUMA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Posta Bidhaa au Huduma",
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
                                .padding(horizontal = containerPadding, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    if (!isSubmitting) {
                                        isSubmitting = true
                                        val furniture = Furniture(
                                            title = title.trim(),
                                            description = description.trim(),
                                            price = price.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0,
                                            category = category,
                                            condition = condition,
                                            address = if (effectiveShop != null) effectiveShop.address else address,
                                            latitude = if (effectiveShop != null) effectiveShop.latitude else (latitude.toDoubleOrNull() ?: 0.0),
                                            longitude = if (effectiveShop != null) effectiveShop.longitude else (longitude.toDoubleOrNull() ?: 0.0),
                                            ownerName = if (effectiveShop != null) effectiveShop.name else sellerName.trim(),
                                            contactPhone = if (effectiveShop != null) effectiveShop.contactPhone else sellerPhone.trim(),
                                            contactEmail = if (effectiveShop != null) effectiveShop.contactEmail else sellerEmail.trim(),
                                            shopId = effectiveShop?.id,
                                            status = "AVAILABLE"
                                        )
                                        onSubmit(furniture, selectedImages, selectedVideo)
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
                                        Text(
                                            text = "INAPOSTA BIDHAA...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            softWrap = false
                                        )
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Publish,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "POSTA BIDHAA SASA",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            softWrap = false
                                        )
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
                        .widthIn(max = 1080.dp)
                        .verticalScroll(scrollState)
                        .padding(horizontal = containerPadding, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Promo Banner
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
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC))
                                    )
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
                                Icon(
                                    imageVector = Icons.Rounded.Storefront,
                                    contentDescription = null,
                                    tint = BrandAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Fikia Maelfu ya Wateja",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BrandNavy
                                )
                                Text(
                                    text = "Tangaza samani, maua, mapambo au huduma za ufundi kwa haraka.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        // Desktop 2-Column Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Column (weight 0.45f): Photos, Video, Summary
                            Column(
                                modifier = Modifier.weight(0.45f),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                MediaSection(
                                    selectedImages = selectedImages,
                                    selectedVideo = selectedVideo,
                                    onPickImages = { imagesPicker.launch() },
                                    onPickVideo = { videoPicker.launch() },
                                    onRemoveImage = { file -> selectedImages = selectedImages - file },
                                    onRemoveVideo = { selectedVideo = null }
                                )

                                // Live Summary Card
                                SummaryCard(
                                    category = category,
                                    price = price,
                                    condition = condition,
                                    title = title
                                )
                            }

                            // Right Column (weight 0.55f): Inputs & Options
                            Column(
                                modifier = Modifier.weight(0.55f),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                FormFieldsSection(
                                    categories = categories,
                                    category = category,
                                    onCategorySelect = { category = it },
                                    shops = shops,
                                    isPostingAsShop = isPostingAsShop,
                                    onPostingAsShopChange = { isPostingAsShop = it },
                                    selectedShop = selectedShop,
                                    onShopSelect = { selectedShop = it },
                                    title = title,
                                    onTitleChange = { title = it },
                                    price = price,
                                    onPriceChange = { price = it },
                                    condition = condition,
                                    conditions = conditions,
                                    onConditionChange = { condition = it },
                                    description = description,
                                    onDescriptionChange = { description = it },
                                    effectiveShop = effectiveShop,
                                    sellerName = sellerName,
                                    onSellerNameChange = { sellerName = it },
                                    sellerPhone = sellerPhone,
                                    onSellerPhoneChange = { sellerPhone = it },
                                    sellerEmail = sellerEmail,
                                    onSellerEmailChange = { sellerEmail = it },
                                    address = address,
                                    onAddressChange = { address = it },
                                    latitude = latitude,
                                    longitude = longitude,
                                    areaSuggestions = areaSuggestions,
                                    selectedFullAddress = selectedFullAddress,
                                    onOpenMap = { showMapModal = true }
                                )
                            }
                        }
                    } else {
                        // Compact / Medium Single-Column Layout
                        FormFieldsSection(
                            categories = categories,
                            category = category,
                            onCategorySelect = { category = it },
                            shops = shops,
                            isPostingAsShop = isPostingAsShop,
                            onPostingAsShopChange = { isPostingAsShop = it },
                            selectedShop = selectedShop,
                            onShopSelect = { selectedShop = it },
                            title = title,
                            onTitleChange = { title = it },
                            price = price,
                            onPriceChange = { price = it },
                            condition = condition,
                            conditions = conditions,
                            onConditionChange = { condition = it },
                            description = description,
                            onDescriptionChange = { description = it },
                            effectiveShop = effectiveShop,
                            sellerName = sellerName,
                            onSellerNameChange = { sellerName = it },
                            sellerPhone = sellerPhone,
                            onSellerPhoneChange = { sellerPhone = it },
                            sellerEmail = sellerEmail,
                            onSellerEmailChange = { sellerEmail = it },
                            address = address,
                            onAddressChange = { address = it },
                            latitude = latitude,
                            longitude = longitude,
                            areaSuggestions = areaSuggestions,
                            selectedFullAddress = selectedFullAddress,
                            onOpenMap = { showMapModal = true },
                            mediaContent = {
                                MediaSection(
                                    selectedImages = selectedImages,
                                    selectedVideo = selectedVideo,
                                    onPickImages = { imagesPicker.launch() },
                                    onPickVideo = { videoPicker.launch() },
                                    onRemoveImage = { file -> selectedImages = selectedImages - file },
                                    onRemoveVideo = { selectedVideo = null }
                                )
                            }
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
private fun MediaSection(
    selectedImages: List<KmpFile>,
    selectedVideo: KmpFile?,
    onPickImages: () -> Unit,
    onPickVideo: () -> Unit,
    onRemoveImage: (KmpFile) -> Unit,
    onRemoveVideo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Product Photos (Dropzone / Picker Card)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Picha za Bidhaa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrandSlate
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("*", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = if (selectedImages.isNotEmpty()) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${selectedImages.size} zilizochaguliwa",
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
                    .heightIn(min = 200.dp),
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(BrandBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = BrandNavy,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Gusa au buruta picha hapa",
                                color = BrandNavy,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Unaweza kuchagua picha kadhaa kwa pamoja",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.padding(12.dp)) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(selectedImages) { file ->
                                Box(modifier = Modifier.size(110.dp)) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.Image,
                                                contentDescription = null,
                                                tint = BrandNavy,
                                                modifier = Modifier.size(32.dp)
                                            )
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
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Ondoa",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                Surface(
                                    onClick = onPickImages,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .pointerHoverIcon(PointerIcon.Hand),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.4f))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = BrandNavy,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Ongeza Picha",
                                            fontSize = 11.sp,
                                            color = BrandNavy,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Inline helper text for photos requirement
            if (selectedImages.isEmpty()) {
                Text(
                    text = "⚠️ Weka angalau picha 1 ya bidhaa au huduma yako",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Compact Video Card
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Video ya Bidhaa (Sio lazima)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandSlate
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable { onPickVideo() }
                    .pointerHoverIcon(PointerIcon.Hand),
                color = if (selectedVideo != null) Color(0xFFDCFCE7) else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (selectedVideo != null) Color(0xFF22C55E) else BorderColor
                ),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (selectedVideo != null) Icons.Rounded.CheckCircle else Icons.Rounded.PlayCircle,
                        contentDescription = null,
                        tint = if (selectedVideo != null) Color(0xFF16A34A) else BrandNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (selectedVideo != null) "✓ Video imechaguliwa" else "Pakia video fupi ya kuonyesha bidhaa",
                        color = if (selectedVideo != null) Color(0xFF15803D) else Color(0xFF475569),
                        fontSize = 12.sp,
                        fontWeight = if (selectedVideo != null) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedVideo != null) {
                        IconButton(
                            onClick = onRemoveVideo,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Ondoa Video",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    category: String,
    price: String,
    condition: String,
    title: String
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Visibility, null, tint = BrandNavy, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Muhtasari wa Posti Yako",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandNavy
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kategoria:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(
                    if (category.isNotBlank()) category.takeWhile { it != '(' }.trim() else "Haijachaguliwa",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandSlate
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Hali:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(
                    condition,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Bei:", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(
                    if (price.isNotBlank()) "TZS $price" else "Haijawekwa",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandNavy
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FormFieldsSection(
    categories: List<FurnitureCategoryVector>,
    category: String,
    onCategorySelect: (String) -> Unit,
    shops: List<Shop>,
    isPostingAsShop: Boolean,
    onPostingAsShopChange: (Boolean) -> Unit,
    selectedShop: Shop?,
    onShopSelect: (Shop?) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    condition: String,
    conditions: List<Triple<String, String, ImageVector>>,
    onConditionChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    effectiveShop: Shop?,
    sellerName: String,
    onSellerNameChange: (String) -> Unit,
    sellerPhone: String,
    onSellerPhoneChange: (String) -> Unit,
    sellerEmail: String,
    onSellerEmailChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    latitude: String,
    longitude: String,
    areaSuggestions: List<String>,
    selectedFullAddress: String,
    onOpenMap: () -> Unit,
    mediaContent: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Render media content if provided (in single-column layout)
        mediaContent?.invoke()

        // 1. Aina ya Bidhaa / Huduma * (Chips using FlowRow)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Aina ya Bidhaa / Huduma",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandSlate
                )
                Spacer(Modifier.width(4.dp))
                Text("*", color = Color.Red, fontWeight = FontWeight.Bold)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat.fullName
                    Surface(
                        onClick = { onCategorySelect(cat.fullName) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) BrandNavy else Color.White,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) BrandNavy else BorderColor
                        ),
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) BrandAmber else BrandNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = cat.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else BrandSlate,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (category.isBlank()) {
                Text(
                    text = "⚠️ Chagua aina ya bidhaa au huduma hapa juu",
                    color = Color(0xFFDC2626),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Aina ya Muuzaji Segmented Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Aina ya Muuzaji",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandSlate
            )

            if (shops.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                onPostingAsShopChange(false)
                                onShopSelect(null)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isPostingAsShop) Color.White else Color.Transparent,
                            shadowElevation = if (!isPostingAsShop) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = if (!isPostingAsShop) BrandNavy else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Muuza Huru (Binafsi)",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isPostingAsShop) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isPostingAsShop) BrandNavy else Color(0xFF64748B),
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                onPostingAsShopChange(true)
                                if (selectedShop == null) onShopSelect(shops.firstOrNull())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isPostingAsShop) Color.White else Color.Transparent,
                            shadowElevation = if (isPostingAsShop) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Storefront,
                                    contentDescription = null,
                                    tint = if (isPostingAsShop) BrandNavy else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Chini ya Duka (${shops.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isPostingAsShop) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPostingAsShop) BrandNavy else Color(0xFF64748B),
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (isPostingAsShop) {
                    var shopExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = shopExpanded,
                        onExpandedChange = { shopExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedShop?.name ?: "-- Chagua Duka --",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chagua Duka Lako") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shopExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Rounded.Storefront, null, tint = BrandNavy) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = shopExpanded,
                            onDismissRequest = { shopExpanded = false }
                        ) {
                            shops.forEach { shop ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(shop.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                shop.address ?: "Eneo halijawekwa",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        onShopSelect(shop)
                                        shopExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Maelezo ya Msingi (Jina, Bei, Hali)
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Maelezo ya Msingi",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = BrandSlate
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Jina la Bidhaa au Huduma *") },
                    placeholder = { Text("Mfano: Sofa ya Viti 5, Meza ya Mbao, au Maua ya Sebule") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Rounded.Inventory2, null, tint = BrandNavy) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandNavy,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                if (title.isBlank()) {
                    Text(
                        text = "⚠️ Jina la bidhaa au huduma linahitajika",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { input -> onPriceChange(input.filter { it.isDigit() }) },
                    label = { Text("Bei (TZS) *") },
                    placeholder = { Text("e.g. 250000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text(
                            text = "TZS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandNavy,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandNavy,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                if (price.isBlank()) {
                    Text(
                        text = "⚠️ Bei ya bidhaa inahitajika",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hali ya Bidhaa (Condition Chips)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Hali ya Bidhaa:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    conditions.forEach { (condValue, condLabel, condIcon) ->
                        val isSelected = condition == condValue
                        Surface(
                            onClick = { onConditionChange(condValue) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BrandNavy else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) BrandNavy else BorderColor),
                            modifier = Modifier
                                .weight(1f)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    condIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) BrandAmber else BrandNavy,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = condLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else BrandSlate,
                                    textAlign = TextAlign.Center,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Maelezo ya Ziada") },
                placeholder = { Text("Weka vipimo, rangi, aina ya mti, kitambaa au utaratibu wa usafirishaji...") },
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

        // 4. Contact Info & Location (Only if NOT under shop)
        if (effectiveShop == null) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Mawasiliano ya Muuzaji",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandSlate
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = sellerName,
                        onValueChange = onSellerNameChange,
                        label = { Text("Jina Kamili la Muuzaji *") },
                        placeholder = { Text("Mfano: Nestory Karani") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Rounded.Person, null, tint = BrandNavy) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandNavy,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                    if (sellerName.isBlank()) {
                        Text(
                            text = "⚠️ Jina la muuzaji linahitajika",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = sellerPhone,
                        onValueChange = onSellerPhoneChange,
                        label = { Text("Namba ya Simu *") },
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
                    if (sellerPhone.isBlank()) {
                        Text(
                            text = "⚠️ Namba ya simu inahitajika",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                OutlinedTextField(
                    value = sellerEmail,
                    onValueChange = onSellerEmailChange,
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

                // Location Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Eneo Bidhaa/Huduma Inapopatikana",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrandSlate
                    )

                    if (latitude.isNotBlank() && longitude.isNotBlank()) {
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = address.ifBlank { "Eneo limechaguliwa" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF14532D)
                                    )
                                    if (selectedFullAddress.isNotBlank()) {
                                        Text(
                                            text = selectedFullAddress,
                                            fontSize = 11.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                                IconButton(onClick = onOpenMap) {
                                    Icon(
                                        imageVector = Icons.Rounded.EditLocationAlt,
                                        contentDescription = "Badili Eneo",
                                        tint = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    } else {
                        AutocompleteTextField(
                            value = address,
                            onValueChange = onAddressChange,
                            suggestions = areaSuggestions,
                            label = "Mtaa / Eneo (Andika au chagua)",
                            placeholder = "Mfano: Sinza, Kijitonyama, Masaki...",
                            onSuggestionSelected = onAddressChange
                        )

                        Button(
                            onClick = onOpenMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = BrandNavy
                            ),
                            border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Rounded.Map, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Chagua Eneo kwenye Ramani", fontWeight = FontWeight.Bold, fontSize = 13.sp, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
