package org.com.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.com.auth.AuthState
import org.com.model.RegisterRequest
import org.com.ui.AppColors


/* ============================================================
   ROOMIFY COLORS - MATCHING LOGIN SCREEN
   ============================================================ */

private object RegisterColors {
    // Same as LoginColors
    val GradientStart = AppColors.SplashGradientStart   // #1A237E
    val GradientEnd = AppColors.SplashGradientEnd       // #3949AB

    val White = Color(0xFFFFFFFF)
    val White70 = Color(0xB3FFFFFF)
    val White55 = Color(0x8CFFFFFF)

    val Surface = Color.White
    val TextDark = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFF9E9E9E) // Stronger border

    val Error = Color(0xFFEF4444)
    val ErrorBackground = Color(0xFFFEF2F2)
    val ChipBackground = Color(0xFFF3F4F6)

    // Additional Register-specific
    val InfoBackground = Color(0xFFEEF2FF)
    val InfoText = Color(0xFF1A237E)
}


/* ============================================================
   REGISTER SCREEN
   ============================================================ */

@Composable
fun RegisterScreen(
    authState: AuthState,
    onRegister: (RegisterRequest) -> Unit,
    onGoogleRegister: (String, String) -> Unit,
    onLoginClick: () -> Unit,
    onBack: () -> Unit
) {

    var selectedRole by remember { mutableStateOf("tenant") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Owner fields
    var businessName by remember { mutableStateOf("") }
    var baloziName by remember { mutableStateOf("") }
    var baloziPhone by remember { mutableStateOf("") }
    var baloziArea by remember { mutableStateOf("") }
    var baloziVillage by remember { mutableStateOf("") }
    var baloziWard by remember { mutableStateOf("") }
    var baloziDistrict by remember { mutableStateOf("") }
    var baloziRegion by remember { mutableStateOf("") }

    // Dalali fields
    var licenseNumber by remember { mutableStateOf("") }
    var locationArea by remember { mutableStateOf("") }

    val loading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    val passwordsMatch = password == confirmPassword

    val basicFieldsValid = name.isNotBlank() &&
            email.isNotBlank() &&
            phone.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            passwordsMatch

    val roleFieldsValid = when (selectedRole) {
        "tenant" -> true
        "owner" -> businessName.isNotBlank() &&
                baloziName.isNotBlank() &&
                baloziPhone.isNotBlank() &&
                baloziArea.isNotBlank() &&
                baloziVillage.isNotBlank()
        "dalali" -> businessName.isNotBlank() &&
                licenseNumber.isNotBlank() &&
                locationArea.isNotBlank()
        else -> false
    }

    val formValid = basicFieldsValid && roleFieldsValid

    // Using the same animation as LoginScreen
    val contentWidth = 320.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RegisterColors.GradientStart,
                        RegisterColors.GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Decorative circles (same as LoginScreen)
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .alpha(0.06f)
                .background(Color.White, shape = CircleShape)
                .align(Alignment.TopStart)
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 140.dp, y = 140.dp)
                .alpha(0.06f)
                .background(Color.White, shape = CircleShape)
                .align(Alignment.BottomEnd)
        )

        // Main Card - same style as LoginScreen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 360.dp, max = 440.dp)
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = RegisterColors.Surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Logo - same as LoginScreen
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    RegisterColors.GradientStart,
                                    RegisterColors.GradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Roomify",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title - same as LoginScreen
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = RegisterColors.TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Sign up to get started",
                    fontSize = 14.sp,
                    color = RegisterColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // NAME - same style as LoginScreen email field
                RegisterTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Full Name",
                    icon = Icons.Default.Person,
                    enabled = !loading,
                    contentWidth = contentWidth
                )

                Spacer(modifier = Modifier.height(12.dp))

                // EMAIL - same style as LoginScreen
                RegisterTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email Address",
                    icon = Icons.Default.Email,
                    enabled = !loading,
                    contentWidth = contentWidth
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PHONE - same style as LoginScreen
                RegisterTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "Phone Number",
                    icon = Icons.Default.Phone,
                    enabled = !loading,
                    contentWidth = contentWidth
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PASSWORD - same style as LoginScreen
                RegisterPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    visible = showPassword,
                    onToggle = { showPassword = !showPassword },
                    enabled = !loading,
                    contentWidth = contentWidth
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Password Strength (simplified to match LoginScreen style)
                AnimatedVisibility(
                    visible = password.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    PasswordStrengthIndicator(
                        password = password,
                        contentWidth = contentWidth
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CONFIRM PASSWORD - same style as LoginScreen
                RegisterPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm Password",
                    visible = showConfirmPassword,
                    onToggle = { showConfirmPassword = !showConfirmPassword },
                    enabled = !loading,
                    contentWidth = contentWidth
                )

                // Password error - same style as LoginScreen
                AnimatedVisibility(
                    visible = confirmPassword.isNotBlank() && !passwordsMatch,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .width(contentWidth)
                            .padding(top = 6.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = RegisterColors.Error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Passwords do not match",
                            color = RegisterColors.Error,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ROLE LABEL - same as LoginScreen
                Text(
                    text = "Sign up as",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = RegisterColors.TextSecondary,
                    modifier = Modifier
                        .width(contentWidth)
                        .padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ROLE CHIPS - same style as LoginScreen
                Row(
                    modifier = Modifier.width(contentWidth),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RegisterRoleChip(
                        label = "Tenant",
                        icon = Icons.Default.Person,
                        isSelected = selectedRole == "tenant",
                        onClick = { selectedRole = "tenant" },
                        modifier = Modifier.weight(1f)
                    )

                    RegisterRoleChip(
                        label = "Owner",
                        icon = Icons.Default.Storefront,
                        isSelected = selectedRole == "owner",
                        onClick = { selectedRole = "owner" },
                        modifier = Modifier.weight(1f)
                    )

                    RegisterRoleChip(
                        label = "Dalali",
                        icon = Icons.Default.Work,
                        isSelected = selectedRole == "dalali",
                        onClick = { selectedRole = "dalali" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Role-specific fields
                AnimatedVisibility(
                    visible = selectedRole == "tenant",
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                    exit = fadeOut()
                ) {
                    RoleWelcomeCard(
                        icon = Icons.Default.Home,
                        title = "Tenant Account",
                        description = "Find rooms, apartments and comfortable places to stay.",
                        contentWidth = contentWidth
                    )
                }

                AnimatedVisibility(
                    visible = selectedRole == "owner",
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                    exit = fadeOut()
                ) {
                    OwnerDetailsSection(
                        businessName = businessName,
                        onBusinessNameChange = { businessName = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        baloziName = baloziName,
                        onBaloziNameChange = { baloziName = it },
                        baloziPhone = baloziPhone,
                        onBaloziPhoneChange = { baloziPhone = it },
                        baloziArea = baloziArea,
                        onBaloziAreaChange = { baloziArea = it },
                        baloziVillage = baloziVillage,
                        onBaloziVillageChange = { baloziVillage = it },
                        baloziWard = baloziWard,
                        onBaloziWardChange = { baloziWard = it },
                        baloziDistrict = baloziDistrict,
                        onBaloziDistrictChange = { baloziDistrict = it },
                        baloziRegion = baloziRegion,
                        onBaloziRegionChange = { baloziRegion = it },
                        loading = loading,
                        contentWidth = contentWidth
                    )
                }

                AnimatedVisibility(
                    visible = selectedRole == "dalali",
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                    exit = fadeOut()
                ) {
                    DalaliDetailsSection(
                        businessName = businessName,
                        onBusinessNameChange = { businessName = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        locationArea = locationArea,
                        onLocationAreaChange = { locationArea = it },
                        licenseNumber = licenseNumber,
                        onLicenseNumberChange = { licenseNumber = it },
                        loading = loading,
                        contentWidth = contentWidth
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Error Message - same style as LoginScreen
                if (errorMessage != null) {
                    ErrorMessageRegister(
                        message = errorMessage,
                        contentWidth = contentWidth
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // REGISTER BUTTON - same style as LoginScreen
                Button(
                    onClick = {
                        val request = when (selectedRole) {
                            "tenant" -> RegisterRequest.createTenant(
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                phone = phone.trim()
                            )
                            "owner" -> RegisterRequest.createOwner(
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                businessName = businessName.trim(),
                                phone = phone.trim(),
                                baloziName = baloziName.trim(),
                                baloziPhone = baloziPhone.trim(),
                                baloziArea = baloziArea.trim(),
                                baloziVillage = baloziVillage.trim(),
                                baloziWard = baloziWard.trim(),
                                baloziDistrict = baloziDistrict.trim(),
                                baloziRegion = baloziRegion.trim()
                            )
                            "dalali" -> RegisterRequest.createDalali(
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                businessName = businessName.trim(),
                                phone = phone.trim(),
                                licenseNumber = licenseNumber.trim(),
                                locationArea = locationArea.trim()
                            )
                            else -> RegisterRequest.createTenant(
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                phone = phone.trim()
                            )
                        }
                        onRegister(request)
                    },
                    enabled = !loading && formValid,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RegisterColors.GradientStart,
                        disabledContainerColor = RegisterColors.Border
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "SIGN UP",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DIVIDER - same as LoginScreen
                Row(
                    modifier = Modifier.width(contentWidth),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RegisterColors.Border
                    )
                    Text(
                        text = "OR",
                        fontSize = 12.sp,
                        color = RegisterColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = RegisterColors.Border
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // GOOGLE BUTTON - same as LoginScreen
                OutlinedButton(
                    onClick = { onGoogleRegister("", selectedRole) },
                    enabled = !loading,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RegisterColors.Border)
                ) {
                    Text(
                        text = "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RegisterColors.TextDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = RegisterColors.TextDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LOGIN LINK - same as LoginScreen
                Row(
                    modifier = Modifier.width(contentWidth),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        color = RegisterColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = onLoginClick,
                        enabled = !loading,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "Login",
                            color = RegisterColors.GradientStart,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // BACK - same as LoginScreen
                TextButton(
                    onClick = onBack,
                    enabled = !loading,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = RegisterColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Back",
                        fontSize = 12.sp,
                        color = RegisterColors.TextSecondary
                    )
                }
            }
        }
    }

    // Loading Overlay
    AnimatedVisibility(
        visible = loading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 260.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 15.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = RegisterColors.GradientStart,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Creating your account...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RegisterColors.TextDark
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Please wait",
                        fontSize = 12.sp,
                        color = RegisterColors.TextSecondary
                    )
                }
            }
        }
    }
}


// ============================================================
// COMPONENTS - MATCHING LOGIN SCREEN STYLE
// ============================================================

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    enabled: Boolean,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(contentWidth)
            .height(60.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = RegisterColors.TextSecondary.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RegisterColors.GradientStart,
                modifier = Modifier.size(18.dp)
            )
        },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RegisterColors.GradientStart,
            focusedLabelColor = RegisterColors.GradientStart,
            unfocusedBorderColor = RegisterColors.Border,
            unfocusedLabelColor = RegisterColors.TextSecondary,
            cursorColor = RegisterColors.GradientStart
        )
    )
}


@Composable
private fun RegisterPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(contentWidth)
            .height(60.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = RegisterColors.TextSecondary.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = RegisterColors.GradientStart,
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onToggle,
                enabled = enabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Hide" else "Show",
                    tint = RegisterColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RegisterColors.GradientStart,
            focusedLabelColor = RegisterColors.GradientStart,
            unfocusedBorderColor = RegisterColors.Border,
            unfocusedLabelColor = RegisterColors.TextSecondary,
            cursorColor = RegisterColors.GradientStart
        )
    )
}


@Composable
private fun RegisterRoleChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier.height(36.dp),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RegisterColors.ChipBackground,
            labelColor = RegisterColors.TextSecondary,
            iconColor = RegisterColors.TextSecondary,
            selectedContainerColor = RegisterColors.GradientStart.copy(alpha = 0.12f),
            selectedLabelColor = RegisterColors.GradientStart,
            selectedLeadingIconColor = RegisterColors.GradientStart
        )
    )
}


@Composable
private fun PasswordStrengthIndicator(
    password: String,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    val strength = when {
        password.length < 6 -> 0.25f
        password.length < 8 -> 0.50f
        password.any { it.isDigit() } && password.any { it.isUpperCase() } && password.length >= 8 -> 1f
        else -> 0.75f
    }

    val label = when {
        strength <= 0.25f -> "Weak"
        strength <= 0.50f -> "Fair"
        strength <= 0.75f -> "Good"
        else -> "Strong"
    }

    Column(
        modifier = Modifier
            .width(contentWidth)
            .padding(top = 4.dp, start = 4.dp, end = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Password strength",
                fontSize = 11.sp,
                color = RegisterColors.TextSecondary
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RegisterColors.GradientStart
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { strength },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = RegisterColors.GradientStart,
            trackColor = RegisterColors.Border
        )
    }
}


@Composable
private fun RoleWelcomeCard(
    icon: ImageVector,
    title: String,
    description: String,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = Modifier
            .width(contentWidth)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = RegisterColors.InfoBackground
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RegisterColors.GradientStart.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RegisterColors.GradientStart,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RegisterColors.InfoText
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = RegisterColors.TextSecondary
                )
            }
        }
    }
}


@Composable
private fun OwnerDetailsSection(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    baloziName: String,
    onBaloziNameChange: (String) -> Unit,
    baloziPhone: String,
    onBaloziPhoneChange: (String) -> Unit,
    baloziArea: String,
    onBaloziAreaChange: (String) -> Unit,
    baloziVillage: String,
    onBaloziVillageChange: (String) -> Unit,
    baloziWard: String,
    onBaloziWardChange: (String) -> Unit,
    baloziDistrict: String,
    onBaloziDistrictChange: (String) -> Unit,
    baloziRegion: String,
    onBaloziRegionChange: (String) -> Unit,
    loading: Boolean,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier.width(contentWidth)
    ) {
        // Business Name
        RegisterTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            placeholder = "Business / Company Name",
            icon = Icons.Default.Business,
            enabled = !loading,
            contentWidth = contentWidth
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Phone
        RegisterTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = "Owner Phone Number",
            icon = Icons.Default.Phone,
            enabled = !loading,
            contentWidth = contentWidth
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Balozi Card
        Card(
            modifier = Modifier
                .width(contentWidth)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = RegisterColors.InfoBackground
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = "Balozi Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RegisterColors.InfoText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Property location verification",
                    fontSize = 11.sp,
                    color = RegisterColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                RegisterTextField(
                    value = baloziName,
                    onValueChange = onBaloziNameChange,
                    placeholder = "Balozi Full Name",
                    icon = Icons.Default.Person,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
                Spacer(modifier = Modifier.height(8.dp))

                RegisterTextField(
                    value = baloziPhone,
                    onValueChange = onBaloziPhoneChange,
                    placeholder = "Balozi Phone",
                    icon = Icons.Default.ContactPhone,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
                Spacer(modifier = Modifier.height(8.dp))

                RegisterTextField(
                    value = baloziArea,
                    onValueChange = onBaloziAreaChange,
                    placeholder = "Area Name",
                    icon = Icons.Default.LocationOn,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
                Spacer(modifier = Modifier.height(8.dp))

                RegisterTextField(
                    value = baloziVillage,
                    onValueChange = onBaloziVillageChange,
                    placeholder = "Village",
                    icon = Icons.Default.Home,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
                Spacer(modifier = Modifier.height(8.dp))

                RegisterTextField(
                    value = baloziWard,
                    onValueChange = onBaloziWardChange,
                    placeholder = "Ward",
                    icon = Icons.Default.LocationOn,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
                Spacer(modifier = Modifier.height(8.dp))

                RegisterTextField(
                    value = baloziDistrict,
                    onValueChange = onBaloziDistrictChange,
                    placeholder = "District",
                    icon = Icons.Default.LocationOn,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
                Spacer(modifier = Modifier.height(8.dp))

                RegisterTextField(
                    value = baloziRegion,
                    onValueChange = onBaloziRegionChange,
                    placeholder = "Region",
                    icon = Icons.Default.LocationOn,
                    enabled = !loading,
                    contentWidth = contentWidth
                )
            }
        }
    }
}


@Composable
private fun DalaliDetailsSection(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    locationArea: String,
    onLocationAreaChange: (String) -> Unit,
    licenseNumber: String,
    onLicenseNumberChange: (String) -> Unit,
    loading: Boolean,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier.width(contentWidth)
    ) {
        RegisterTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = "Phone Number",
            icon = Icons.Default.Phone,
            enabled = !loading,
            contentWidth = contentWidth
        )
        Spacer(modifier = Modifier.height(10.dp))

        RegisterTextField(
            value = locationArea,
            onValueChange = onLocationAreaChange,
            placeholder = "Working Location",
            icon = Icons.Default.LocationOn,
            enabled = !loading,
            contentWidth = contentWidth
        )
        Spacer(modifier = Modifier.height(10.dp))

        RegisterTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            placeholder = "Agency / Business Name",
            icon = Icons.Default.Business,
            enabled = !loading,
            contentWidth = contentWidth
        )
        Spacer(modifier = Modifier.height(10.dp))

        RegisterTextField(
            value = licenseNumber,
            onValueChange = onLicenseNumberChange,
            placeholder = "License / ID Number",
            icon = Icons.Default.Badge,
            enabled = !loading,
            contentWidth = contentWidth
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .width(contentWidth)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = RegisterColors.InfoBackground
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = RegisterColors.GradientStart,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dalali accounts require verification before listing rooms.",
                    fontSize = 11.sp,
                    color = RegisterColors.InfoText,
                    lineHeight = 16.sp
                )
            }
        }
    }
}


@Composable
private fun ErrorMessageRegister(
    message: String,
    contentWidth: androidx.compose.ui.unit.Dp
) {
    Row(
        modifier = Modifier
            .width(contentWidth)
            .clip(RoundedCornerShape(8.dp))
            .background(RegisterColors.ErrorBackground)
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "!",
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(RegisterColors.Error.copy(alpha = 0.12f))
                .padding(top = 1.dp),
            color = RegisterColors.Error,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = message,
            color = RegisterColors.Error,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
