package org.com.ui

import androidx.compose.runtime.Composable
import org.com.model.Room

@Composable
expect fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    currentStatusFilter: String = "ALL",
    shouldFitBounds: Boolean = false,
    onStatusFilterChange: (String) -> Unit = {},
    onFitBoundsHandled: () -> Unit = {},
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
)
