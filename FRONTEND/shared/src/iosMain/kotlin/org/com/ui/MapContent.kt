package org.com.ui

import androidx.compose.runtime.Composable
import org.com.model.Room

@Composable
actual fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    // Placeholder for iOS
}
