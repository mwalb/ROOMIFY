package org.com.auth

import org.com.model.User

/**
 * Authentication State - Simple and Easy to Understand
 *
 * Think of this as the "status" of the user:
 * - Loading: Waiting for server response (show spinner)
 * - LoggedOut: User is not logged in (show LoginScreen)
 * - Authenticated: User is logged in (show home screen)
 * - Error: Something went wrong (show error message)
 */
sealed class AuthState {

    /** Waiting for server - show loading spinner */
    data object Loading : AuthState()

    /** User is not logged in - show LoginScreen */
    data object LoggedOut : AuthState()

    /** User is logged in - show home screen */
    data class Authenticated(
        val user: User,      // User info (name, email, role, etc.)
        val token: String    // JWT token for API calls
    ) : AuthState()

    /** Error occurred - show error message */
    data class Error(
        val message: String  // User-friendly error message
    ) : AuthState()
}

/**
 * Convert error message to something the user can understand.
 *
 * Example: "Invalid credentials" → "Invalid email or password. Please try again."
 */
fun AuthState.getUserFriendlyMessage(): String? {
    if (this !is AuthState.Error) return null

    val msg = this.message
    if (msg.isBlank()) return "Something went wrong. Please try again."

    return when {
        // Login errors
        msg.contains("Invalid credentials", ignoreCase = true) ||
                msg.contains("Invalid email or password", ignoreCase = true) ||
                msg.contains("Unauthorized", ignoreCase = true) ->
            "Invalid email or password. Please try again."

        msg.contains("User not found", ignoreCase = true) ->
            "No account found with this email address."

        // Registration errors
        msg.contains("Email already", ignoreCase = true) ->
            "This email is already registered. Please login instead."

        msg.contains("Password", ignoreCase = true) && msg.contains("short", ignoreCase = true) ->
            "Password must be at least 6 characters long."

        msg.contains("Invalid email", ignoreCase = true) ->
            "Please enter a valid email address."

        msg.contains("Phone", ignoreCase = true) && msg.contains("required", ignoreCase = true) ->
            "Phone number is required."

        // Network errors
        msg.contains("Network", ignoreCase = true) ||
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("timed out", ignoreCase = true) ->
            "Connection error. Please check your internet and try again."

        msg.contains("Server", ignoreCase = true) ||
                msg.contains("500", ignoreCase = true) ->
            "Server error. Please try again later."

        msg.contains("Fail to fetch", ignoreCase = true) ||
                msg.contains("Connection refused", ignoreCase = true) ->
            "Cannot connect to the server. Please check your internet connection."

        // Default - show original message
        else -> msg
    }
}