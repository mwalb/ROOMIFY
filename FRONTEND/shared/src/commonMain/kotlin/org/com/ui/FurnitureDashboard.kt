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
    val categories = listOf("All", "Bed", "Sofa", "Table", "Electronics", "Decor")
    var selectedCategory by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            Column(Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { Text("Furniture Hub", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            if (selectedTab == 0) onNavigate("post_furniture")
                            else onNavigate("post_shop")
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
                
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Color(0xFF6200EE)) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Products", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Shops", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (selectedTab == 0) {
                // Category Selector
                LazyRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Furniture List
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val filtered = if (selectedCategory == "All") furnitures
                                   else furnitures.filter { it.category.equals(selectedCategory, ignoreCase = true) }

                    if (filtered.isEmpty()) {
                        item {
                            EmptyStatePlaceholder("Hakuna bidhaa zilizopatikana")
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
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (shops.isEmpty()) {
                        item {
                            EmptyStatePlaceholder("Hakuna maduka yaliyosajiliwa bado")
                        }
                    } else {
                        items(shops) { shop ->
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
    Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(text, color = Color.Gray)
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
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
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
                Text(shop.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(shop.address ?: "Mtaa haujatajwa", color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
                Spacer(Modifier.height(4.dp))
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
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop,
                    onLoading = { 
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF6200EE))
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                Text("Image Unavailable", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                )

                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        furniture.condition,
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
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        furniture.formattedPrice,
                        color = Color(0xFF6200EE),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(furniture.address ?: "Location unknown", color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).background(Color(0xFFF0F2F5), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(furniture.ownerName ?: "Seller", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                        Text("Miliki sasa", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
