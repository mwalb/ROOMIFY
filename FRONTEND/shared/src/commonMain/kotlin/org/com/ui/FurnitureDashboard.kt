package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
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
import org.com.ui.components.AIRoomArrangerDialog
import org.com.ui.components.roomifyAmbientBackground
import org.com.ui.theme.RoomifyColors

private val BrandNavy = Color(0xFF142170)
private val BrandIndigo = Color(0xFF283593)
private val BrandAmber = Color(0xFFFFB300)
private val BackgroundSurface = RoomifyColors.Background
private val BorderColor = Color(0xFFE2E8F0)

private data class CategoryVectorItem(val name: String, val icon: ImageVector)

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
        CategoryVectorItem("Zote", Icons.Rounded.AutoAwesome),
        CategoryVectorItem("Samani", Icons.Rounded.Chair),
        CategoryVectorItem("Mapambo", Icons.Rounded.CropOriginal),
        CategoryVectorItem("Maua na Mimea", Icons.Rounded.LocalFlorist),
        CategoryVectorItem("Taa na Mwanga", Icons.Rounded.Lightbulb),
        CategoryVectorItem("Vifaa vya Nyumbani", Icons.Rounded.Countertops),
        CategoryVectorItem("Huduma", Icons.Rounded.Handyman),
        CategoryVectorItem("Nyingine", Icons.Rounded.Category)
    )
    var selectedCategory by remember { mutableStateOf("Zote") }
    var searchQuery by remember { mutableStateOf("") }

    var itemToEdit by remember { mutableStateOf<Furniture?>(null) }
    var itemToDelete by remember { mutableStateOf<Furniture?>(null) }
    var showAIArrangerDialog by remember { mutableStateOf(false) }

    val uniqueShops = remember(shops) {
        shops.distinctBy {
            val cleanName = it.name.trim().lowercase()
            val cleanPhone = it.contactPhone?.trim()?.filter { c -> c.isDigit() } ?: ""
            if (cleanName.isNotBlank()) cleanName + "_" + cleanPhone else ("id_" + it.id)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().roomifyAmbientBackground()) {
        val isCompact = maxWidth < 600.dp
        val isMedium = maxWidth >= 600.dp && maxWidth < 840.dp
        val isExpanded = maxWidth >= 840.dp

        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    // Vibrant #142170 Navy Hero App Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(BrandNavy, BrandIndigo)))
                            .statusBarsPadding()
                            .padding(horizontal = if (isCompact) 12.dp else 24.dp, vertical = if (isCompact) 10.dp else 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 1200.dp)
                                .align(Alignment.Center),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Back button + ROOMIFY MARKET + Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                        .pointerHoverIcon(PointerIcon.Hand)
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
                                        Surface(
                                            color = BrandAmber,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "ROOMIFY MARKET",
                                                color = BrandNavy,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                letterSpacing = 0.8.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "Samani, Maua & Mapambo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isCompact) 16.sp else 18.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            // Right: Actions (AI Panga Chumba + Desktop Post Button)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .clickable { showAIArrangerDialog = true }
                                        .pointerHoverIcon(PointerIcon.Hand),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.AutoAwesome,
                                            contentDescription = "AI Room Visualizer",
                                            tint = BrandAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (isCompact) "AI Chumba" else "AI Panga Chumba",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    Button(
                                        onClick = {
                                            if (selectedTab == 0) onNavigate("post_furniture")
                                            else onNavigate("post_shop")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandAmber, contentColor = BrandNavy),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                    ) {
                                        Icon(Icons.Rounded.AddCircle, null, modifier = Modifier.size(16.dp), tint = BrandNavy)
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (selectedTab == 0) "+ Posta Bidhaa" else "+ Sajili Duka",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Centered Search & Filter Content Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (isExpanded) 24.dp else 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 1200.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        "Tafuta sofa, meza, vitanda, au eneo (Mikocheni, Sinza, Kariakoo...)",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8),
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(BrandNavy.copy(alpha = 0.08f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Search, null, tint = BrandNavy, modifier = Modifier.size(18.dp))
                                    }
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Rounded.Close, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = BrandNavy,
                                    unfocusedBorderColor = BorderColor
                                ),
                                singleLine = true
                            )

                            // Instant Place Suggestion Chips on typing
                            if (searchQuery.isNotBlank()) {
                                val placeSuggestions = remember(searchQuery) {
                                    PlaceSuggestionEngine.suggest(searchQuery).primary.take(5)
                                }
                                if (placeSuggestions.isNotEmpty()) {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        items(placeSuggestions) { suggestion ->
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { searchQuery = suggestion.name }
                                                    .pointerHoverIcon(PointerIcon.Hand),
                                                color = Color(0xFFEEF2FF),
                                                border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Rounded.LocationOn, null, tint = BrandNavy, modifier = Modifier.size(13.dp))
                                                    Text(
                                                        suggestion.name,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BrandNavy
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Segmented Tab Switcher (44.dp height)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val tab0Hover = remember { MutableInteractionSource() }
                                    val isTab0Hovered by tab0Hover.collectIsHoveredAsState()

                                    val tab1Hover = remember { MutableInteractionSource() }
                                    val isTab1Hovered by tab1Hover.collectIsHoveredAsState()

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clickable { selectedTab = 0 }
                                            .hoverable(tab0Hover)
                                            .pointerHoverIcon(PointerIcon.Hand),
                                        color = if (selectedTab == 0) BrandNavy else if (isTab0Hovered) Color(0xFFE2E8F0) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp),
                                        shadowElevation = if (selectedTab == 0) 2.dp else 0.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.Weekend,
                                                contentDescription = null,
                                                tint = if (selectedTab == 0) Color.White else Color(0xFF475569),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Bidhaa (${furnitures.size})",
                                                color = if (selectedTab == 0) Color.White else Color(0xFF475569),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clickable { selectedTab = 1 }
                                            .hoverable(tab1Hover)
                                            .pointerHoverIcon(PointerIcon.Hand),
                                        color = if (selectedTab == 1) BrandNavy else if (isTab1Hovered) Color(0xFFE2E8F0) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp),
                                        shadowElevation = if (selectedTab == 1) 2.dp else 0.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Rounded.Storefront,
                                                contentDescription = null,
                                                tint = if (selectedTab == 1) Color.White else Color(0xFF475569),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Maduka & Mafundi (${uniqueShops.size})",
                                                color = if (selectedTab == 1) Color.White else Color(0xFF475569),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                // Show floating CTA on Mobile/Medium, hide on Desktop where header button is active
                if (!isExpanded) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (selectedTab == 0) onNavigate("post_furniture")
                            else onNavigate("post_shop")
                        },
                        icon = { Icon(Icons.Rounded.AddCircle, null, tint = Color.White) },
                        text = {
                            Text(
                                if (selectedTab == 0) (if (isCompact) "Posta Bidhaa" else "Posta Bidhaa Yako")
                                else (if (isCompact) "Sajili Duka" else "Sajili Duka Lako"),
                                fontWeight = FontWeight.Bold,
                                softWrap = false
                            )
                        },
                        containerColor = BrandNavy,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                    )
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
                        .widthIn(max = 1200.dp)
                ) {
                    if (selectedTab == 0) {
                        // Category Filter Chips (LazyRow, height = 40.dp)
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (isExpanded) 24.dp else 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(categories) { item ->
                                val isSelected = selectedCategory == item.name
                                val interactionSource = remember { MutableInteractionSource() }
                                val isHovered by interactionSource.collectIsHoveredAsState()

                                Surface(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .clickable { selectedCategory = item.name }
                                        .hoverable(interactionSource)
                                        .pointerHoverIcon(PointerIcon.Hand),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BrandNavy else if (isHovered) Color(0xFFF1F5F9) else Color.White,
                                    border = BorderStroke(1.5.dp, if (isSelected) BrandNavy else BorderColor),
                                    shadowElevation = if (isSelected) 3.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Checkbox indicator box
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isSelected) Color(0xFF10B981) else Color.Transparent)
                                                .then(
                                                    if (!isSelected) Modifier.border(1.5.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                                                    else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }

                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) BrandAmber else BrandNavy,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            item.name,
                                            fontSize = 12.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Filter Logic
                        val filtered = furnitures.filter { item ->
                            val matchesCategory = selectedCategory == "Zote" ||
                                    item.category.contains(selectedCategory, ignoreCase = true) ||
                                    (selectedCategory == "Samani" && listOf("Bed", "Sofa", "Table", "Chair", "Furniture").any { item.category.contains(it, ignoreCase = true) }) ||
                                    (selectedCategory == "Mapambo" && listOf("Decor", "Picha", "Vioo").any { item.category.contains(it, ignoreCase = true) }) ||
                                    (selectedCategory == "Maua na Mimea" && listOf("Maua", "Mimea", "Flower", "Plant").any { item.category.contains(it, ignoreCase = true) }) ||
                                    (selectedCategory == "Taa na Mwanga" && listOf("Taa", "Mwanga", "Light").any { item.category.contains(it, ignoreCase = true) }) ||
                                    (selectedCategory == "Vifaa vya Nyumbani" && listOf("Vifaa", "Jikoni", "Kitchen", "Appliance").any { item.category.contains(it, ignoreCase = true) })

                            val matchesSearch = searchQuery.isBlank() ||
                                    item.title.contains(searchQuery, ignoreCase = true) ||
                                    item.description.contains(searchQuery, ignoreCase = true) ||
                                    item.category.contains(searchQuery, ignoreCase = true) ||
                                    item.displayLocation.contains(searchQuery, ignoreCase = true) ||
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
                                        "Kuwa wa kwanza kuposti samani, mapambo, au vifaa vya nyumbani ili uwafikie wapangaji na wateja wengi!",
                                    actionText = "+ Posta Bidhaa Sasa",
                                    onAction = { onNavigate("post_furniture") },
                                    isExpanded = isExpanded
                                )
                            } else {
                                val gridColumns = when {
                                    isExpanded -> 3
                                    isMedium -> 2
                                    else -> 1
                                }
                                val spacing = if (isCompact) 12.dp else 16.dp

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(gridColumns),
                                    contentPadding = PaddingValues(
                                        horizontal = if (isExpanded) 24.dp else 16.dp,
                                        vertical = 12.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(spacing),
                                    verticalArrangement = Arrangement.spacedBy(spacing),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filtered, key = { it.id ?: it.title }) { item ->
                                        FurnitureCard(
                                            furniture = item,
                                            currentUser = currentUser,
                                            onClick = { onViewDetail(item) },
                                            onEdit = { itemToEdit = item },
                                            onDelete = { itemToDelete = item },
                                            onToggleStatus = { onToggleStatus?.invoke(item) }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Shops & Mafundi List
                        val filteredShops = uniqueShops.filter { shop ->
                            searchQuery.isBlank() ||
                                    shop.name.contains(searchQuery, ignoreCase = true) ||
                                    (shop.description?.contains(searchQuery, ignoreCase = true) ?: false) ||
                                    shop.displayLocation.contains(searchQuery, ignoreCase = true) ||
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
                                    actionText = "+ Sajili Duka Lako Sasa",
                                    onAction = { onNavigate("post_shop") },
                                    isExpanded = isExpanded
                                )
                            } else {
                                val shopColumns = if (isCompact) 1 else 2
                                val spacing = if (isCompact) 12.dp else 16.dp

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(shopColumns),
                                    contentPadding = PaddingValues(
                                        horizontal = if (isExpanded) 24.dp else 16.dp,
                                        vertical = 12.dp
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(spacing),
                                    verticalArrangement = Arrangement.spacedBy(spacing),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredShops, key = { it.id ?: it.name }) { shop ->
                                        ShopCard(shop, onClick = { onViewShop(shop) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DELETE CONFIRMATION DIALOG
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            icon = { Icon(Icons.Rounded.DeleteForever, null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp)) },
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

    // EDIT FURNITURE DIALOG
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
                                label = { Text("INAPATIKANA", softWrap = false) }
                            )
                            FilterChip(
                                selected = editStatus == "SOLD",
                                onClick = { editStatus = "SOLD" },
                                label = { Text("IMEUZWA", softWrap = false) }
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

    // AI ROOM ARRANGER DIALOG
    if (showAIArrangerDialog) {
        AIRoomArrangerDialog(
            onDismiss = { showAIArrangerDialog = false }
        )
    }
}

@Composable
private fun EmptyStatePlaceholder(
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    isExpanded: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderColor),
            shadowElevation = 2.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                // 56.dp circular icon container (#EEF2FF, #142170 icon)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFEEF2FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Weekend,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = BrandNavy
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
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

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(Icons.Rounded.AddCircle, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        actionText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun ShopCard(shop: Shop, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) Color(0xFFF8FAFC) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 4.dp else 1.dp),
        border = BorderStroke(1.dp, if (isHovered) BrandNavy.copy(alpha = 0.3f) else BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Avatar / Shop Image (48.dp, RoundedCornerShape(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                if (shop.firstImageUrl != null) {
                    KamelImage(
                        resource = { asyncPainterResource(shop.firstImageUrl!!) },
                        contentDescription = shop.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Rounded.Storefront, null, tint = BrandNavy, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    shop.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        shop.displayLocation,
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Eneo Lililohifadhiwa",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (shop.hasVideo) {
                        Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.PlayCircle, null, tint = Color.Red, modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(3.dp))
                                Text("VIDEO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = BrandNavy, modifier = Modifier.size(18.dp))
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

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) Color(0xFFF8FAFC) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 4.dp else 2.dp),
        border = BorderStroke(1.dp, if (isHovered) BrandNavy.copy(alpha = 0.3f) else BorderColor)
    ) {
        Column {
            Box {
                KamelImage(
                    resource = { asyncPainterResource(furniture.firstImageUrl ?: "") },
                    contentDescription = furniture.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = BrandNavy)
                        }
                    },
                    onFailure = {
                        Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Rounded.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                Text("Picha haipatikani", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                )

                if (furniture.category.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        color = BrandNavy.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            furniture.category.uppercase(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        furniture.condition.ifBlank { "MPYA" },
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Column(Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        furniture.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        furniture.formattedPrice,
                        color = BrandNavy,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        furniture.displayLocation,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(26.dp).background(Color(0xFFEEF2FF), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Person, null, modifier = Modifier.size(14.dp), tint = BrandNavy)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        furniture.ownerName ?: "Muuzaji",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(color = Color(0xFFEEF2FF), shape = CircleShape) {
                        Text(
                            "Angalia",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = BrandNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            softWrap = false
                        )
                    }
                }
            }

            // MANAGEMENT BAR FOR OWNER/ADMIN
            if (isOwner) {
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = if (furniture.status == "SOLD") Color(0xFFFFEBEE) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onToggleStatus?.invoke() }
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (furniture.status == "SOLD") Icons.Rounded.Cancel else Icons.Rounded.CheckCircle,
                                null,
                                tint = if (furniture.status == "SOLD") Color.Red else Color(0xFF15803D),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (furniture.status == "SOLD") "IMEUZWA" else "INAPATIKANA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (furniture.status == "SOLD") Color.Red else Color(0xFF15803D),
                                softWrap = false
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { onEdit?.invoke() }) {
                            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(14.dp), tint = BrandNavy)
                            Spacer(Modifier.width(4.dp))
                            Text("Hariri", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandNavy, softWrap = false)
                        }
                        TextButton(onClick = { onDelete?.invoke() }) {
                            Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(14.dp), tint = Color(0xFFD32F2F))
                            Spacer(Modifier.width(4.dp))
                            Text("Futa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
