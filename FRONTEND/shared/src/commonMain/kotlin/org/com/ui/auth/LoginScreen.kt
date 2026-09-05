package org.com.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
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
import org.com.ui.AppColors


/* ============================================================
   ROOMIFY COLORS
   ============================================================ */

private object LoginColors {

    val GradientStart = AppColors.SplashGradientStart
    val GradientEnd = AppColors.SplashGradientEnd

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
}


/* ============================================================
   RESPONSIVE LAYOUT
   ============================================================ */

private enum class LoginLayout {
    MOBILE,
    TABLET,
    DESKTOP
}

private fun currentLoginLayout(): LoginLayout {
    return LoginLayout.DESKTOP
}


/* ============================================================
   MAIN SCREEN
   ============================================================ */

@Composable
fun LoginScreen(
    authState: AuthState,
    onLogin: (String, String, String) -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit,
    onGuestLogin: () -> Unit,
    onForgotPassword: () -> Unit
) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var showPassword by remember {
        mutableStateOf(false)
    }

    var selectedRole by remember {
        mutableStateOf("Tenant")
    }

    val loading =
        authState is AuthState.Loading

    val errorMessage =
        (authState as? AuthState.Error)?.message

    val formValid =
        email.isNotBlank() &&
                password.isNotBlank()

    val layout =
        currentLoginLayout()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LoginColors.GradientStart,
                        LoginColors.GradientEnd
                    )
                )
            )
    ) {

        when (layout) {

            LoginLayout.MOBILE -> {

                MobileLoginLayout(
                    loading = loading,
                    errorMessage = errorMessage,
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    showPassword = showPassword,
                    onTogglePassword = {
                        showPassword = !showPassword
                    },
                    selectedRole = selectedRole,
                    onRoleChange = {
                        selectedRole = it
                    },
                    formValid = formValid,
                    onLogin = {
                        onLogin(
                            email.trim(),
                            password,
                            selectedRole
                        )
                    },
                    onGoogleLogin = onGoogleLogin,
                    onRegisterClick = onRegisterClick,
                    onBack = onBack,
                    onGuestLogin = onGuestLogin,
                    onForgotPassword = onForgotPassword
                )
            }

            LoginLayout.TABLET,
            LoginLayout.DESKTOP -> {

                DesktopLoginLayout(
                    loading = loading,
                    errorMessage = errorMessage,
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    showPassword = showPassword,
                    onTogglePassword = {
                        showPassword = !showPassword
                    },
                    selectedRole = selectedRole,
                    onRoleChange = {
                        selectedRole = it
                    },
                    formValid = formValid,
                    onLogin = {
                        onLogin(
                            email.trim(),
                            password,
                            selectedRole
                        )
                    },
                    onGoogleLogin = onGoogleLogin,
                    onRegisterClick = onRegisterClick,
                    onBack = onBack,
                    onGuestLogin = onGuestLogin,
                    onForgotPassword = onForgotPassword
                )
            }
        }
    }
}


/* ============================================================
   MOBILE LAYOUT
   ============================================================ */

@Composable
private fun MobileLoginLayout(
    loading: Boolean,
    errorMessage: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    selectedRole: String,
    onRoleChange: (String) -> Unit,
    formValid: Boolean,
    onLogin: () -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit,
    onGuestLogin: () -> Unit,
    onForgotPassword: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 16.dp,
                vertical = 24.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        LoginBrand(compact = true)

        Spacer(
            modifier = Modifier.height(22.dp)
        )

        LoginFormCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp),
            loading = loading,
            errorMessage = errorMessage,
            email = email,
            onEmailChange = onEmailChange,
            password = password,
            onPasswordChange = onPasswordChange,
            showPassword = showPassword,
            onTogglePassword = onTogglePassword,
            selectedRole = selectedRole,
            onRoleChange = onRoleChange,
            formValid = formValid,
            onLogin = onLogin,
            onGoogleLogin = onGoogleLogin,
            onRegisterClick = onRegisterClick,
            onBack = onBack,
            onGuestLogin = onGuestLogin,
            onForgotPassword = onForgotPassword
        )
    }
}


/* ============================================================
   DESKTOP / WEB LAYOUT
   ============================================================ */

@Composable
private fun DesktopLoginLayout(
    loading: Boolean,
    errorMessage: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    selectedRole: String,
    onRoleChange: (String) -> Unit,
    formValid: Boolean,
    onLogin: () -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit,
    onGuestLogin: () -> Unit,
    onForgotPassword: () -> Unit
) {

    /*
     * ========================================================
     * DESKTOP CONTROL WIDTH
     * ========================================================
     *
     * The main card keeps its original size.
     *
     * Card:
     *     max = 440.dp
     *
     * Controls inside:
     *     width = 320.dp
     *
     * Because the parent Column uses:
     *
     *     horizontalAlignment = Alignment.CenterHorizontally
     *
     * the controls are automatically centered.
     *
     * Mobile is NOT affected.
     */

    val contentWidth = 320.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LoginColors.GradientStart,
                        LoginColors.GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        /* ====================================================
           DECORATIVE CIRCLE - TOP LEFT
           ==================================================== */

        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(
                    x = (-100).dp,
                    y = (-100).dp
                )
                .alpha(0.06f)
                .background(
                    Color.White,
                    shape = CircleShape
                )
                .align(Alignment.TopStart)
        )

        /* ====================================================
           DECORATIVE CIRCLE - BOTTOM RIGHT
           ==================================================== */

        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(
                    x = 140.dp,
                    y = 140.dp
                )
                .alpha(0.06f)
                .background(
                    Color.White,
                    shape = CircleShape
                )
                .align(Alignment.BottomEnd)
        )

        /* ====================================================
           MAIN CARD
           ==================================================== */

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(
                    min = 360.dp,
                    max = 440.dp
                )
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    LoginColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                /* ====================================================
                   LOGO
                   ==================================================== */

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    LoginColors.GradientStart,
                                    LoginColors.GradientEnd
                                )
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Home,
                        contentDescription =
                            "Roomify",
                        tint = Color.White,
                        modifier =
                            Modifier.size(30.dp)
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                /* ====================================================
                   TITLE
                   ==================================================== */

                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoginColors.TextDark
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Sign in to your account",
                    fontSize = 14.sp,
                    color = LoginColors.TextSecondary
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                /* ====================================================
                   EMAIL
                   ==================================================== */

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(60.dp),
                    label = {
                        Text(
                            "Email Address",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.Email,
                            contentDescription =
                                null,
                            tint =
                                LoginColors.GradientStart,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    enabled = !loading,
                    shape =
                        RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                                LoginColors.GradientStart,
                            focusedLabelColor =
                                LoginColors.GradientStart,
                            unfocusedBorderColor =
                                LoginColors.Border,
                            unfocusedLabelColor =
                                LoginColors.TextSecondary,
                            cursorColor =
                                LoginColors.GradientStart
                        )
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                /* ====================================================
                   PASSWORD
                   ==================================================== */

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(60.dp),
                    label = {
                        Text(
                            "Password",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.Lock,
                            contentDescription =
                                null,
                            tint =
                                LoginColors.GradientStart,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {

                        IconButton(
                            onClick =
                                onTogglePassword,
                            enabled = !loading,
                            modifier =
                                Modifier.size(32.dp)
                        ) {

                            Icon(
                                imageVector =
                                    if (showPassword)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                contentDescription =
                                    if (showPassword)
                                        "Hide"
                                    else
                                        "Show",
                                tint =
                                    LoginColors.TextSecondary,
                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation =
                        if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !loading,
                    shape =
                        RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor =
                                LoginColors.GradientStart,
                            focusedLabelColor =
                                LoginColors.GradientStart,
                            unfocusedBorderColor =
                                LoginColors.Border,
                            unfocusedLabelColor =
                                LoginColors.TextSecondary,
                            cursorColor =
                                LoginColors.GradientStart
                        )
                )

                /* ====================================================
                   FORGOT PASSWORD
                   ==================================================== */

                Row(
                    modifier = Modifier
                        .width(contentWidth),
                    horizontalArrangement =
                        Arrangement.End
                ) {

                    TextButton(
                        onClick =
                            onForgotPassword,
                        enabled = !loading,
                        modifier =
                            Modifier.height(32.dp)
                    ) {

                        Text(
                            text =
                                "Forgot Password?",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                LoginColors.GradientStart
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /* ====================================================
                   ROLE LABEL
                   ==================================================== */

                Text(
                    text = "Sign in as",
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        LoginColors.TextSecondary,
                    modifier = Modifier
                        .width(contentWidth)
                        .padding(start = 4.dp)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /* ====================================================
                   ROLE CHIPS
                   ==================================================== */

                Row(
                    modifier = Modifier
                        .width(contentWidth),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    LoginRoleChip(
                        modifier =
                            Modifier.weight(1f),
                        label = "Tenant",
                        icon =
                            Icons.Default.Person,
                        selected =
                            selectedRole == "Tenant",
                        enabled = !loading,
                        onClick = {
                            onRoleChange("Tenant")
                        }
                    )

                    LoginRoleChip(
                        modifier =
                            Modifier.weight(1f),
                        label = "Owner",
                        icon =
                            Icons.Default.Storefront,
                        selected =
                            selectedRole == "Owner",
                        enabled = !loading,
                        onClick = {
                            onRoleChange("Owner")
                        }
                    )

                    LoginRoleChip(
                        modifier =
                            Modifier.weight(1f),
                        label = "Dalali",
                        icon =
                            Icons.Default.Handshake,
                        selected =
                            selectedRole == "Dalali",
                        enabled = !loading,
                        onClick = {
                            onRoleChange("Dalali")
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /* ====================================================
                   ERROR
                   ==================================================== */

                if (errorMessage != null) {

                    ErrorMessage(
                        message = errorMessage
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )
                }

                /* ====================================================
                   LOGIN BUTTON
                   ==================================================== */

                Button(
                    onClick = onLogin,
                    enabled =
                        !loading && formValid,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(48.dp),
                    shape =
                        RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                LoginColors.GradientStart,
                            disabledContainerColor =
                                LoginColors.Border
                        )
                ) {

                    if (loading) {

                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )

                    } else {

                        Text(
                            text = "LOG IN",
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color = Color.White,
                            letterSpacing =
                                0.8.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /* ====================================================
                   DIVIDER
                   ==================================================== */

                Row(
                    modifier = Modifier
                        .width(contentWidth),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    HorizontalDivider(
                        modifier =
                            Modifier.weight(1f),
                        color =
                            LoginColors.Border
                    )

                    Text(
                        text = "OR",
                        fontSize = 12.sp,
                        color =
                            LoginColors.TextSecondary,
                        modifier =
                            Modifier.padding(
                                horizontal = 14.dp
                            )
                    )

                    HorizontalDivider(
                        modifier =
                            Modifier.weight(1f),
                        color =
                            LoginColors.Border
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /* ====================================================
                   GOOGLE BUTTON
                   ==================================================== */

                OutlinedButton(
                    onClick = {
                        onGoogleLogin(selectedRole)
                    },
                    enabled = !loading,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(44.dp),
                    shape =
                        RoundedCornerShape(12.dp),
                    border =
                        BorderStroke(
                            1.dp,
                            LoginColors.Border
                        )
                ) {

                    Text(
                        text = "G",
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            LoginColors.TextDark
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text =
                            "Continue with Google",
                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            LoginColors.TextDark
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                /* ====================================================
                   GUEST + SIGN UP
                   ==================================================== */

                Row(
                    modifier = Modifier
                        .width(contentWidth),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick =
                            onGuestLogin,
                        enabled = !loading,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape =
                            RoundedCornerShape(10.dp),
                        border =
                            BorderStroke(
                                1.dp,
                                LoginColors.Border
                            )
                    ) {

                        Text(
                            text = "Guest",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Medium,
                            color =
                                LoginColors.GradientStart
                        )
                    }

                    OutlinedButton(
                        onClick =
                            onRegisterClick,
                        enabled = !loading,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape =
                            RoundedCornerShape(10.dp),
                        border =
                            BorderStroke(
                                1.dp,
                                LoginColors.GradientStart
                            )
                    ) {

                        Text(
                            text = "Sign Up",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Medium,
                            color =
                                LoginColors.GradientStart
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                /* ====================================================
                   BACK
                   ==================================================== */

                TextButton(
                    onClick = onBack,
                    enabled = !loading,
                    modifier = Modifier
                        .width(contentWidth)
                        .height(32.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.ArrowBack,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(14.dp),
                        tint =
                            LoginColors.TextSecondary
                    )

                    Spacer(
                        modifier = Modifier.width(4.dp)
                    )

                    Text(
                        text = "Back",
                        fontSize = 12.sp,
                        color =
                            LoginColors.TextSecondary
                    )
                }
            }
        }
    }
}


/* ============================================================
   BRAND
   ============================================================ */

@Composable
private fun LoginBrand(
    compact: Boolean
) {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        BrandLogo(
            size =
                if (compact)
                    64.dp
                else
                    80.dp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "Roomify",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = LoginColors.TextDark
        )

        Text(
            text = "Find your next home",
            fontSize = 12.sp,
            color = LoginColors.TextSecondary
        )
    }
}


/* ============================================================
   LOGO
   ============================================================ */

@Composable
private fun BrandLogo(
    size: androidx.compose.ui.unit.Dp
) {

    Box(
        modifier = Modifier
            .size(size)
            .clip(
                RoundedCornerShape(20.dp)
            )
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        LoginColors.GradientStart,
                        LoginColors.GradientEnd
                    )
                )
            ),
        contentAlignment =
            Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.Home,
            contentDescription =
                "Roomify",
            tint = Color.White,
            modifier =
                Modifier.size(
                    size * 0.45f
                )
        )
    }
}


/* ============================================================
   LOGIN FORM CARD
   MOBILE ONLY
   ============================================================ */

@Composable
private fun LoginFormCard(
    modifier: Modifier,
    loading: Boolean,
    errorMessage: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    selectedRole: String,
    onRoleChange: (String) -> Unit,
    formValid: Boolean,
    onLogin: () -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit,
    onGuestLogin: () -> Unit,
    onForgotPassword: () -> Unit
) {

    Card(
        modifier = modifier,
        shape =
            RoundedCornerShape(24.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    LoginColors.Surface
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            Text(
                text = "Sign in",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LoginColors.TextDark
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text =
                    "Enter your details to continue.",
                fontSize = 13.sp,
                color = LoginColors.TextSecondary
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /* EMAIL */

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                label = {
                    Text(
                        "Email",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Default.Email,
                        contentDescription =
                            null,
                        tint =
                            LoginColors.GradientStart,
                        modifier =
                            Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                enabled = !loading,
                shape =
                    RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                            LoginColors.GradientStart,
                        focusedLabelColor =
                            LoginColors.GradientStart,
                        unfocusedBorderColor =
                            LoginColors.Border,
                        unfocusedLabelColor =
                            LoginColors.TextSecondary,
                        cursorColor =
                            LoginColors.GradientStart
                    )
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            /* PASSWORD */

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                label = {
                    Text(
                        "Password",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Default.Lock,
                        contentDescription =
                            null,
                        tint =
                            LoginColors.GradientStart,
                        modifier =
                            Modifier.size(18.dp)
                    )
                },
                trailingIcon = {

                    IconButton(
                        onClick =
                            onTogglePassword,
                        enabled = !loading,
                        modifier =
                            Modifier.size(36.dp)
                    ) {

                        Icon(
                            imageVector =
                                if (showPassword)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                            contentDescription =
                                null,
                            tint =
                                LoginColors.TextSecondary,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }
                },
                visualTransformation =
                    if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                singleLine = true,
                enabled = !loading,
                shape =
                    RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor =
                            LoginColors.GradientStart,
                        focusedLabelColor =
                            LoginColors.GradientStart,
                        unfocusedBorderColor =
                            LoginColors.Border,
                        unfocusedLabelColor =
                            LoginColors.TextSecondary,
                        cursorColor =
                            LoginColors.GradientStart
                    )
            )

            /* FORGOT PASSWORD */

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {

                TextButton(
                    onClick =
                        onForgotPassword,
                    enabled = !loading,
                    modifier =
                        Modifier.height(30.dp)
                ) {

                    Text(
                        text = "Forgot?",
                        fontSize = 11.sp,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            LoginColors.GradientStart
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /* ROLE */

            Text(
                text = "Sign in as",
                fontSize = 12.sp,
                fontWeight =
                    FontWeight.Medium,
                color =
                    LoginColors.TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                LoginRoleChip(
                    modifier =
                        Modifier.weight(1f),
                    label = "Tenant",
                    icon =
                        Icons.Default.Person,
                    selected =
                        selectedRole == "Tenant",
                    enabled = !loading,
                    onClick = {
                        onRoleChange("Tenant")
                    }
                )

                LoginRoleChip(
                    modifier =
                        Modifier.weight(1f),
                    label = "Owner",
                    icon =
                        Icons.Default.Storefront,
                    selected =
                        selectedRole == "Owner",
                    enabled = !loading,
                    onClick = {
                        onRoleChange("Owner")
                    }
                )

                LoginRoleChip(
                    modifier =
                        Modifier.weight(1f),
                    label = "Dalali",
                    icon =
                        Icons.Default.Handshake,
                    selected =
                        selectedRole == "Dalali",
                    enabled = !loading,
                    onClick = {
                        onRoleChange("Dalali")
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /* ERROR */

            if (errorMessage != null) {

                ErrorMessage(
                    message = errorMessage
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }

            /* LOGIN */

            Button(
                onClick = onLogin,
                enabled =
                    !loading && formValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape =
                    RoundedCornerShape(12.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            LoginColors.GradientStart,
                        disabledContainerColor =
                            LoginColors.Border
                    )
            ) {

                if (loading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Sign In",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            /* DIVIDER */

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                HorizontalDivider(
                    modifier =
                        Modifier.weight(1f),
                    color =
                        LoginColors.Border
                )

                Text(
                    text = "OR",
                    fontSize = 11.sp,
                    color =
                        LoginColors.TextSecondary,
                    modifier =
                        Modifier.padding(
                            horizontal = 12.dp
                        )
                )

                HorizontalDivider(
                    modifier =
                        Modifier.weight(1f),
                    color =
                        LoginColors.Border
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /* GOOGLE */

            OutlinedButton(
                onClick = {
                    onGoogleLogin(selectedRole)
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape =
                    RoundedCornerShape(12.dp),
                border =
                    BorderStroke(
                        1.dp,
                        LoginColors.Border
                    )
            ) {

                Text(
                    text = "G",
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        LoginColors.TextDark
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Google",
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        LoginColors.TextDark
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            /* GUEST + REGISTER */

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick =
                        onGuestLogin,
                    enabled = !loading,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape =
                        RoundedCornerShape(11.dp),
                    border =
                        BorderStroke(
                            1.dp,
                            LoginColors.Border
                        )
                ) {

                    Text(
                        text = "Guest",
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            LoginColors.GradientStart
                    )
                }

                OutlinedButton(
                    onClick =
                        onRegisterClick,
                    enabled = !loading,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape =
                        RoundedCornerShape(11.dp),
                    border =
                        BorderStroke(
                            1.dp,
                            LoginColors.GradientStart
                        )
                ) {

                    Text(
                        text = "Sign Up",
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Medium,
                        color =
                            LoginColors.GradientStart
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            /* BACK */

            TextButton(
                onClick = onBack,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(14.dp),
                    tint =
                        LoginColors.TextSecondary
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = "Back",
                    fontSize = 12.sp,
                    color =
                        LoginColors.TextSecondary
                )
            }
        }
    }
}


/* ============================================================
   ROLE CHIP
   ============================================================ */

@Composable
private fun LoginRoleChip(
    modifier: Modifier,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {

    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier.height(36.dp),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight =
                    if (selected)
                        FontWeight.Bold
                    else
                        FontWeight.Normal
            )
        },
        leadingIcon = {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier =
                    Modifier.size(15.dp)
            )
        },
        shape =
            RoundedCornerShape(10.dp),
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor =
                    LoginColors.ChipBackground,
                labelColor =
                    LoginColors.TextSecondary,
                iconColor =
                    LoginColors.TextSecondary,
                selectedContainerColor =
                    LoginColors.GradientStart
                        .copy(alpha = 0.12f),
                selectedLabelColor =
                    LoginColors.GradientStart,
                selectedLeadingIconColor =
                    LoginColors.GradientStart
            )
    )
}


/* ============================================================
   ERROR
   ============================================================ */

@Composable
private fun ErrorMessage(
    message: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(8.dp)
            )
            .background(
                LoginColors.ErrorBackground
            )
            .padding(10.dp),
        verticalAlignment =
            Alignment.Top
    ) {

        Text(
            text = "!",
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    LoginColors.Error
                        .copy(alpha = 0.12f)
                )
                .padding(top = 1.dp),
            color = LoginColors.Error,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(
            modifier = Modifier.width(6.dp)
        )

        Text(
            text = message,
            color = LoginColors.Error,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
