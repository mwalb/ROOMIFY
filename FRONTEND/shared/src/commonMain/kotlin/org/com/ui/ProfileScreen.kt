package org.com.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.com.i18n.Language
import org.com.i18n.LocalLocalizationManager
import org.com.i18n.LocalRoomifyStrings
import org.com.model.*
import org.com.network.ApiClient
import org.com.network.RoomifyApi

private val PrimaryColor = Color(0xFF1A237E)
private val PrimaryLight = Color(0xFF3949AB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onUpdateUser: (User) -> Unit = {}
) {
    val strings = LocalRoomifyStrings.current
    val localizationManager = LocalLocalizationManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    
    // Entry animations
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(500))
        scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
    }

    val imagePicker = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files ->
            files.firstOrNull()?.let { file ->
                scope.launch {
                    isUploading = true
                    try {
                        val bytes = file.readByteArray(context)
                        val response = RoomifyApi.uploadProfileImage(bytes)
                        if (response.success && response.data != null) {
                            // Update user with new image URL
                            onUpdateUser(user.copy(profileImage = response.data))
                        }
                    } catch (e: Exception) {
                        println("Profile: Upload failed: ${e.message}")
                    } finally {
                        isUploading = false
                    }
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PrimaryColor, PrimaryLight))),
        contentAlignment = Alignment.Center
    ) {
        // Main Centered Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 480.dp)
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp)
                .graphicsLayer {
                    alpha = alphaAnim.value
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Stylish Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Brush.horizontalGradient(listOf(PrimaryColor, PrimaryLight)))
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    
                    Text(
                        text = "My Profile",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Profile Image and Basic Info Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-50).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier
                                .size(110.dp)
                                .border(4.dp, Color.White, CircleShape)
                                .shadow(8.dp, CircleShape),
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5)
                        ) {
                            if (isUploading) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp, color = PrimaryColor)
                                }
                            } else if (!user.profileImage.isNullOrBlank()) {
                                val fullUrl = if (user.profileImage.startsWith("http")) user.profileImage 
                                              else "${ApiClient.MEDIA_BASE_URL}${if (user.profileImage.startsWith("/")) "" else "/"}${user.profileImage}"
                                KamelImage(
                                    resource = { asyncPainterResource(fullUrl) },
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onLoading = { _: Float -> Box(Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) }
                                )
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.initials,
                                        color = PrimaryColor,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        
                        IconButton(
                            onClick = { imagePicker.launch() },
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryColor, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = user.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                when(user.role.uppercase()) {
                                    "OWNER" -> Icons.Default.Business
                                    "DALALI" -> Icons.Default.Handshake
                                    else -> Icons.Default.Person
                                },
                                null,
                                tint = PrimaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = user.role.uppercase(),
                                color = PrimaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Profile Sections
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-20).dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeader("Account Details")
                    
                    ProfileOptionItem(
                        icon = Icons.Default.Edit,
                        title = "Edit Profile Info",
                        subtitle = "Name, Phone, Bio",
                        color = PrimaryColor,
                        onClick = { showEditDialog = true }
                    )
                    
                    ProfileOptionItem(
                        icon = Icons.Default.Language,
                        title = strings.language,
                        subtitle = localizationManager.currentLanguage.displayName,
                        color = Color(0xFF00897B),
                        onClick = {
                            val nextLang = if (localizationManager.currentLanguage.code == "en") 
                                Language.SWAHILI
                            else 
                                Language.ENGLISH
                            localizationManager.changeLanguage(nextLang)
                        }
                    )

                    SectionHeader("Security & Support")

                    ProfileOptionItem(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        color = Color(0xFFF9A825),
                        onClick = { /* TODO */ }
                    )

                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.HelpCenter,
                        title = "Help & Support",
                        color = Color(0xFF7E57C2),
                        onClick = { /* TODO */ }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Logout Action
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable { onLogout() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                    ) {
                        Row(
                            Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            @Suppress("DEPRECATION")
                            Text(strings.logout, color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                onUpdateUser(updatedUser)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.LightGray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FA),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var businessName by remember { mutableStateOf(user.businessName ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+255...") },
                    shape = RoundedCornerShape(12.dp)
                )

                if (user.role.uppercase() == "OWNER" || user.role.uppercase() == "DALALI") {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CANCEL")
                    }
                    
                    Button(
                        onClick = {
                            isSaving = true
                            val updated = user.copy(
                                name = name,
                                phone = phone,
                                businessName = if (businessName.isBlank()) null else businessName
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("SAVE CHANGES", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
