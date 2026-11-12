package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.model.Furniture
import org.com.model.Shop
import org.com.model.User

private val BrandNavy = Color(0xFF1A237E)
private val BrandIndigo = Color(0xFF3949AB)
private val BrandAmber = Color(0xFFFF9800)
private val BackgroundSurface = Color(0xFFF8FAFC)

private data class CategoryItem(val name: String, val emoji: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurnitureDashboard(
    furnitures: List<Furniture>,
    shops: List<Shop> = emptyList(),
    currentUser: User? = null,
    onBack: () -> Unit,
    onViewDetail: (Furniture) -> Unit,
    onViewShop: (Shop) -> Unit = {},
    onEditFurniture: ((Furniture) -> Unit)? = null,
    onDeleteFurniture: ((Furniture) -> Unit)? = null,
    onToggleStatus: ((Furniture) -> Unit)? = null,
    onNavigate: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf(
        CategoryItem("Zote", "✨"),
        CategoryItem("Samani", "🛋️"),
        CategoryItem("Mapambo", "🖼️"),
        CategoryItem("Maua na Mimea", "🌿"),
        CategoryItem("Taa na Mwanga", "💡"),
        CategoryItem("Vifaa vya Nyumbani", "🍳"),
        CategoryItem("Huduma", "🛠️"),
        CategoryItem("Nyingine", "📦")
    )
    var selectedCategory by remember { mutableStateOf("Zote") }
    var searchQuery by remember { mutableStateOf("") }

    var itemToEdit by remember { mutableStateOf<Furniture?>(null) }
    var itemToDelete by remember { mutableStateOf<Furniture?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Vibrant Branded Hero App Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(BrandNavy, BrandIndigo)))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Rudi",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ROOMIFY", color = BrandAmber, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                                        Text("MARKET", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Samani, Maua & Mapambo", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color.White)
                            }
                        }

                        // Post action button
                        Button(
                            onClick = { 
                                if (selectedTab == 0) onNavigate("post_furniture")
                                else onNavigate("post_shop")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (selectedTab == 0) "Posta" else "Sajili Duka", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Modern Search Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tafuta sofa, meza, vitanda, maua, vioo...", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
                        leadingIcon = { 
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(BrandNavy.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Search, null, tint = BrandNavy, modifier = Modifier.size(18.dp))
                            }
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = BrandNavy,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )
                }

                // Animated Pill-style Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    val tabModifier = Modifier.weight(1f)
                    
                    Surface(
                        modifier = tabModifier.clickable { selectedTab = 0 },
                        color = if (selectedTab == 0) BrandNavy else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = if (selectedTab == 0) 3.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🛋️ Bidhaa (${furnitures.size})",
                                color = if (selectedTab == 0) Color.White else Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        modifier = tabModifier.clickable { selectedTab = 1 },
                        color = if (selectedTab == 1) BrandNavy else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = if (selectedTab == 1) 3.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🏬 Maduka & Mafundi (${shops.size})",
                                color = if (selectedTab == 1) Color.White else Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(4.dp))
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    if (selectedTab == 0) onNavigate("post_furniture")
                    else onNavigate("post_shop")
                },
                icon = { Icon(Icons.Default.AddCircle, null) },
                text = { Text(if (selectedTab == 0) "Posta Bidhaa Yako" else "Sajili Duka Lako", fontWeight = FontWeight.Bold) },
                containerColor = BrandNavy,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundSurface)
        ) {
            if (selectedTab == 0) {
                // Interactive Category Filter Bar with Emojis
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { item ->
                        val isSelected = selectedCategory == item.name
                        Surface(
                            modifier = Modifier
                                .clickable { selectedCategory = item.name },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) BrandNavy else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) BrandNavy else Color(0xFFE2E8F0)),
                            shadowElevation = if (isSelected) 3.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(item.emoji, fontSize = 14.sp)
                                Text(
                                    item.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                // Filter logic
                val filtered = furnitures.filter { item ->
                    val matchesCategory = selectedCategory == "Zote" ||
                            item.category.contains(selectedCategory, ignoreCase = true) ||
                            (selectedCategory == "Samani" && listOf("Bed", "Sofa", "Table", "Chair", "Furniture").any { item.category.contains(it, ignoreCase = true) }) ||
                            (selectedCategory == "Mapambo" && listOf("Decor", "Picha", "Vioo").any { item.category.contains(it, ignoreCase = true) }) ||
                            (selectedCategory == "Maua na Mimea" && listOf("Maua", "Mimea", "Flower", "Plant").any { item.category.contains(it, ignoreCase = true) })

                    val matchesSearch = searchQuery.isBlank() ||
                            item.title.contains(searchQuery, ignoreCase = true) ||
                            item.description.contains(searchQuery, ignoreCase = true) ||
                            item.category.contains(searchQuery, ignoreCase = true) ||
                            (item.address?.contains(searchQuery, ignoreCase = true) ?: false)

                    matchesCategory && matchesSearch
                }

                AnimatedContent(
                    targetState = filtered.isEmpty(),
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { isEmpty ->
                    if (isEmpty) {
                        EmptyStatePlaceholder(
                            title = if (searchQuery.isNotBlank()) "Hakuna matokeo ya utafutaji" else "Hakuna bidhaa zilizopatikana",
                            description = if (searchQuery.isNotBlank()) 
                                "Jaribu kubadilisha maneno ya utafutaji au chagua kategoria ya 'Zote'" 
                            else 
                                "Kuwa wa kwanza kuposti samani, mapambo, au vifaa vya nyumbani na ufikie maelfu ya wateja!",
                            actionText = "Posta Bidhaa Sasa",
                            onAction = { onNavigate("post_furniture") }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filtered) { item ->
                                FurnitureCard(
                                    furniture = item,
                                    currentUser = currentUser,
                                    onClick = { onViewDetail(item) },
                                    onEdit = { itemToEdit = item },
                                    onDelete = { itemToDelete = item },
                                    onToggleStatus = { onToggleStatus?.invoke(item) }
                                )
                            }
                            item { Spacer(Modifier.height(88.dp)) }
                        }
                    }
                }
            } else {
                // Shops List
                val filteredShops = shops.filter { shop ->
                    searchQuery.isBlank() ||
                            shop.name.contains(searchQuery, ignoreCase = true) ||
                            (shop.description?.contains(searchQuery, ignoreCase = true) ?: false) ||
                            (shop.address?.contains(searchQuery, ignoreCase = true) ?: false)
                }

                AnimatedContent(
                    targetState = filteredShops.isEmpty(),
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { isEmpty ->
                    if (isEmpty) {
                        EmptyStatePlaceholder(
                            title = "Hakuna maduka yaliyosajiliwa bado",
                            description = "Je, unamiliki gereji, karakana ya samani au duka la vifaa vya ndani? Sajili duka lako bure hapa!",
                            actionText = "Sajili Duka Lako Sasa",
                            onAction = { onNavigate("post_shop") }
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredShops) { shop ->
                                ShopCard(shop, onClick = { onViewShop(shop) })
                            }
                            item { Spacer(Modifier.height(88.dp)) }
                        }
                    }
                }
            }
        }
    }

    // ============================================================
    // DELETE CONFIRMATION DIALOG
    // ============================================================
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp)) },
            title = { Text("Futa Bidhaa", fontWeight = FontWeight.Bold) },
            text = { Text("Je, una uhakika unataka kufuta '${item.title}' kabisa kutoka sokoni?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFurniture?.invoke(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Futa Kabisa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Ghairi", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ============================================================
    // EDIT FURNITURE DIALOG
    // ============================================================
    itemToEdit?.let { item ->
        var editTitle by remember { mutableStateOf(item.title) }
        var editPrice by remember { mutableStateOf(item.price.toLong().toString()) }
        var editDesc by remember { mutableStateOf(item.description) }
        var editAddress by remember { mutableStateOf(item.address ?: "") }
        var editStatus by remember { mutableStateOf(item.status) }

        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Hariri Bidhaa Yako", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Jina la Bidhaa") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it.filter { char -> char.isDigit() } },
                        label = { Text("Bei (TZS)") },
                        prefix = { Text("TZS ", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Maelezo") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Anwani / Eneo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hali:", fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = editStatus == "AVAILABLE",
                                onClick = { editStatus = "AVAILABLE" },
                                label = { Text("INAPATIKANA") }
                            )
                            FilterChip(
                                selected = editStatus == "SOLD",
                                onClick = { editStatus = "SOLD" },
                                label = { Text("IMEUZWA") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedPrice = editPrice.toDoubleOrNull() ?: item.price
                        val updated = item.copy(
                            title = editTitle,
                            price = parsedPrice,
                            description = editDesc,
                            address = editAddress,
                            status = editStatus
                        )
                        onEditFurniture?.invoke(updated)
                        itemToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hifadhi Mabadiliko", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Ghairi", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun EmptyStatePlaceholder(
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .heightIn(min = 360.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().scale(pulseScale),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 6.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(BrandAmber.copy(alpha = 0.25f), BrandNavy.copy(alpha = 0.08f))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Weekend,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = BrandNavy
                    )
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = description,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(actionText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ShopCard(shop: Shop, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                if (shop.firstImageUrl != null) {
                    KamelImage(
                        resource = { asyncPainterResource(shop.firstImageUrl!!) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Storefront, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(shop.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(shop.address ?: "Mtaa haujatajwa", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
                Spacer(Modifier.height(6.dp))
                if (shop.hasVideo) {
                    Surface(color = Color(0xFFFFEBEE), shape = CircleShape) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayCircle, null, tint = Color.Red, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("VIDEO DEMO", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Red)
                        }
                    }
                }
            }
            
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun FurnitureCard(
    furniture: Furniture,
    currentUser: User? = null,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onToggleStatus: (() -> Unit)? = null
) {
    val isOwner = currentUser != null && (
            (furniture.postedBy != null && furniture.postedBy == currentUser.id) ||
            currentUser.role.equals("ADMIN", ignoreCase = true)
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box {
                KamelImage(
                    resource = { asyncPainterResource(furniture.firstImageUrl ?: "") },
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(210.dp),
                    contentScale = ContentScale.Crop,
                    onLoading = { 
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF1A237E))
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                Text("Picha haipatikani", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                )

                if (furniture.category.isNotBlank()) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                        color = Color(0xFF1A237E).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            furniture.category.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        furniture.condition.ifBlank { "MPYA" },
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        furniture.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        furniture.formattedPrice,
                        color = Color(0xFF1A237E),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(furniture.address ?: "Eneo halijatajwa", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).background(Color(0xFFF0F2F5), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(furniture.ownerName ?: "Muuzaji", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                    Spacer(Modifier.weight(1f))
                    Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                        Text("Angalia Zote", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // MANAGEMENT BAR (FOR OWNER / ADMIN)
            if (isOwner) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = if (furniture.status == "SOLD") Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onToggleStatus?.invoke() }
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (furniture.status == "SOLD") Icons.Default.Cancel else Icons.Default.CheckCircle,
                                null,
                                tint = if (furniture.status == "SOLD") Color.Red else Color(0xFF2E7D32),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (furniture.status == "SOLD") "IMEUZWA" else "INAPATIKANA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (furniture.status == "SOLD") Color.Red else Color(0xFF2E7D32)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { onEdit?.invoke() }) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp), tint = Color(0xFF1A237E))
                            Spacer(Modifier.width(4.dp))
                            Text("Hariri", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                        }
                        TextButton(onClick = { onDelete?.invoke() }) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp), tint = Color(0xFFD32F2F))
                            Spacer(Modifier.width(4.dp))
                            Text("Futa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        }
                    }
                }
            }
        }
    }
}
