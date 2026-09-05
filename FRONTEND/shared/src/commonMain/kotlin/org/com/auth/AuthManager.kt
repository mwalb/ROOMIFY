package org.com.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.com.model.GoogleLoginRequest
import org.com.model.LoginRequest
import org.com.model.RegisterRequest
import org.com.model.User
import org.com.network.ApiClient
import org.com.network.RoomifyApi

class AuthManager {

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var currentSession: UserSession? = null

    fun getSession(): UserSession? = currentSession

    // ============================================================
    // LOGIN
    // ============================================================

    suspend fun login(email: String, password: String, role: String = "tenant") {
        _authState.value = AuthState.Loading

        try {
            println("AuthManager: Logging in $email as $role")
            val request = LoginRequest(
                email = email,
                password = password,
                role = role.lowercase()
            )
            val response = RoomifyApi.login(request)
            handleResponse(response)

        } catch (e: Exception) {
            println("AuthManager: Login error: ${e.message}")
            _authState.value = AuthState.Error(getUserFriendlyError(e.message ?: "Login failed"))
        }
    }

    // ============================================================
    // REGISTER
    // ============================================================

    suspend fun register(request: RegisterRequest) {
        _authState.value = AuthState.Loading

        try {
            println("AuthManager: Registering ${request.email} as ${request.role}")
            val response = RoomifyApi.register(request)
            handleResponse(response)

        } catch (e: Exception) {
            println("AuthManager: Register error: ${e.message}")
            _authState.value = AuthState.Error(getUserFriendlyError(e.message ?: "Registration failed"))
        }
    }

    // ============================================================
    // GUEST LOGIN
    // ============================================================

    suspend fun guestLogin() {
        _authState.value = AuthState.Loading

        try {
            println("AuthManager: Guest login")
            val response = RoomifyApi.guestLogin()
            handleResponse(response)

        } catch (e: Exception) {
            println("AuthManager: Guest login error: ${e.message}")
            _authState.value = AuthState.Error("Guest login failed. Please try again.")
        }
    }

    // ============================================================
    // GOOGLE LOGIN
    // ============================================================

    suspend fun googleLogin(idToken: String) {
        _authState.value = AuthState.Loading

        try {
            println("AuthManager: Google login started")
            val request = GoogleLoginRequest(idToken = idToken)
            val response = RoomifyApi.googleLogin(request)
            handleResponse(response)

        } catch (e: Exception) {
            println("AuthManager: Google login error: ${e.message}")
            _authState.value = AuthState.Error("Google login failed. Please try again.")
        }
    }

    // ============================================================
    // GOOGLE REGISTER
    // ============================================================

    suspend fun googleRegister(idToken: String, role: String) {
        _authState.value = AuthState.Loading

        try {
            println("AuthManager: Google register started")
            val response = RoomifyApi.googleRegister(idToken, role)
            handleResponse(response)

        } catch (e: Exception) {
            println("AuthManager: Google register error: ${e.message}")
            _authState.value = AuthState.Error("Google registration failed. Please try again.")
        }
    }

    // ============================================================
    // LOGOUT
    // ============================================================

    suspend fun logout() {
        try {
            RoomifyApi.logout()
        } catch (e: Exception) {
            println("AuthManager: Logout error: ${e.message}")
        } finally {
            ApiClient.clearToken()
            currentSession = null
            _authState.value = AuthState.LoggedOut
            println("AuthManager: Logged out")
        }
    }

    // ============================================================
    // RESTORE SESSION
    // ============================================================

    suspend fun restoreSession() {
        val token = ApiClient.getToken()

        if (token.isNullOrBlank()) {
            println("AuthManager: No token to restore")
            _authState.value = AuthState.LoggedOut
            return
        }

        _authState.value = AuthState.Loading

        try {
            val response = RoomifyApi.getCurrentUser()

            if (response.success && response.user != null && !response.token.isNullOrBlank()) {
                val user = response.user!!
                val token = response.token!!
                ApiClient.setToken(token)
                currentSession = UserSession(user = user, token = token)
                _authState.value = AuthState.Authenticated(user = user, token = token)
                println("AuthManager: Session restored for ${user.name}")
            } else {
                ApiClient.clearToken()
                currentSession = null
                _authState.value = AuthState.LoggedOut
            }

        } catch (e: Exception) {
            println("AuthManager: Session restore error: ${e.message}")
            ApiClient.clearToken()
            currentSession = null
            _authState.value = AuthState.LoggedOut
        }
    }

    // ============================================================
    // CLEAR ERROR - ADD THIS!
    // ============================================================

    /**
     * Clear any error state and return to LoggedOut.
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.LoggedOut
        }
    }

    /**
     * Update the current user state locally after a profile change.
     */
    fun updateCurrentUser(updatedUser: User) {
        val current = _authState.value
        if (current is AuthState.Authenticated) {
            _authState.value = current.copy(user = updatedUser)
            currentSession = currentSession?.copy(user = updatedUser)
            println("AuthManager: Current user updated to ${updatedUser.name}")
        }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private fun handleResponse(response: org.com.model.AuthResponse) {
        if (!response.success) {
            val errorMsg = getUserFriendlyError(response.message.ifBlank { "Authentication failed" })
            _authState.value = AuthState.Error(errorMsg)
            return
        }

        val token = response.token
        if (token.isNullOrBlank()) {
            _authState.value = AuthState.Error("Authentication succeeded but no token was returned")
            return
        }

        val user = response.user
        if (user == null) {
            _authState.value = AuthState.Error("Authentication succeeded but no user was returned")
            return
        }

        ApiClient.setToken(token)
        currentSession = UserSession(user = user, token = token)
        _authState.value = AuthState.Authenticated(user = user, token = token)
        println("AuthManager: ✅ Authenticated as ${user.name} (${user.role})")
    }

    private fun getUserFriendlyError(message: String): String {
        if (message.isBlank()) return "Something went wrong. Please try again."

        return when {
            message.contains("Invalid credentials", ignoreCase = true) ||
                    message.contains("Invalid email or password", ignoreCase = true) ||
                    message.contains("Unauthorized", ignoreCase = true) ->
                "Invalid email or password. Please try again."

            message.contains("User not found", ignoreCase = true) ->
                "No account found with this email address."

            message.contains("Email already", ignoreCase = true) ->
                "This email is already registered. Please login instead."

            message.contains("Password", ignoreCase = true) && message.contains("short", ignoreCase = true) ->
                "Password must be at least 6 characters long."

            message.contains("Network", ignoreCase = true) ||
                    message.contains("timeout", ignoreCase = true) ||
                    message.contains("timed out", ignoreCase = true) ->
                "Connection error. Please check your internet and try again."

            message.contains("Server", ignoreCase = true) ||
                    message.contains("500", ignoreCase = true) ->
                "Server error. Please try again later."

            message.contains("Fail to fetch", ignoreCase = true) ||
                    message.contains("Connection refused", ignoreCase = true) ->
                "Cannot connect to the server. Please check your internet connection."

            else -> message
        }
    }
}

// ============================================================
// USER SESSION - ONLY ONE DEFINITION
// ============================================================

data class UserSession(
    val user: User,
    val token: String
) {
    fun isLoggedIn(): Boolean = token.isNotBlank()
    fun getRole(): String = user.role
    fun getUserId(): Long = user.id
    fun getUserName(): String = user.name
    fun getUserEmail(): String = user.email
}
