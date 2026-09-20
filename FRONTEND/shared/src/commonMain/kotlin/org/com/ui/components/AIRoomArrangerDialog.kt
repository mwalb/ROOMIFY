package org.com.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.com.model.AIRoomRequest
import org.com.model.AIRoomResponse
import org.com.model.Room
import org.com.network.ApiClient
import org.com.network.RoomifyApi

private val PrimaryColor = Color(0xFF1A237E)
private val AccentColor = Color(0xFF03DAC5)
private val MagicGradient = Brush.linearGradient(listOf(PrimaryColor, Color(0xFF6200EE)))

val furnitureOptions = listOf(
    "Bed", "Single Bed", "Double Bed", "Bunk Bed", "Wardrobe", "Study Desk",
    "Office Desk", "Chair", "Sofa", "Coffee Table", "TV", "TV Stand",
    "Bookshelf", "Dining Table", "Dining Chairs", "Bedside Table",
    "Dresser", "Mirror", "Shoe Rack", "Curtains", "Plants"
)

val styleOptions = listOf(
    "Modern", "Minimalist", "Student", "Luxury", "Traditional",
    "Scandinavian", "Industrial", "Cozy"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRoomArrangerDialog(
    room: Room,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var selectedImageUrl by remember { mutableStateOf(room.images.firstOrNull() ?: "") }
    var selectedFurniture by remember { mutableStateOf(setOf<String>()) }
    var selectedStyle by remember { mutableStateOf(styleOptions.first()) }

    val initialDim = if(room.area > 0) (kotlin.math.sqrt(room.area) * 10).toInt() / 10.0 else 0.0
    var roomWidth by remember { mutableStateOf(if(initialDim > 0) initialDim.toString() else "") }
    var roomLength by remember { mutableStateOf(if(initialDim > 0) initialDim.toString() else "") }
    var ceilingHeight by remember { mutableStateOf("2.8") }
    var instructions by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var generationResult by remember { mutableStateOf<AIRoomResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier.fillMaxWidth().background(MagicGradient).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
                        }
                        Text(
                            "AI Room Arranger",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f)
                        )
                        if (generationResult == null) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ) {
                                Text(
                                    "$currentStep/5",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (generationResult != null) {
                    ResultScreen(
                        originalUrl = ApiClient.resolveUrl(selectedImageUrl),
                        result = generationResult!!,
                        onBack = { generationResult = null; currentStep = 5 },
                        onClose = onDismiss
                    )
                } else {
                    Column(
                        modifier = Modifier.weight(1f).padding(24.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        when (currentStep) {
                            1 -> StepSelectImage(room.images, selectedImageUrl) { selectedImageUrl = it }
                            2 -> StepSelectFurniture(selectedFurniture) {
                                selectedFurniture = if (it in selectedFurniture) selectedFurniture - it else selectedFurniture + it
                            }
                            3 -> StepSelectStyle(selectedStyle) { selectedStyle = it }
                            4 -> StepDimensions(roomWidth, roomLength, ceilingHeight, {roomWidth = it}, {roomLength = it}, {ceilingHeight = it})
                            5 -> StepInstructions(instructions) { instructions = it }
                        }
                        
                        if (errorMessage != null) {
                            Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(12.dp)) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, null, tint = Color.Red)
                                    Spacer(Modifier.width(12.dp))
                                    Text(errorMessage!!, color = Color.Red, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Footer Actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("BACK")
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStep < 5) {
                                    currentStep++
                                } else {
                                    isGenerating = true
                                    scope.launch {
                                        val request = AIRoomRequest(
                                            sourceImageUrl = selectedImageUrl,
                                            furniture = selectedFurniture.toList(),
                                            style = selectedStyle,
                                            roomWidth = roomWidth.toDoubleOrNull(),
                                            roomLength = roomLength.toDoubleOrNull(),
                                            ceilingHeight = ceilingHeight.toDoubleOrNull(),
                                            additionalInstructions = instructions
                                        )
                                        val response = RoomifyApi.visualizeRoom(request)
                                        if (response.success) {
                                            generationResult = response.data
                                        } else {
                                            errorMessage = response.message
                                        }
                                        isGenerating = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(if(currentStep > 1) 2f else 1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                            enabled = when(currentStep) {
                                1 -> selectedImageUrl.isNotBlank()
                                2 -> selectedFurniture.isNotEmpty()
                                else -> true
                            }
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(if (currentStep == 5) "✨ GENERATE AI ROOM" else "NEXT STEP")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSelectImage(images: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text("Step 1: Select Room Image", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Choose the photo you want to furnish with AI", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(images) { imageUrl ->
                val fullUrl = ApiClient.resolveUrl(imageUrl)
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (selected == imageUrl) 3.dp else 1.dp,
                            color = if (selected == imageUrl) PrimaryColor else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelect(imageUrl) }
                ) {
                    KamelImage(
                        resource = { asyncPainterResource(fullUrl) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (selected == imageUrl) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                            color = PrimaryColor,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp).padding(2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepSelectFurniture(selected: Set<String>, onToggle: (String) -> Unit) {
    Column {
        Text("Step 2: Select Furniture", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("What items would you like to see in this room?", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            furnitureOptions.forEach { item ->
                FilterChip(
                    selected = item in selected,
                    onClick = { onToggle(item) },
                    label = { Text(item) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryColor.copy(alpha = 0.1f),
                        selectedLabelColor = PrimaryColor
                    )
                )
            }
        }
    }
}

@Composable
private fun StepSelectStyle(selected: String, onSelect: (String) -> Unit) {
    Column {
        Text("Step 3: Select Interior Style", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Choose an aesthetic for your furniture", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        styleOptions.forEach { style ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelect(style) },
                color = if (selected == style) PrimaryColor.copy(alpha = 0.05f) else Color.White,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (selected == style) PrimaryColor else Color(0xFFEEEEEE))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == style, onClick = { onSelect(style) })
                    Spacer(Modifier.width(12.dp))
                    Text(style, fontWeight = if (selected == style) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun StepDimensions(w: String, l: String, h: String, onW: (String) -> Unit, onL: (String) -> Unit, onH: (String) -> Unit) {
    Column {
        Text("Step 4: Room Dimensions (Optional)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Accurate dimensions help AI place furniture realistically", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = w, onValueChange = onW, label = { Text("Width (m)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = l, onValueChange = onL, label = { Text("Length (m)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = h, onValueChange = onH, label = { Text("Ceiling Height (m)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    }
}

@Composable
private fun StepInstructions(value: String, onChange: (String) -> Unit) {
    Column {
        Text("Step 5: Additional Instructions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Any specific layout requests or preferences?", color = Color.Gray, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text("e.g. Keep the window clear and put the study desk close to natural light.") },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun ResultScreen(originalUrl: String, result: AIRoomResponse, onBack: () -> Unit, onClose: () -> Unit) {
    var showAfter by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("AI Room Visualization", fontSize = 22.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF0F2F5))
        ) {
            KamelImage(
                resource = { asyncPainterResource(if(showAfter && result.generatedImageUrl != null) result.generatedImageUrl else originalUrl) },
                contentDescription = "AI Visualization",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onLoading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryColor)
                    }
                },
                onFailure = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Text("Image failed to render", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            )
            
            if (result.generatedImageUrl == null && showAfter) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                    Text("AI generated a description but no image was returned.", color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                }
            }

            // Switch Toggle
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(50.dp)
            ) {
                Row(Modifier.padding(4.dp)) {
                    Button(
                        onClick = { showAfter = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if(!showAfter) Color.White else Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("BEFORE", color = if(!showAfter) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showAfter = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if(showAfter) Color.White else Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(50.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("AFTER", color = if(showAfter) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Surface(color = PrimaryColor.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("AI Recommendation", fontWeight = FontWeight.Bold, color = PrimaryColor)
                }
                Spacer(Modifier.height(8.dp))
                Text(result.recommendation ?: "This layout optimizes space while maintaining the room's character.", fontSize = 14.sp, lineHeight = 20.sp)
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { /* Save to gallery or favorites */ },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("REGENERATE")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
