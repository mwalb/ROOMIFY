package org.com.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGoogleSignInLauncher(): GoogleSignInLauncher {
    return remember {
        GoogleSignInLauncher { onResult ->
            println("Google Sign-In is not implemented on Web in this build")
            onResult(null)
        }
    }
}
