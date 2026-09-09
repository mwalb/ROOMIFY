package org.com.ui

import androidx.compose.runtime.Composable
import org.com.model.Room

import org.com.viewmodel.MapDetail

@Composable
expect fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    mapDetailLevel: String = "Standard",
    currentStatusFilter: String = "ALL",
    onStatusFilterChange: (String) -> Unit = {},
    onMapDetailChange: (MapDetail) -> Unit = {},
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
)
