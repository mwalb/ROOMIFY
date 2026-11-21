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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.platform.LocalUriHandler
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

private data class StyleMeta(val icon: androidx.compose.ui.graphics.vector.ImageVector, val description: String)

private val styleMeta: Map<String, StyleMeta> = mapOf(
    "Modern" to StyleMeta(Icons.Default.Weekend, "Clean lines, neutral tones, sleek pieces"),
    "Minimalist" to StyleMeta(Icons.Default.CropSquare, "Only the essentials, lots of open space"),
    "Student" to StyleMeta(Icons.Default.School, "Compact, practical, study-friendly"),
    "Luxury" to StyleMeta(Icons.Default.Diamond, "Rich textures, statement furniture"),
    "Traditional" to StyleMeta(Icons.Default.HistoryEdu, "Classic wood and timeless warmth"),
    "Scandinavian" to StyleMeta(Icons.Default.AcUnit, "Bright, airy, light wood"),
    "Industrial" to StyleMeta(Icons.Default.Factory, "Raw metal, dark tones, loft feel"),
    "Cozy" to StyleMeta(Icons.Default.LocalFireDepartment, "Warm, soft, inviting")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRoomArrangerDialog(
    room: Room? = null,
    initialFurnitureName: String? = null,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(if (room == null || room.images.isEmpty()) 2 else 1) }
    var selectedImageUrl by remember { mutableStateOf(room?.images?.firstOrNull() ?: "") }
    var selectedFurniture by remember { 
        mutableStateOf(if (initialFurnitureName != null) setOf(initialFurnitureName) else setOf<String>()) 
    }
    var selectedStyle by remember { mutableStateOf(styleOptions.first()) }

    val roomArea = room?.area ?: 0.0
    val initialDim = if(roomArea > 0) (kotlin.math.sqrt(roomArea) * 10).toInt() / 10.0 else 0.0
    var roomWidth by remember { mutableStateOf(if(initialDim > 0) initialDim.toString() else "4.0") }
    var roomLength by remember { mutableStateOf(if(initialDim > 0) initialDim.toString() else "4.5") }
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
                        selectedStyle = selectedStyle,
                        selectedFurniture = selectedFurniture.toList(),
                        onBack = { generationResult = null; currentStep = 5 },
                        onClose = onDismiss
                    )
                } else {
                    Column(
                        modifier = Modifier.weight(1f).padding(24.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        when (currentStep) {
                            1 -> StepSelectImage(room?.images ?: emptyList(), selectedImageUrl) { selectedImageUrl = it }
                            2 -> StepSelectFurniture(selectedFurniture) {
                                selectedFurniture = if (it in selectedFurniture) selectedFurniture - it else selectedFurniture + it
                            }
                            3 -> StepSelectStyle(selectedStyle) { selectedStyle = it }
                            4 -> StepDimensions(roomWidth, roomLength, ceilingHeight, {roomWidth = it}, {roomLength = it}, {ceilingHeight = it})
                            5 -> StepInstructions(instructions) { instructions = it }
                        }
                        
                        if (errorMessage != null) {
                            Surface(
                                color = Color(0xFFFEF2F2),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Couldn't generate room preview",
                                            color = Color(0xFF991B1B),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            cleanAiError(errorMessage!!),
                                            color = Color(0xFFB91C1C),
                                            fontSize = 12.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { errorMessage = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color(0xFF991B1B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
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
                                    errorMessage = null
                                    scope.launch {
                                        try {
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
                                            if (response.success && response.data != null) {
                                                generationResult = response.data
                                            } else {
                                                errorMessage = response.message.ifBlank { "AI generation failed. Please try again." }
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Connection error while contacting AI service."
                                        } finally {
                                            isGenerating = false
                                        }
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
                            } && !isGenerating
                        ) {
                            if (isGenerating) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("DESIGNING YOUR ROOM…", fontWeight = FontWeight.Bold)
                                }
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
            val meta = styleMeta[style]
            val isSelected = selected == style
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelect(style) },
                color = if (isSelected) PrimaryColor.copy(alpha = 0.06f) else Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) PrimaryColor else Color(0xFFE5E7EB))
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MagicGradient else Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            meta?.icon ?: Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else PrimaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(style, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (meta != null) {
                            Text(meta.description, color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = PrimaryColor)
                    }
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
private fun ResultScreen(
    originalUrl: String,
    result: AIRoomResponse,
    selectedStyle: String,
    selectedFurniture: List<String>,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var savedToast by remember { mutableStateOf(false) }

    val effectiveAfterUrl = remember(result.generatedImageUrl, selectedStyle, selectedFurniture) {
        if (!result.generatedImageUrl.isNullOrBlank()) {
            ApiClient.resolveUrl(result.generatedImageUrl)
        } else {
            buildClientFallbackRenderUrl(selectedStyle, selectedFurniture)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("AI Room Visualization", fontSize = 22.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
        Spacer(Modifier.height(16.dp))

        if (originalUrl.isNotBlank()) {
            BeforeAfterSlider(
                beforeUrl = originalUrl,
                afterUrl = effectiveAfterUrl,
                modifier = Modifier.fillMaxWidth().height(340.dp)
            )
            Text(
                "Drag the handle left or right to compare Before & After",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF0F2F5))
            ) {
                RoomifyImage(
                    url = effectiveAfterUrl,
                    contentDescription = "AI Visualization",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Surface(
            color = PrimaryColor.copy(alpha = 0.05f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.14f))
        ) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("AI Interior Design Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryColor)
                }
                Spacer(Modifier.height(12.dp))
                FormattedAiRecommendation(
                    raw = result.recommendation ?: "This layout optimizes space while maintaining the room's character."
                )
            }
        }

        if (savedToast) {
            Spacer(Modifier.height(12.dp))
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Visualization opened in a new tab so you can save or share it.",
                        color = Color(0xFF166534),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    savedToast = true
                    try {
                        uriHandler.openUri(effectiveAfterUrl)
                    } catch (_: Exception) {}
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("SAVE IMAGE", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("REGENERATE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Renders Gemini's Markdown recommendation (`### Headings`, `1. **Title:** details`, `- bullets`)
 * as clean styled sections instead of raw `###` and `**` characters.
 */
@Composable
private fun FormattedAiRecommendation(raw: String) {
    val lines = remember(raw) { raw.lines().map { it.trim() }.filter { it.isNotEmpty() } }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("#") -> {
                    val headingText = line.trimStart('#').trim().replace("**", "")
                    Text(
                        text = headingText,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = PrimaryColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                line.firstOrNull()?.isDigit() == true && line.contains(".") -> {
                    val numPart = line.substringBefore(".").trim()
                    val bodyPart = line.substringAfter(".").trim()
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(numPart, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = parseBoldInline(bodyPart),
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    val bodyPart = line.drop(2).trim()
                    Row(verticalAlignment = Alignment.Top) {
                        Text("•", color = PrimaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = parseBoldInline(bodyPart),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
                else -> {
                    Text(
                        text = parseBoldInline(line),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

private fun parseBoldInline(text: String) = buildAnnotatedString {
    val parts = text.split("**")
    parts.forEachIndexed { idx, segment ->
        if (idx % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))) {
                append(segment)
            }
        } else {
            append(segment)
        }
    }
}

private fun buildClientFallbackRenderUrl(style: String, furniture: List<String>): String {
    val items = if (furniture.isNotEmpty()) furniture.joinToString(", ") else "double bed, wardrobe, sofa, curtains"
    val cleanPrompt = "Photorealistic interior design photograph of a $style bedroom furnished with $items, warm lighting, 4k"
        .replace(" ", "%20")
        .replace(",", "%2C")
    val seed = kotlin.math.abs((style + items).hashCode()) % 99999
    return "https://image.pollinations.ai/prompt/$cleanPrompt?width=1024&height=768&nologo=true&seed=$seed"
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

/** Strips any raw JSON dump if an older backend returns one, keeping a readable one-liner. */
private fun cleanAiError(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.contains("\"status\": \"PERMISSION_DENIED\"") || trimmed.contains("ACCESS_DENIED")) {
        return "Gemini API access is denied for the server's API key (HTTP 403). Enable the Generative Language API in Google Cloud / AI Studio or replace GEMINI_API_KEY."
    }
    val brace = trimmed.indexOf('{')
    val head = if (brace > 0) trimmed.substring(0, brace).trim().trimEnd(':') else trimmed
    return head.removePrefix("AI Generation failed:").trim().ifBlank {
        "AI service is temporarily unavailable. Please try again."
    }
}

