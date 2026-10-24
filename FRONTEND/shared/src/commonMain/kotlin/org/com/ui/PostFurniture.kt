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
import org.com.model.Shop
import org.com.network.RoomifyApi
import org.com.ui.components.AutocompleteTextField
import org.com.ui.components.LocationPickerModal

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
    // Category selection is mandatory - initialized to empty string
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("MPYA") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf(emptyList<KmpFile>()) }
    var selectedVideo by remember { mutableStateOf<KmpFile?>(null) }

    var selectedShop by remember { mutableStateOf<Shop?>(null) }

    var showMapModal by remember { mutableStateOf(false) }
    var selectedFullAddress by remember { mutableStateOf("") }
    
    // Contact Info (for traders / posters)
    var sellerName by remember { mutableStateOf("") }
    var sellerPhone by remember { mutableStateOf("") }
    var sellerEmail by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val categories = listOf(
        "Samani (Furniture)",
        "Mapambo (Home Decor)",
        "Maua na Mimea (Flowers & Plants)",
        "Taa na Mwanga (Lighting)",
        "Vifaa vya Nyumbani (Home Appliances)",
        "Huduma za Ndani (Interior Services)",
        "Nyingine (Other)"
    )

    val conditions = listOf("MPYA", "ZILIZOTUMIKA", "Custom / Made to Order")

    var areaSuggestions by remember { mutableStateOf(emptyList<String>()) }
    LaunchedEffect(Unit) {
        try { areaSuggestions = RoomifyApi.getAreaSuggestions() } catch (e: Exception) {}
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

    val isFormValid = title.isNotBlank() &&
            price.isNotBlank() &&
            category.isNotBlank() &&
            selectedImages.isNotEmpty() &&
            (selectedShop != null || (sellerName.isNotBlank() && sellerPhone.isNotBlank()))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Posta Bidhaa au Huduma", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp, shadowElevation = 16.dp, color = Color.White) {
                Column(Modifier.padding(16.dp)) {
                    if (category.isBlank()) {
                        Text(
                            "⚠️ Lazima uchague Aina ya Bidhaa/Huduma ili kupost",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                    Button(
                        onClick = {
                            val furniture = Furniture(
                                title = title,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                category = category,
                                condition = condition,
                                address = if (selectedShop != null) selectedShop!!.address else address,
                                latitude = if (selectedShop != null) selectedShop!!.latitude else (latitude.toDoubleOrNull() ?: 0.0),
                                longitude = if (selectedShop != null) selectedShop!!.longitude else (longitude.toDoubleOrNull() ?: 0.0),
                                ownerName = if (selectedShop != null) selectedShop!!.name else sellerName,
                                contactPhone = if (selectedShop != null) selectedShop!!.contactPhone else sellerPhone,
                                contactEmail = if (selectedShop != null) selectedShop!!.contactEmail else sellerEmail,
                                shopId = selectedShop?.id,
                                status = "AVAILABLE"
                            )
                            onSubmit(furniture, selectedImages, selectedVideo)
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                    ) {
                        Text("POSTA BIDHAA SASA", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Banner
            Surface(
                color = Color(0xFFEEF2FF),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFC7D2FE))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, null, tint = Color(0xFF1A237E), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Tangaza Samani, Maua na Mapambo", fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                        Text("Wafanyabiashara wa samani, maua, mapambo na utengenezaji wanakaribishwa!", fontSize = 12.sp, color = Color(0xFF3730A3))
                    }
                }
            }

            // Shop Selection
            Text("Chagua Duka (Kama unalo)", fontWeight = FontWeight.Bold)
            var shopExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = shopExpanded,
                onExpandedChange = { shopExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedShop?.name ?: "Binafsi / Mfanyabiashara Hurust (Sio duka)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Duka la Bidhaa") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shopExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Storefront, null, tint = Color(0xFF1A237E)) }
                )
                ExposedDropdownMenu(expanded = shopExpanded, onDismissRequest = { shopExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Binafsi / Mfanyabiashara Huru (Sio duka)") },
                        onClick = { selectedShop = null; shopExpanded = false }
                    )
                    shops.forEach { shop ->
                        DropdownMenuItem(
                            text = { Text(shop.name) },
                            onClick = { selectedShop = shop; shopExpanded = false }
                        )
                    }
                }
            }

            // Mandatory Category Selection
            Text("Aina ya Bidhaa / Huduma (MANDATORY *)", fontWeight = FontWeight.Black, color = if (category.isBlank()) Color(0xFFD32F2F) else Color(0xFF1A237E))
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (category.isNotBlank()) category else "-- Chagua Aina ya Bidhaa/Huduma --",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Aina ya Bidhaa / Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = category.isBlank(),
                    leadingIcon = { Icon(Icons.Default.Category, null, tint = Color(0xFF1A237E)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A237E),
                        unfocusedBorderColor = if (category.isBlank()) Color(0xFFD32F2F) else Color.Gray
                    )
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontWeight = FontWeight.Medium) },
                            onClick = { category = cat; categoryExpanded = false }
                        )
                    }
                }
            }

            // Media Selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Picha za Bidhaa (Angalau picha 1) *", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${selectedImages.size} zilizochaguliwa", fontSize = 12.sp, color = Color.Gray)
            }
            
            Surface(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, if (selectedImages.isEmpty()) Color(0xFFE2E8F0) else Color(0xFF1A237E))
            ) {
                if (selectedImages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().clickable { imagesPicker.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color(0xFF1A237E), modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Gusa hapa kuongeza picha za bidhaa", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                                    }
                                }
                                
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
                                border = BorderStroke(2.dp, Color(0xFF1A237E).copy(alpha = 0.3f))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color(0xFF1A237E))
                                    Text("Ongeza", fontSize = 11.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Video Demo Selection
            Text("Video Demo (Sio lazima)", fontWeight = FontWeight.Bold)
            Surface(
                modifier = Modifier.fillMaxWidth().height(70.dp).clickable { videoPicker.launch() },
                color = if (selectedVideo != null) Color(0xFFE8F5E9) else Color(0xFFF8FAFC),
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
                        if (selectedVideo != null) "Video imechaguliwa" else "Weka video mafupi ya bidhaa",
                        color = if (selectedVideo != null) Color(0xFF2E7D32) else Color.Gray
                    )
                }
            }

            // Form Fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Jina la Bidhaa / Huduma *") },
                placeholder = { Text("e.g. Sofa Set ya Viti 5 au Maua ya Ukutani") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Bei (TZS) *") },
                    placeholder = { Text("e.g. 250000") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                var conditionExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = conditionExpanded,
                    onExpandedChange = { conditionExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hali ya Bidhaa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = conditionExpanded, onDismissRequest = { conditionExpanded = false }) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond) },
                                onClick = { condition = cond; conditionExpanded = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Maelezo Zaidi (Vipimo, Rangi, Aina ya Mti/Maua)") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = false
            )

            if (selectedShop == null) {
                // Contact Information
                Text("Taarifa za Muuzaji / Mfanyabiashara", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Jina Kamili la Muuzaji *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = sellerPhone,
                        onValueChange = { sellerPhone = it },
                        label = { Text("Namba ya Simu *") },
                        placeholder = { Text("07xxxxxxx") },
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
                Text("Eneo Bidhaa/Huduma Inapopatikana", fontWeight = FontWeight.Bold)
                
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
                                Text(address.ifBlank { "Eneo limechaguliwa" }, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
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
                    AutocompleteTextField(
                        value = address,
                        onValueChange = { address = it },
                        suggestions = areaSuggestions,
                        label = "Mtaa / Eneo (Andika hapa)",
                        placeholder = "e.g. Kijitonyama, Dar es Salaam",
                        onSuggestionSelected = { address = it }
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showMapModal = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E), contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Map, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Chagua Eneo kwenye Ramani", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.2f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, null, tint = Color(0xFF1976D2))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Bidhaa hii itapostiwa chini ya duka:", fontSize = 11.sp, color = Color.Gray)
                            Text(selectedShop!!.name, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            Text(selectedShop!!.address ?: "Eneo la duka", fontSize = 12.sp, color = Color(0xFF1976D2))
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

            Spacer(Modifier.height(100.dp))
        }
    }
}
