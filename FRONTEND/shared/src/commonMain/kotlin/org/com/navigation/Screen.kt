package org.com.navigation
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Splash : Screen()

    @Serializable
    data object Home : Screen()

    @Serializable
    data class Details(val id: String) : Screen()

    @Serializable
    data object Settings : Screen()
}