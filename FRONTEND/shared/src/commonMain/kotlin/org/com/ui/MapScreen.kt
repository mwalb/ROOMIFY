package org.com.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.com.model.Room

@Composable
fun MapScreen(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onClearSelection: () -> Unit,
    onViewProperty: (Room) -> Unit,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {

    LaunchedEffect(selectedRoom?.id) {
        println(
            "MapScreen: selectedRoom = ${selectedRoom?.id}"
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        MapContent(
            rooms = rooms,
            selectedRoom = selectedRoom,
            authState = authState,
            routingDestination = routingDestination,
            onClearRoute = onClearRoute,
            onRoomSelected = { room ->

                println(
                    "MapScreen: onRoomSelected -> ${room.id}"
                )

                onRoomSelected(room)
            },

            onRoomCleared = {

                println(
                    "MapScreen: onRoomCleared"
                )

                onClearSelection()
            },

            onViewProperty = { room ->

                println(
                    "MapScreen: onViewProperty -> ${room.id}"
                )

                onViewProperty(room)
            },

            onNavigate = onNavigate
        )
    }
}