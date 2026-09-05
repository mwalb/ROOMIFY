package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.com.i18n.LocalRoomifyStrings
import org.com.model.Room
import org.com.model.User

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)
private val SuccessColor = Color(0xFF4CAF50)

@Composable
fun PropertyDetailsScreen(
    room: Room,
    currentUser: User? = null,
    otherProperties: List<Room> = emptyList(),
    onBack: () -> Unit,
    onBookNow: (Room) -> Unit,
    onMessageOwner: (Room) -> Unit,
    onEditProperty: (Room) -> Unit = {},
    onDeleteProperty: (Room) -> Unit = {},
    onViewProperty: (Room) -> Unit = {}
) {
    val strings = LocalRoomifyStrings.current
    val uriHandler = LocalUriHandler.current
    var isSaved by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    val isMyProperty = currentUser?.id == room.postedBy

    // Animations for entry
    val alphaAnim = remember { Animatable(0f) }
    val translateYAnim = remember { Animatable(50f) }

    LaunchedEffect(room.id) {
        alphaAnim.animateTo(1f, animationSpec = tween(600))
        translateYAnim.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        // Main Centered Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 500.dp)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp)
                .graphicsLayer {
                    alpha = alphaAnim.value
                    translationY = translateYAnim.value
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Image Gallery
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    val imageUrls = room.fullImageUrls
                    if (imageUrls.isNotEmpty()) {
                        val lazyListState = rememberLazyListState()
                        val currentIndex = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

                        LazyRow(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            flingBehavior = rememberSnapFlingBehavior(lazyListState)
                        ) {
                            items(imageUrls) { imageUrl ->
                                PropertyImage(
                                    url = imageUrl,
                                    modifier = Modifier.fillParentMaxSize().clickable { selectedImageUrl = imageUrl }
                                )
                            }
                        }

                        // Image Counter (Bottom Right)
                        if (imageUrls.size > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${currentIndex.value + 1} / ${imageUrls.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Page Indicator (Bottom Center)
                        if (imageUrls.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                imageUrls.forEachIndexed { index, _ ->
                                    val isSelected = currentIndex.value == index
                                    Box(
                                        modifier = Modifier
                                            .size(if (isSelected) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty state placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Image,
                                    null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("No images available", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    // Top layer actions (Back/Save)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { /* Share functionality could go here */ }, modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                                Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            if (!isMyProperty) {
                                IconButton(onClick = { isSaved = !isSaved }, modifier = Modifier.size(36.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                                    Icon(if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null, tint = if (isSaved) Color(0xFFFFC107) else Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title and Basic Info
                    Column {
                        Surface(color = PrimaryColor.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                            Text(room.propertyType?.uppercase() ?: "PROPERTY", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryColor)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(text = room.title ?: "", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1A1A), lineHeight = 28.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            @Suppress("DEPRECATION")
                            Text(text = room.address ?: "", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    // Feature Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp)).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SmallFeature(Icons.Default.Bed, "${room.roomsCount}", strings.beds)
                        SmallFeature(Icons.Default.Bathtub, "${room.bathroomsCount}", strings.baths)
                        SmallFeature(Icons.Default.SquareFoot, "${room.area.toInt()}m²", strings.area)
                    }

                    // Section: Description
                    SectionTitleDetails(strings.description)
                    @Suppress("DEPRECATION")
                    Text(text = room.description ?: "", fontSize = 14.sp, color = Color.DarkGray, lineHeight = 22.sp)

                    // Section: Amenities
                    if (room.amenities.isNotEmpty()) {
                        SectionTitleDetails(strings.amenities)
                        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                            room.amenities.forEach { amenity ->
                                Row(Modifier.background(Color(0xFFF0F2F5), RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, null, tint = SuccessColor, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(amenity, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Section: Video / Virtual Tour
                    if (room.hasVideo && !room.videoUrl.isNullOrBlank()) {
                        val fullVideoUrl = room.getFullUrl(room.videoUrl!!)
                        SectionTitleDetails("Virtual Tour")
                        Card(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                                // Simulate video overlay
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(
                                        onClick = { uriHandler.openUri(fullVideoUrl) },
                                        modifier = Modifier.size(72.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                                    }
                                    Spacer(Modifier.height(16.dp))
                                    Text("Watch Virtual Tour", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Experience the property in HD", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                }
                                
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                    color = Color.Red,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("VIDEO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }

                    // Section: Legal Documents
                    if (room.hasContract && !room.contractUrl.isNullOrBlank()) {
                        val fullContractUrl = room.getFullUrl(room.contractUrl!!)
                        SectionTitleDetails("Documents & Legal")
                        DocumentCard(
                            name = "Rental Agreement & Terms.pdf",
                            description = "Official contract for this property",
                            onView = { uriHandler.openUri(fullContractUrl) },
                            onDownload = { uriHandler.openUri(fullContractUrl) }
                        )
                    }

                    SectionTitleDetails("Location")
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color(0xFFF8F9FA), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=${room.latitude},${room.longitude}") },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                            ) {
                                Icon(Icons.Default.Navigation, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Show Route", fontSize = 12.sp)
                            }
                        }
                    }

                    // Section: Contact / Owner Actions
                    if (!isMyProperty) {
                        SectionTitleDetails("Contact Owner")
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(44.dp).background(Color(0xFFF0F2F5), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = PrimaryColor, modifier = Modifier.size(24.dp)) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(room.ownerName ?: "Owner", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("Owner", fontSize = 12.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { onMessageOwner(room) }, modifier = Modifier.background(Color(0xFF2196F3).copy(alpha = 0.1f), CircleShape)) {
                                    Icon(Icons.AutoMirrored.Filled.Message, null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { uriHandler.openUri("tel:${room.contactPhone}") }, modifier = Modifier.background(SuccessColor.copy(alpha = 0.1f), CircleShape)) {
                                    Icon(Icons.Default.Call, null, tint = SuccessColor, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    } else {
                        SectionTitleDetails("Listing Management")
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryColor.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f))
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("You are the owner of this property. Use the controls below to manage your listing.", fontSize = 12.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OwnerActionItem(Icons.Default.Edit, "Edit Info", Modifier.weight(1f)) { onEditProperty(room) }
                                    OwnerActionItem(Icons.Default.PhotoLibrary, "Manage Photos", Modifier.weight(1f)) { onEditProperty(room) }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OwnerActionItem(Icons.Default.PlayCircle, "Update Video", Modifier.weight(1f)) { onEditProperty(room) }
                                    OwnerActionItem(Icons.Default.Description, "Update Contract", Modifier.weight(1f)) { onEditProperty(room) }
                                }
                                HorizontalDivider(color = PrimaryColor.copy(alpha = 0.1f))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    @Suppress("DEPRECATION")
                                    Text("Status", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
                                    Surface(color = SuccessColor, shape = CircleShape) {
                                        Text(room.status, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                                    }
                                }
                                var showDeleteDialog by remember { mutableStateOf(false) }
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    @Suppress("DEPRECATION")
                                    Text("Delete Property", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (showDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteDialog = false },
                                        title = { Text("Delete Property?") },
                                        text = { Text("This action cannot be undone. Are you sure you want to remove this listing?") },
                                        confirmButton = {
                                            TextButton(onClick = { onDeleteProperty(room); showDeleteDialog = false }) {
                                                Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteDialog = false }) {
                                                Text("CANCEL")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Section: Other Properties by this Owner
                    if (otherProperties.isNotEmpty()) {
                        SectionTitleDetails("More by ${room.ownerName ?: "Owner"}")
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(otherProperties) { otherRoom ->
                                RelatedPropertyCard(otherRoom) { onViewProperty(otherRoom) }
                            }
                        }
                    }
                }

                // Bottom Action Bar
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(strings.monthlyRent, fontSize = 11.sp, color = Color.Gray)
                        Text(room.formattedPrice, fontSize = 18.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
                    }
                    
                    if (isMyProperty) {
                        Button(
                            onClick = { onEditProperty(room) },
                            modifier = Modifier.height(48.dp).widthIn(min = 120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryLight)
                        ) {
                            Text("EDIT PROPERTY", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    } else {
                        Button(
                            onClick = { onBookNow(room) },
                            modifier = Modifier.height(48.dp).widthIn(min = 120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                        ) {
                            Text("BOOK NOW", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Full Screen Image Viewer
    if (selectedImageUrl != null) {
        Dialog(
            onDismissRequest = { selectedImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                PropertyImage(
                    url = selectedImageUrl!!,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight().clickable { selectedImageUrl = null }
                )
                
                IconButton(
                    onClick = { selectedImageUrl = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(name: String, description: String, onView: () -> Unit, onDownload: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(description, fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onView,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View", fontSize = 12.sp)
                }
                
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryColor)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Download", fontSize = 12.sp, color = PrimaryColor)
                }
            }
        }
    }
}

@Composable
private fun RelatedPropertyCard(room: Room, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column {
            KamelImage(
                resource = { asyncPainterResource(room.firstImageUrl ?: "") },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop,
                onLoading = { _: Float ->
                    Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)))
                }
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = room.title ?: "Property",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = room.formattedPrice,
                    fontSize = 11.sp,
                    color = PrimaryColor,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun OwnerActionItem(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(44.dp).clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.1f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryColor)
        }
    }
}

@Composable
private fun SmallFeature(icon: ImageVector, value: String, label: String) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(tween(800))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun SectionTitleDetails(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
}

@Composable
private fun PropertyImage(url: String, modifier: Modifier = Modifier) {
    KamelImage(
        resource = { asyncPainterResource(url) },
        contentDescription = "Property Image",
        modifier = modifier,
        contentScale = ContentScale.Crop,
        onLoading = { progress: Float ->
            Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        },
        onFailure = { throwable: Throwable ->
            Box(Modifier.fillMaxSize().background(Color(0xFFF0F2F5)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(4.dp))
                    @Suppress("DEPRECATION")
                    Text("Image failed to load", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(mainAxisSpacing: Dp, crossAxisSpacing: Dp, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing), verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)) { content() }
}
