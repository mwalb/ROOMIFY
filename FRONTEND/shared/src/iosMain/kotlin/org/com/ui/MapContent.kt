package org.com.ui

import androidx.compose.runtime.Composable
import org.com.model.Room

import org.com.viewmodel.MapDetail

@Composable
actual fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    mapDetailLevel: String,
    currentStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onMapDetailChange: (MapDetail) -> Unit,
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    // Placeholder for iOS
}
