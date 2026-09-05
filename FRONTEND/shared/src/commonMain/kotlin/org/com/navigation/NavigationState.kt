package org.com.navigation

import org.com.model.Room

sealed class NavigationState {

    data object Map : NavigationState()

    data class Login(
        val requestedRoom: Room? = null
    ) : NavigationState()

    data class Register(
        val requestedRoom: Room? = null
    ) : NavigationState()

    data class PropertyDetails(
        val room: Room
    ) : NavigationState()
}