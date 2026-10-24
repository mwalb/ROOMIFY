package org.com.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurnitureDashboard(
    furnitures: List<Furniture>,
    shops: List<Shop> = emptyList(),
    onBack: () -> Unit,
    onViewDetail: (Furniture) -> Unit,
    onViewShop: (Shop) -> Unit = {},
    onNavigate: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val categories = listOf(
        "Zote",
        "Samani",
        "Mapambo",
        "Maua na Mimea",
        "Taa na Mwanga",
        "Vifaa vya Nyumbani",
        "Huduma",
        "Nyingine"
    )
    var selectedCategory by remember { mutableStateOf("Zote") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Soko la Samani na Mapambo", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Samani, Maua, Mapambo na Huduma", fontSize = 11.sp, color = Color.Gray)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF1A237E))
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            if (selectedTab == 0) onNavigate("post_furniture")
                            else onNavigate("post_shop")
                        }) {
                            Icon(Icons.Default.Add, "Posta Yako", tint = Color(0xFF1A237E))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tafuta samani, maua, mapambo...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = Color.Gray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedBorderColor = Color(0xFF1A237E)
                    ),
                    singleLine = true
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF1A237E)
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Bidhaa & Huduma (${furnitures.size})", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Maduka & Wafanyabiashara (${shops.size})", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate("post_furniture") },
                icon = { Icon(Icons.Default.AddCircle, null) },
                text = { Text("Posta Bidhaa Yako", fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFF1A237E),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (selectedTab == 0) {
                // Category Filter Row
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1A237E),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Furniture & Decor List
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

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (filtered.isEmpty()) {
                        item {
                            EmptyStatePlaceholder("Hakuna bidhaa zilizopatikana kulingana na utafutaji wako")
                        }
                    } else {
                        items(filtered) { item ->
                            FurnitureCard(item, onClick = { onViewDetail(item) })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            } else {
                // Shops List
                val filteredShops = shops.filter { shop ->
                    searchQuery.isBlank() ||
                            shop.name.contains(searchQuery, ignoreCase = true) ||
                            (shop.description?.contains(searchQuery, ignoreCase = true) ?: false) ||
                            (shop.address?.contains(searchQuery, ignoreCase = true) ?: false)
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (filteredShops.isEmpty()) {
                        item {
                            EmptyStatePlaceholder("Hakuna maduka yaliyosajiliwa bado")
                        }
                    } else {
                        items(filteredShops) { shop ->
                            ShopCard(shop, onClick = { onViewShop(shop) })
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(text: String) {
    Box(Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(text, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
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
fun FurnitureCard(furniture: Furniture, onClick: () -> Unit) {
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
        }
    }
}
