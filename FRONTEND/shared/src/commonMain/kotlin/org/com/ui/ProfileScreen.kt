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
import org.com.currentTimeMillis
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
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
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
                        fontSize = 24.sp,
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
                                val fullUrl = ApiClient.resolveUrl(user.profileImage)
                                
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111111)
                    )
                    Text(
                        text = user.email,
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
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
                    SectionHeader(strings.accountDetails)
                    
                    ProfileOptionItem(
                        icon = Icons.Default.Edit,
                        title = strings.editProfileInfo,
                        subtitle = strings.namePhoneBio,
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

                    SectionHeader(strings.securitySettings)

                    ProfileOptionItem(
                        icon = Icons.Default.Lock,
                        title = strings.changePassword,
                        color = Color(0xFFF9A825),
                        onClick = { showChangePasswordDialog = true }
                    )

                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.HelpCenter,
                        title = strings.helpSupport,
                        color = Color(0xFF7E57C2),
                        onClick = { showHelpDialog = true }
                    )

                    ProfileOptionItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently deactivate your account & data",
                        color = Color(0xFFD32F2F),
                        onClick = { showDeleteAccountDialog = true }
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

    if (showChangePasswordDialog) {
        var oldPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        var successMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showChangePasswordDialog = false },
            title = { Text(strings.changePassword, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (successMsg != null) {
                        Text(successMsg!!, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    } else {
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Current Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password (min 6 chars)") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm New Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorMsg != null) {
                            Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (successMsg != null) {
                    Button(onClick = { showChangePasswordDialog = false }) {
                        Text("OK")
                    }
                } else {
                    Button(
                        onClick = {
                            if (newPassword.length < 6) {
                                errorMsg = "Password must be at least 6 characters"
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                errorMsg = "New passwords do not match"
                                return@Button
                            }
                            scope.launch {
                                isSubmitting = true
                                errorMsg = null
                                try {
                                    val res = RoomifyApi.changePassword(oldPassword, newPassword)
                                    if (res.success) {
                                        successMsg = "Password changed successfully!"
                                    } else {
                                        errorMsg = res.message ?: "Failed to change password"
                                    }
                                } catch (e: Exception) {
                                    errorMsg = e.message ?: "Error changing password"
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting && oldPassword.isNotBlank() && newPassword.isNotBlank()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("SAVE")
                        }
                    }
                }
            },
            dismissButton = {
                if (successMsg == null) {
                    TextButton(onClick = { showChangePasswordDialog = false }) {
                        Text("CANCEL")
                    }
                }
            }
        )
    }

    if (showHelpDialog) {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(strings.helpSupport, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Our customer support team is here to assist you with room listings, bookings, and inquiries.", fontSize = 13.sp, color = Color.DarkGray)
                    
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("+255 754 000 111 / +255 682 000 222", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("support@roomify.co.tz", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Mon - Sat: 8:00 AM - 8:00 PM EAT", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Button(
                        onClick = { uriHandler.openUri("tel:+255754000111") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Call Support Now", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("CLOSE")
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        var isDeleting by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteAccountDialog = false },
            title = { Text("Delete Account?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your Roomify account? All your personal information, room listings, favorites, and history will be deactivated. This action cannot be undone.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                RoomifyApi.deleteAccount()
                                showDeleteAccountDialog = false
                                onLogout()
                            } catch (e: Exception) {
                                println("Delete account error: ${e.message}")
                            } finally {
                                isDeleting = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("DELETE PERMANENTLY", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }, enabled = !isDeleting) {
                    Text("CANCEL")
                }
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
    val strings = LocalRoomifyStrings.current
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
                Text(strings.editProfile, fontSize = 20.sp, fontWeight = FontWeight.Black, color = PrimaryColor)
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.fullName) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.phoneNumber) },
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
                        Text(strings.cancel.uppercase())
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
                            Text(strings.saveChanges.uppercase(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
