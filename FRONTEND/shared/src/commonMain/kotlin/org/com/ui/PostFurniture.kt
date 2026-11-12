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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val BrandNavy = Color(0xFF1A237E)
private val BrandIndigo = Color(0xFF3949AB)
private val BrandBlueLight = Color(0xFFE8EAF6)
private val BrandAmber = Color(0xFFFF9800)
private val BrandSlate = Color(0xFF1E293B)
private val BackgroundSurface = Color(0xFFF8FAFC)
private val BorderColor = Color(0xFFE2E8F0)
private val BrandSuccess = Color(0xFF10B981)

private data class CategoryItem(
    val name: String,
    val emoji: String,
    val fullName: String
)

@OptIn(ExperimentalMaterial3Api::class)
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

    val scrollState = rememberScrollState()

    val categories = listOf(
        CategoryItem("Samani", "🛋️", "Samani (Furniture)"),
        CategoryItem("Mapambo", "🖼️", "Mapambo (Home Decor)"),
        CategoryItem("Maua & Mimea", "🌿", "Maua na Mimea (Flowers & Plants)"),
        CategoryItem("Taa & Mwanga", "💡", "Taa na Mwanga (Lighting)"),
        CategoryItem("Vifaa vya Ndani", "🔌", "Vifaa vya Nyumbani (Home Appliances)"),
        CategoryItem("Huduma za Ndani", "🛠️", "Huduma za Ndani (Interior Services)"),
        CategoryItem("Nyingine", "✨", "Nyingine (Other)")
    )

    val conditions = listOf(
        Pair("MPYA", "✨ Mpya Kabisa"),
        Pair("ZILIZOTUMIKA", "🔄 Imetumika"),
        Pair("Custom / Made to Order", "🎨 Inatengenezwa (Custom)")
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

    Scaffold(
        containerColor = BackgroundSurface,
        topBar = {
            // Branded Vibrant Gradient Hero Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(BrandNavy, BrandIndigo)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
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
                                text = "ROOMIFY MARKET",
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
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Helpful validation hint
                    AnimatedVisibility(
                        visible = !isFormValid,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val missingStep = when {
                            category.isBlank() -> "⚠️ Chagua Aina ya Bidhaa/Huduma hapo juu"
                            selectedImages.isEmpty() -> "📷 Weka angalau picha 1 ya bidhaa"
                            title.isBlank() -> "✏️ Andika jina la bidhaa"
                            price.isBlank() -> "💰 Weka bei ya bidhaa"
                            effectiveShop == null && sellerName.isBlank() -> "👤 Weka jina lako la muuzaji"
                            effectiveShop == null && sellerPhone.isBlank() -> "📞 Weka namba yako ya simu"
                            else -> "Kamilisha taarifa zote zinazohitajika"
                        }
                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = missingStep,
                                color = Color(0xFFC2410C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
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
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
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
                            Icon(
                                imageVector = Icons.Default.Publish,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "POSTA BIDHAA SASA",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
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
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. Promotional Callout Banner
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
                            Brush.linearGradient(
                                listOf(Color(0xFFEEF2FF), Color(0xFFF5F3FF))
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(listOf(BrandNavy, BrandIndigo)),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
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

            // 2. Interactive Visual Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    if (category.isNotBlank()) {
                        Surface(
                            color = BrandBlueLight,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Imechaguliwa",
                                color = BrandNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Gusa aina inayoelezea bidhaa au huduma yako vizuri:",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                // Chips row for fast 1-tap category selection
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = category == cat.fullName
                        Surface(
                            onClick = { category = cat.fullName },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) BrandNavy else Color.White,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) BrandNavy else BorderColor
                            ),
                            shadowElevation = if (isSelected) 3.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.emoji, fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else BrandSlate
                                )
                                if (isSelected) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = BrandAmber,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Shop Affiliation Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Aina ya Muuzaji",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandSlate
                )

                if (shops.isEmpty()) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = BrandNavy,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Binafsi / Mfanyabiashara Huru",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrandNavy
                                )
                                Text(
                                    text = "Unauza binafsi. Taarifa zako za simu zitaonekana kwa wateja.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                } else {
                    // Segmented choice between Individual and Shop
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = {
                                isPostingAsShop = false
                                selectedShop = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isPostingAsShop) Color.White else Color.Transparent,
                            shadowElevation = if (!isPostingAsShop) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (!isPostingAsShop) BrandNavy else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Muuza Huru (Binafsi)",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isPostingAsShop) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isPostingAsShop) BrandNavy else Color(0xFF64748B)
                                )
                            }
                        }

                        Surface(
                            onClick = {
                                isPostingAsShop = true
                                if (selectedShop == null) selectedShop = shops.firstOrNull()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isPostingAsShop) Color.White else Color.Transparent,
                            shadowElevation = if (isPostingAsShop) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isPostingAsShop) BrandNavy else Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Chini ya Duka (${shops.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (isPostingAsShop) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPostingAsShop) BrandNavy else Color(0xFF64748B)
                                )
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
                                shape = RoundedCornerShape(14.dp),
                                leadingIcon = {
                                    Icon(Icons.Default.Storefront, null, tint = BrandNavy)
                                },
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
                                            selectedShop = shop
                                            shopExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Product Photos Section
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
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp),
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
                                .clickable { imagesPicker.launch() }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(BrandBlueLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = BrandNavy,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Gusa hapa kupakia picha za bidhaa",
                                    color = BrandNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Unaweza kuchagua picha kadhaa kwa pamoja (Picha 1 au zaidi)",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
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
                                Box(modifier = Modifier.size(110.dp)) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Image,
                                                    contentDescription = null,
                                                    tint = BrandIndigo,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "Picha",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
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
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Ondoa",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                Surface(
                                    onClick = { imagesPicker.launch() },
                                    modifier = Modifier.size(110.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.4f))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
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

            // 5. Video Demo Selection (Optional)
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
                        .height(60.dp)
                        .clickable { videoPicker.launch() },
                    color = if (selectedVideo != null) Color(0xFFDCFCE7) else Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (selectedVideo != null) Color(0xFF22C55E) else BorderColor
                    ),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedVideo != null) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = if (selectedVideo != null) Color(0xFF16A34A) else BrandIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (selectedVideo != null) "✓ Video ya bidhaa imechaguliwa" else "Pakia video fupi ya kuonyesha bidhaa",
                            color = if (selectedVideo != null) Color(0xFF15803D) else Color(0xFF475569),
                            fontSize = 13.sp,
                            fontWeight = if (selectedVideo != null) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedVideo != null) {
                            IconButton(
                                onClick = { selectedVideo = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Ondoa Video",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Basic Information (Title, Price, Condition)
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Maelezo ya Msingi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandSlate
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Jina la Bidhaa au Huduma *") },
                    placeholder = { Text("Mfano: Sofa ya Viti 5, Meza ya Mbao, au Maua ya Sebule") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Inventory2, null, tint = BrandNavy)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandNavy,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Price and Condition
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { input ->
                            // Allow digits only
                            price = input.filter { it.isDigit() }
                        },
                        label = { Text("Bei (TZS) *") },
                        placeholder = { Text("e.g. 250000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Text(
                                text = "TZS",
                                fontSize = 11.sp,
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
                        )
                    )
                }

                // Condition Choice Chips
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
                        conditions.forEach { (condValue, condLabel) ->
                            val isSelected = condition == condValue
                            Surface(
                                onClick = { condition = condValue },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrandNavy else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BrandNavy else BorderColor
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = condLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else BrandSlate,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Maelezo ya Ziada") },
                    placeholder = { Text("Weka vipimo, rangi, aina ya mti, kitambaa au utaratibu wa usafirishaji...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
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

            // 7. Seller Info & Location (Only if NOT under a shop)
            if (effectiveShop == null) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Mawasiliano ya Muuzaji",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = BrandSlate
                    )

                    OutlinedTextField(
                        value = sellerName,
                        onValueChange = { sellerName = it },
                        label = { Text("Jina Kamili la Muuzaji *") },
                        placeholder = { Text("Mfano: Nestory Karani") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = BrandNavy)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandNavy,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = sellerPhone,
                            onValueChange = { sellerPhone = it },
                            label = { Text("Namba ya Simu *") },
                            placeholder = { Text("0712345678") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, null, tint = BrandNavy)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = sellerEmail,
                            onValueChange = { sellerEmail = it },
                            label = { Text("Barua Pepe") },
                            placeholder = { Text("Sio lazima") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(Icons.Default.Email, null, tint = BrandNavy)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandNavy,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

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
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = address.ifBlank { "Eneo limechaguliwa" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
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
                                    IconButton(onClick = { showMapModal = true }) {
                                        Icon(
                                            imageVector = Icons.Default.EditLocationAlt,
                                            contentDescription = "Badili Eneo",
                                            tint = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        } else {
                            AutocompleteTextField(
                                value = address,
                                onValueChange = { address = it },
                                suggestions = areaSuggestions,
                                label = "Mtaa / Eneo (Andika au chagua)",
                                placeholder = "Mfano: Sinza, Kijitonyama, Masaki...",
                                onSuggestionSelected = { address = it }
                            )

                            Button(
                                onClick = { showMapModal = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = BrandNavy
                                ),
                                border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.3f))
                            ) {
                                Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Chagua Eneo kwenye Ramani", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                // Shop Summary Card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.5.dp, BrandNavy.copy(alpha = 0.2f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFEEF2FF), Color.White)
                                )
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(BrandNavy, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Bidhaa itapostiwa chini ya duka:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = effectiveShop.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = BrandNavy
                            )
                            Text(
                                text = effectiveShop.address ?: "Eneo la duka",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
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

            Spacer(Modifier.height(40.dp))
        }
    }
}
