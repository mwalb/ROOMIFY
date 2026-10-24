package org.com.auth

import androidx.compose.runtime.Composable

fun interface GoogleSignInLauncher {
    fun launchGoogleSignIn(onResult: (idToken: String?) -> Unit)
}

@Composable
expect fun rememberGoogleSignInLauncher(): GoogleSignInLauncher
