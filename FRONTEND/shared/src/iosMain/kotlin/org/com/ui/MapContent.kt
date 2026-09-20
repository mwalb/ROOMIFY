package org.com.ui

import androidx.compose.runtime.Composable
import org.com.model.Room

@Composable
actual fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    currentStatusFilter: String,
    shouldFitBounds: Boolean,
    viewedRoomIds: Set<Long>,
    savedRoomIds: Set<Long>,
    onStatusFilterChange: (String) -> Unit,
    onFiltersChange: (type: String?, area: String?, maxPrice: Double?, status: String?) -> Unit,
    onFitBoundsHandled: () -> Unit,
    onClearRoute: () -> Unit,
    onMenuClick: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    // Placeholder for iOS
}
