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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.com.i18n.Language
import org.com.i18n.LocalLocalizationManager
import org.com.i18n.LocalRoomifyStrings
import org.com.model.*
import org.com.network.ApiClient
import org.com.network.RoomifyApi

private val NavyPrimary = Color(0xFF142170)

@Composable
fun ProfileScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onUpdateUser: (User) -> Unit = {},
    onNavigate: (String) -> Unit = {}
) {
    val localizationManager = LocalLocalizationManager.current
    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyPrimary),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val isExpanded = width >= 840.dp
        val isMedium = width >= 600.dp && width < 840.dp

        if (isExpanded) {
            DesktopProfileLayout(
                user = user,
                isUploading = isUploading,
                onBack = onBack,
                onLogout = onLogout,
                onAvatarUpload = { imagePicker.launch() },
                onEditProfileClick = { showEditDialog = true },
                onLanguageClick = {
                    val nextLang = if (localizationManager.currentLanguage.code == "en")
                        Language.SWAHILI
                    else
                        Language.ENGLISH
                    localizationManager.changeLanguage(nextLang)
                },
                onChangePasswordClick = { showChangePasswordDialog = true },
                onHelpClick = { showHelpDialog = true },
                onDeleteAccountClick = { showDeleteAccountDialog = true },
                onNavigate = onNavigate
            )
        } else if (isMedium) {
            MediumProfileLayout(
                user = user,
                isUploading = isUploading,
                onBack = onBack,
                onLogout = onLogout,
                onAvatarUpload = { imagePicker.launch() },
                onEditProfileClick = { showEditDialog = true },
                onLanguageClick = {
                    val nextLang = if (localizationManager.currentLanguage.code == "en")
                        Language.SWAHILI
                    else
                        Language.ENGLISH
                    localizationManager.changeLanguage(nextLang)
                },
                onChangePasswordClick = { showChangePasswordDialog = true },
                onHelpClick = { showHelpDialog = true },
                onDeleteAccountClick = { showDeleteAccountDialog = true }
            )
        } else {
            CompactProfileLayout(
                user = user,
                isUploading = isUploading,
                onBack = onBack,
                onLogout = onLogout,
                onAvatarUpload = { imagePicker.launch() },
                onEditProfileClick = { showEditDialog = true },
                onLanguageClick = {
                    val nextLang = if (localizationManager.currentLanguage.code == "en")
                        Language.SWAHILI
                    else
                        Language.ENGLISH
                    localizationManager.changeLanguage(nextLang)
                },
                onChangePasswordClick = { showChangePasswordDialog = true },
                onHelpClick = { showHelpDialog = true },
                onDeleteAccountClick = { showDeleteAccountDialog = true }
            )
        }
    }

    if (showEditDialog) {
        EditProfileModal(
            user = user,
            onDismiss = { showEditDialog = false },
            onSave = { updatedUser ->
                onUpdateUser(updatedUser)
                showEditDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordModal(
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showHelpDialog) {
        HelpSupportModal(
            onDismiss = { showHelpDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountModal(
            onDismiss = { showDeleteAccountDialog = false },
            onAccountDeleted = {
                showDeleteAccountDialog = false
                onLogout()
            }
        )
    }
}

@Composable
private fun DesktopProfileLayout(
    user: User,
    isUploading: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAvatarUpload: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onHelpClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .widthIn(max = 1120.dp)
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
                .verticalScroll(scrollState)
        ) {
            // Clean Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { onBack() },
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Text(
                    text = "My Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // 2-Column Split Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column: Identity Summary Card (weight = 0.40f, minWidth = 320.dp)
                Surface(
                    modifier = Modifier
                        .weight(0.40f)
                        .widthIn(min = 320.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarSummary(
                            user = user,
                            isUploading = isUploading,
                            onAvatarUpload = onAvatarUpload
                        )

                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        LogoutButton(onClick = onLogout, height = 46.dp)
                    }
                }

                // Right Column: Settings & Snapshot Cards (weight = 0.60f)
                Column(
                    modifier = Modifier.weight(0.60f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Section 1: ACCOUNT DETAILS
                    SettingsSectionCard(
                        headerTitle = "ACCOUNT DETAILS",
                        items = listOf(
                            SettingRowData(
                                icon = Icons.Default.Edit,
                                title = "Edit Profile Info",
                                subtitle = "Name, Phone, Bio",
                                bgColor = Color(0xFFEEF2FF),
                                iconColor = NavyPrimary,
                                onClick = onEditProfileClick
                            ),
                            SettingRowData(
                                icon = Icons.Default.Language,
                                title = "Language",
                                subtitle = LocalLocalizationManager.current.currentLanguage.displayName,
                                bgColor = Color(0xFFE6F4F1),
                                iconColor = Color(0xFF00897B),
                                onClick = onLanguageClick
                            )
                        )
                    )

                    // Section 2: SECURITY & SETTINGS
                    SettingsSectionCard(
                        headerTitle = "SECURITY & SETTINGS",
                        items = listOf(
                            SettingRowData(
                                icon = Icons.Default.Lock,
                                title = "Change Password",
                                subtitle = "Update your account password",
                                bgColor = Color(0xFFFEF3C7),
                                iconColor = Color(0xFFD97706),
                                onClick = onChangePasswordClick
                            ),
                            SettingRowData(
                                icon = Icons.AutoMirrored.Filled.HelpCenter,
                                title = "Help & Support",
                                subtitle = "Contact support & FAQs",
                                bgColor = Color(0xFFF3E8FF),
                                iconColor = Color(0xFF7E57C2),
                                onClick = onHelpClick
                            ),
                            SettingRowData(
                                icon = Icons.Default.DeleteForever,
                                title = "Delete Account",
                                subtitle = "Permanently deactivate your account & data",
                                bgColor = Color(0xFFFEE2E2),
                                iconColor = Color(0xFFDC2626),
                                onClick = onDeleteAccountClick
                            )
                        )
                    )

                    // Section 3: ACCOUNT & ACTIVITY SNAPSHOT
                    AccountActivitySnapshotCard(user = user, onNavigate = onNavigate)
                }
            }
        }
    }
}

@Composable
private fun AccountActivitySnapshotCard(
    user: User,
    onNavigate: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ACCOUNT & ACTIVITY SNAPSHOT",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Language", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            LocalLocalizationManager.current.currentLanguage.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Account Role", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            user.role.uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 14.dp)
                )

                Text("Quick Jump Links", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onNavigate("saved") },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFCE7F3),
                        border = BorderStroke(1.dp, Color(0xFFFBCFE8))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Favorite, null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Saved Places", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D174D))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onNavigate("bookings") },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ListAlt, null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("My Bookings", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MediumProfileLayout(
    user: User,
    isUploading: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAvatarUpload: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onHelpClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .background(NavyPrimary)
            ) {
                Surface(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .size(40.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable { onBack() },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "My Profile",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Avatar Header Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-48).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarSummary(
                    user = user,
                    isUploading = isUploading,
                    onAvatarUpload = onAvatarUpload
                )
            }

            // Settings Sections Stack
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-20).dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SettingsSectionCard(
                    headerTitle = "ACCOUNT DETAILS",
                    items = listOf(
                        SettingRowData(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile Info",
                            subtitle = "Name, Phone, Bio",
                            bgColor = Color(0xFFEEF2FF),
                            iconColor = NavyPrimary,
                            onClick = onEditProfileClick
                        ),
                        SettingRowData(
                            icon = Icons.Default.Language,
                            title = "Language",
                            subtitle = LocalLocalizationManager.current.currentLanguage.displayName,
                            bgColor = Color(0xFFE6F4F1),
                            iconColor = Color(0xFF00897B),
                            onClick = onLanguageClick
                        )
                    )
                )

                SettingsSectionCard(
                    headerTitle = "SECURITY & SETTINGS",
                    items = listOf(
                        SettingRowData(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            subtitle = "Update your account password",
                            bgColor = Color(0xFFFEF3C7),
                            iconColor = Color(0xFFD97706),
                            onClick = onChangePasswordClick
                        ),
                        SettingRowData(
                            icon = Icons.AutoMirrored.Filled.HelpCenter,
                            title = "Help & Support",
                            subtitle = "Contact support & FAQs",
                            bgColor = Color(0xFFF3E8FF),
                            iconColor = Color(0xFF7E57C2),
                            onClick = onHelpClick
                        ),
                        SettingRowData(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete Account",
                            subtitle = "Permanently deactivate your account & data",
                            bgColor = Color(0xFFFEE2E2),
                            iconColor = Color(0xFFDC2626),
                            onClick = onDeleteAccountClick
                        )
                    )
                )

                Spacer(Modifier.height(8.dp))

                LogoutButton(onClick = onLogout, height = 48.dp)

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CompactProfileLayout(
    user: User,
    isUploading: Boolean,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAvatarUpload: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onHelpClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Curved Top Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .background(NavyPrimary)
            ) {
                Surface(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .size(40.dp)
                        .clickable { onBack() },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = "My Profile",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Identity Header Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-48).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AvatarSummary(
                    user = user,
                    isUploading = isUploading,
                    onAvatarUpload = onAvatarUpload
                )
            }

            // Settings Sections & Logout Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SettingsSectionCard(
                    headerTitle = "ACCOUNT DETAILS",
                    items = listOf(
                        SettingRowData(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile Info",
                            subtitle = "Name, Phone, Bio",
                            bgColor = Color(0xFFEEF2FF),
                            iconColor = NavyPrimary,
                            onClick = onEditProfileClick
                        ),
                        SettingRowData(
                            icon = Icons.Default.Language,
                            title = "Language",
                            subtitle = LocalLocalizationManager.current.currentLanguage.displayName,
                            bgColor = Color(0xFFE6F4F1),
                            iconColor = Color(0xFF00897B),
                            onClick = onLanguageClick
                        )
                    )
                )

                SettingsSectionCard(
                    headerTitle = "SECURITY & SETTINGS",
                    items = listOf(
                        SettingRowData(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            subtitle = "Update your account password",
                            bgColor = Color(0xFFFEF3C7),
                            iconColor = Color(0xFFD97706),
                            onClick = onChangePasswordClick
                        ),
                        SettingRowData(
                            icon = Icons.AutoMirrored.Filled.HelpCenter,
                            title = "Help & Support",
                            subtitle = "Contact support & FAQs",
                            bgColor = Color(0xFFF3E8FF),
                            iconColor = Color(0xFF7E57C2),
                            onClick = onHelpClick
                        ),
                        SettingRowData(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete Account",
                            subtitle = "Permanently deactivate your account & data",
                            bgColor = Color(0xFFFEE2E2),
                            iconColor = Color(0xFFDC2626),
                            onClick = onDeleteAccountClick
                        )
                    )
                )

                Spacer(Modifier.height(8.dp))

                LogoutButton(onClick = onLogout, height = 48.dp)

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AvatarSummary(
    user: User,
    isUploading: Boolean,
    onAvatarUpload: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(104.dp)
                    .border(3.dp, Color.White, CircleShape)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = Color(0xFFF1F5F9)
            ) {
                if (isUploading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = NavyPrimary
                        )
                    }
                } else if (!user.profileImage.isNullOrBlank()) {
                    val fullUrl = ApiClient.resolveUrl(user.profileImage)
                    KamelImage(
                        resource = { asyncPainterResource(fullUrl) },
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { Box(Modifier.fillMaxSize().background(Color(0xFFE2E8F0))) }
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = user.initials,
                            color = NavyPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onAvatarUpload,
                modifier = Modifier
                    .size(34.dp)
                    .background(NavyPrimary, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = user.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = user.email,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Surface(
            color = Color(0xFFEEF2FF),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = when (user.role.uppercase()) {
                        "OWNER" -> Icons.Default.Business
                        "DALALI" -> Icons.Default.Handshake
                        else -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = user.role.uppercase(),
                    color = Color(0xFF1E3A8A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class SettingRowData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val bgColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun SettingsSectionCard(
    headerTitle: String,
    items: List<SettingRowData>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = headerTitle.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    GroupedSettingRow(
                        icon = item.icon,
                        title = item.title,
                        subtitle = item.subtitle,
                        bgColor = item.bgColor,
                        iconColor = item.iconColor,
                        onClick = item.onClick
                    )

                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp,
                            modifier = Modifier.padding(start = 70.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GroupedSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                        }
                    }
                }
            }
            .clickable { onClick() },
        color = if (isHovered) Color(0xFFF8FAFC) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LogoutButton(
    onClick: () -> Unit,
    height: Dp = 46.dp
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFEF2F2),
        border = BorderStroke(1.dp, Color(0xFFFECACA))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Logout",
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ResponsiveDialogContainer(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            surfaceTintColor = Color.Transparent,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    titleColor: Color = Color(0xFF0F172A),
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Close",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EditProfileModal(
    user: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    val strings = LocalRoomifyStrings.current
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var bio by remember { mutableStateOf(user.bio ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    ResponsiveDialogContainer(onDismiss = onDismiss) {
        DialogHeader(title = strings.editProfile, onClose = onDismiss)

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.fullName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(strings.phoneNumber) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                prefix = {
                    Text("+255 ", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio (Optional)") },
                minLines = 3,
                maxLines = 4,
                placeholder = { Text("Tell hosts a bit about yourself...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("Cancel", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    isSaving = true
                    val updated = user.copy(
                        name = name,
                        phone = phone,
                        bio = if (bio.isBlank()) null else bio
                    )
                    onSave(updated)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordModal(
    onDismiss: () -> Unit
) {
    val strings = LocalRoomifyStrings.current
    val scope = rememberCoroutineScope()
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val isValid = oldPassword.isNotBlank() && newPassword.length >= 6 && newPassword == confirmPassword

    ResponsiveDialogContainer(onDismiss = { if (!isSubmitting) onDismiss() }) {
        DialogHeader(title = strings.changePassword, onClose = { if (!isSubmitting) onDismiss() })

        Spacer(Modifier.height(16.dp))

        if (successMsg != null) {
            Surface(
                color = Color(0xFFF0FDF4),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = successMsg!!,
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                imageVector = if (oldPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password (min 6 chars)") },
                    singleLine = true,
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = Color(0xFFDC2626),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("Cancel", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                }

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
                    enabled = !isSubmitting && isValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        disabledContainerColor = Color(0xFFE2E8F0)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            "Save Password",
                            color = if (isValid) Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSupportModal(
    onDismiss: () -> Unit
) {
    val strings = LocalRoomifyStrings.current
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var phoneCopied by remember { mutableStateOf(false) }
    var emailCopied by remember { mutableStateOf(false) }

    ResponsiveDialogContainer(onDismiss = onDismiss) {
        DialogHeader(title = strings.helpSupport, onClose = onDismiss)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Our customer support team is here to assist you with room listings, bookings, and inquiries.",
            fontSize = 14.sp,
            color = Color(0xFF475569),
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(16.dp))

        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Phone Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "+255 754 000 111 / +255 682 000 222",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("+255 754 000 111 / +255 682 000 222"))
                            phoneCopied = true
                            scope.launch {
                                delay(2000)
                                phoneCopied = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (phoneCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = "Copy phone",
                            tint = if (phoneCopied) Color(0xFF16A34A) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Email Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "support@roomify.co.tz",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("support@roomify.co.tz"))
                            emailCopied = true
                            scope.launch {
                                delay(2000)
                                emailCopied = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (emailCopied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                            contentDescription = "Copy email",
                            tint = if (emailCopied) Color(0xFF16A34A) else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Hours Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Mon - Sat: 8:00 AM - 8:00 PM EAT",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Action Buttons
        Button(
            onClick = { uriHandler.openUri("tel:+255754000111") },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Call Support Now", fontWeight = FontWeight.SemiBold, color = Color.White)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { uriHandler.openUri("mailto:support@roomify.co.tz") },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Icon(Icons.Default.Mail, contentDescription = null, tint = Color(0xFF334155), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Email Support", fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        }
    }
}

@Composable
private fun DeleteAccountModal(
    onDismiss: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var confirmText by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }

    val isDeleteEnabled = confirmText.trim() == "DELETE"

    ResponsiveDialogContainer(onDismiss = { if (!isDeleting) onDismiss() }) {
        DialogHeader(
            title = "Delete Account",
            titleColor = Color(0xFFDC2626),
            onClose = { if (!isDeleting) onDismiss() }
        )

        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "This action is permanent and cannot be undone.",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF991B1B)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Are you sure you want to permanently delete your Roomify account? All your personal information, room listings, favorites, and history will be deactivated.",
            fontSize = 14.sp,
            color = Color(0xFF475569),
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = confirmText,
            onValueChange = { confirmText = it },
            label = { Text("Type DELETE to confirm") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isDeleting,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("Cancel", color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    scope.launch {
                        isDeleting = true
                        try {
                            RoomifyApi.deleteAccount()
                            onAccountDeleted()
                        } catch (e: Exception) {
                            println("Delete account error: ${e.message}")
                        } finally {
                            isDeleting = false
                        }
                    }
                },
                enabled = !isDeleting && isDeleteEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    disabledContainerColor = Color(0xFFE2E8F0)
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        "Delete Permanently",
                        color = if (isDeleteEnabled) Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
