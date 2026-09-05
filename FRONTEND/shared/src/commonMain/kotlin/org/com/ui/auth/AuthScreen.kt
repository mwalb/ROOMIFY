package org.com.ui.auth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.com.auth.AuthManager
import org.com.auth.AuthState

private enum class AuthPage {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    authManager: AuthManager,
    onAuthenticated: () -> Unit,
    onBack: () -> Unit
) {

    var page by remember {
        mutableStateOf(
            AuthPage.LOGIN
        )
    }

    val authState by
    authManager.authState.collectAsState()

    val scope =
        rememberCoroutineScope()


    /*
     * ============================================================
     * AUTHENTICATED
     * ============================================================
     *
     * App.kt is responsible for restoring the session.
     *
     * Once authentication succeeds, App.kt will automatically
     * switch from AuthScreen to PropertyDetailsScreen because
     * roomToView is still preserved.
     */

    androidx.compose.runtime.LaunchedEffect(
        authState
    ) {

        if (
            authState is AuthState.Authenticated
        ) {

            onAuthenticated()
        }
    }


    /*
     * ============================================================
     * PAGE ROUTING
     * ============================================================
     */

    when (page) {

        /*
         * ========================================================
         * LOGIN
         * ========================================================
         */

        AuthPage.LOGIN -> {

            LoginScreen(

                authState =
                    authState,

                onLogin = { email, password, role ->

                    scope.launch {

                        authManager.login(
                            email = email,
                            password = password,
                            role = role
                        )
                    }
                },

                onGoogleLogin = { idToken ->

                    scope.launch {

                        authManager.googleLogin(
                            idToken = idToken
                        )
                    }
                },

                onRegisterClick = {

                    authManager.clearError()

                    page =
                        AuthPage.REGISTER
                },

                onBack = {

                    onBack()
                },

                onGuestLogin = {

                    scope.launch {

                        authManager.guestLogin()
                    }
                },

                onForgotPassword = {

                    /*
                     * Forgot-password implementation can be
                     * connected here later.
                     *
                     * For now the LoginScreen can expose the
                     * action without breaking the auth flow.
                     */

                    println(
                        "Roomify: Forgot password clicked"
                    )
                }
            )
        }


        /*
         * ========================================================
         * REGISTER
         * ========================================================
         */

        AuthPage.REGISTER -> {

            RegisterScreen(

                authState =
                    authState,

                onRegister = { request ->

                    scope.launch {

                        authManager.register(
                            request
                        )
                    }
                },

                onGoogleRegister = { idToken, role ->

                    scope.launch {

                        authManager.googleRegister(
                            idToken = idToken,
                            role = role
                        )
                    }
                },

                onLoginClick = {

                    authManager.clearError()

                    page =
                        AuthPage.LOGIN
                },

                onBack = {

                    authManager.clearError()

                    page =
                        AuthPage.LOGIN
                }
            )
        }
    }
}