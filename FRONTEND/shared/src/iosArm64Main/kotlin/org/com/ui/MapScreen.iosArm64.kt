package org.com.ui

import androidx.compose.runtime.Composable

@Composable
actual fun MapContent(
    rooms: List<org.com.model.Room>,
    selectedRoom: org.com.model.Room?,
    authState: org.com.auth.AuthState,
    routingDestination: org.com.model.Room?,
    mapDetailLevel: String,
    currentStatusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    onMapDetailChange: (org.com.viewmodel.MapDetail) -> Unit,
    onClearRoute: () -> Unit,
    onRoomSelected: (org.com.model.Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (org.com.model.Room) -> Unit,
    onNavigate: (String) -> Unit
) {
}
