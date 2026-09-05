package org.com.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.com.model.*
import kotlin.js.ExperimentalWasmJsInterop
import org.w3c.dom.events.Event
import org.com.i18n.*
import org.com.network.ApiClient


/*
 * ============================================================
 * MAP CONTAINER
 * ============================================================
 */

private fun ensureMapContainer() {

    val existing =
        document.getElementById(
            "google-map-container"
        )

    if (existing != null) {

        existing.setAttribute(
            "style",
            """
            position:fixed;
            top:0;
            left:0;
            width:100vw;
            height:100vh;
            display:block;
            visibility:visible;
            opacity:1;
            touch-action:none;
            z-index:1;
            """.trimIndent()
        )

        return
    }

    val container =
        document.createElement(
            "div"
        )

    container.id =
        "google-map-container"

    container.setAttribute(
        "style",
        """
        position:fixed;
        top:0;
        left:0;
        width:100vw;
        height:100vh;
        display:block;
        visibility:visible;
        opacity:1;
        touch-action:none;
        z-index:1;
        """.trimIndent()
    )

    document.body?.appendChild(
        container
    )
}


@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { if (typeof window.roomifyShowMap === 'function') { window.roomifyShowMap(); } }")
private external fun showMapLayer()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { var map = document.getElementById('google-map-container'); if (map) { map.style.display = 'none'; map.style.visibility = 'hidden'; map.style.opacity = '0'; map.style.zIndex = '-1'; console.log('Roomify: Map layer hidden'); } }")
private external fun hideMapLayer()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { var map = document.getElementById('google-map-container'); if (map) { map.style.display = 'block'; map.style.visibility = 'visible'; map.style.opacity = '1'; map.style.zIndex = '1'; console.log('Roomify: Map layer shown'); } }")
private external fun showMapLayerDirect()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { return typeof window.google !== 'undefined' && typeof window.google.maps !== 'undefined'; }")
private external fun googleMapsReady(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { return typeof window.roomifyMap !== 'undefined' && window.roomifyMap !== null; }")
private external fun roomifyMapReady(): Boolean

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { if (typeof window.roomifyCreateMap === 'function') { window.roomifyCreateMap(); } }")
private external fun initializeMap()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { if (typeof window.roomifyResizeMap === 'function') { window.roomifyResizeMap(); } else if (window.roomifyMap && window.google && window.google.maps) { window.google.maps.event.trigger(window.roomifyMap, 'resize'); } }")
private external fun triggerMapResize()

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(roomsJson) => { if (typeof window.roomifyUpdateMarkers === 'function') { window.roomifyUpdateMarkers(roomsJson); } }")
private external fun updateMarkers(roomsJson: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(latitude, longitude) => { if (typeof window.roomifyMoveToRoom === 'function') { window.roomifyMoveToRoom(latitude, longitude); } }")
private external fun moveToRoom(latitude: Double, longitude: Double)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(event) => { return event.detail ? String(event.detail) : null; }")
private external fun getEventDetail(event: Event): String?

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(role, name, email, initials, profileImage) => { if (typeof window.roomifyUpdateUser === 'function') { window.roomifyUpdateUser(role, name, email, initials, profileImage); } }")
private external fun updateSidebarUser(role: String?, name: String?, email: String?, initials: String?, profileImage: String?)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(localizationJson) => { if (typeof window.roomifyUpdateLocalization === 'function') { window.roomifyUpdateLocalization(localizationJson); } }")
private external fun updateSidebarLocalization(localizationJson: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(originLat, originLng, destLat, destLng) => { if (typeof window.showRoute === 'function') { window.showRoute(originLat, originLng, destLat, destLng); } }")
private external fun showRoute(originLat: Double, originLng: Double, destLat: Double, destLng: Double)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { if (typeof window.clearRoute === 'function') { window.clearRoute(); } }")
private external fun clearRoute()


/*
 * ============================================================
 * GLOBAL NAVIGATION LISTENER
 * ============================================================
 */

private var globalNavigationListenerRegistered = false

private fun registerGlobalNavigationListener(
    onNavigate: (String) -> Unit
) {
    if (globalNavigationListenerRegistered) {
        println("Roomify: Global navigation listener already registered")
        return
    }

    println("Roomify: Registering GLOBAL navigation listener")

    val listener: (Event) -> Unit = { event ->
        val destination = getEventDetail(event)
        println("Roomify: GLOBAL navigation - event received: $destination")

        if (!destination.isNullOrBlank()) {
            println("Roomify: GLOBAL navigation - navigating to: $destination")

            // IMPORTANT: Hide map when navigating away from map/explore
            if (destination == "map" || destination == "explore") {
                println("Roomify: Showing map for map/explore")
                showMapLayerDirect()
            } else {
                println("Roomify: Hiding map for $destination")
                hideMapLayer()
            }

            onNavigate(destination)
        }
    }

    document.addEventListener(
        "roomifySidebarNavigation",
        listener
    )

    globalNavigationListenerRegistered = true

    println("Roomify: Global navigation listener registered successfully")
}


/*
 * ============================================================
 * WEB MAP CONTENT
 * ============================================================
 */

@Composable
actual fun MapContent(
    rooms: List<Room>,
    selectedRoom: Room?,
    authState: org.com.auth.AuthState,
    routingDestination: Room?,
    onClearRoute: () -> Unit,
    onRoomSelected: (Room) -> Unit,
    onRoomCleared: () -> Unit,
    onViewProperty: (Room) -> Unit,
    onNavigate: (String) -> Unit
) {
    val strings = LocalRoomifyStrings.current
    val localizationManager = LocalLocalizationManager.current

    /*
     * ========================================================
     * SYNC LOCALIZATION TO JS
     * ========================================================
     */

    LaunchedEffect(strings) {
        val json = """
            {
                "explore": "${strings.explore}",
                "dashboard": "${strings.dashboard}",
                "savedRooms": "${strings.savedRooms}",
                "myBookings": "${strings.myBookings}",
                "messages": "${strings.messages}",
                "ownerDashboard": "${strings.ownerDashboard}",
                "postARoom": "${strings.postARoom}",
                "adminPanel": "${strings.adminPanel}",
                "account": "${strings.account}",
                "search": "${strings.search}",
                "filters": "${strings.filters}",
                "profile": "${strings.profile}",
                "logout": "${strings.logout}",
                "loginRegister": "${strings.loginRegister}",
                "searchPlaceholder": "${strings.searchPlaceholder}",
                "mySearches": "${strings.mySearches}",
                "language": "${strings.language}",
                "languageCode": "${localizationManager.currentLanguage.code}"
            }
        """.trimIndent()
        updateSidebarLocalization(json)
    }

    /*
     * ========================================================
     * HANDLE LANGUAGE CHANGE FROM JS
     * ========================================================
     */

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = { event ->
            val langCode = getEventDetail(event)
            if (langCode != null) {
                localizationManager.changeLanguage(Language.fromCode(langCode))
            }
        }

        document.addEventListener("roomifyLanguageChange", listener)
        onDispose {
            document.removeEventListener("roomifyLanguageChange", listener)
        }
    }

    /*
     * ========================================================
     * UPDATE SIDEBAR USER STATE
     * ========================================================
     */

    LaunchedEffect(authState) {
        val user = (authState as? org.com.auth.AuthState.Authenticated)?.user
        if (user != null) {
            val profileImg = if (user.profileImage.isNullOrBlank()) null 
                             else if (user.profileImage!!.startsWith("http")) user.profileImage 
                             else "${ApiClient.MEDIA_BASE_URL}${if (user.profileImage!!.startsWith("/")) "" else "/"}${user.profileImage}"
            
            updateSidebarUser(
                role = user.role.uppercase(),
                name = user.name,
                email = user.email,
                initials = user.initials,
                profileImage = profileImg
            )
        } else {
            updateSidebarUser(null, null, null, null, null)
        }
    }

    LaunchedEffect(routingDestination?.id) {
        if (routingDestination == null) {
            clearRoute()
            return@LaunchedEffect
        }

        val dest = routingDestination
        if (dest.latitude != 0.0 && dest.longitude != 0.0) {
            // In a real app we'd get the user's current location.
            // For now, use a default starting point (Dar es Salaam center)
            showRoute(-6.7924, 39.2083, dest.latitude, dest.longitude)
        }
    }

    // The map DOM node outlives Compose navigation.  It may have been hidden
    // by the login screen, so reveal it as soon as the map composable enters.
    LaunchedEffect(Unit) {
        println("Roomify: MapContent rendering - showing map")
        ensureMapContainer()
        showMapLayer()
        delay(100)
        triggerMapResize()
        println("Roomify: Map layer shown")
    }

    /*
     * ========================================================
     * REGISTER GLOBAL NAVIGATION LISTENER
     * ========================================================
     */

    LaunchedEffect(Unit) {
        registerGlobalNavigationListener(onNavigate)
    }


    /*
     * ========================================================
     * INITIALIZE MAP
     * ========================================================
     */

    LaunchedEffect(Unit) {

        println("Roomify: MapContent initialized")

        ensureMapContainer()

        repeat(40) {

            if (
                googleMapsReady()
            ) {

                println("Roomify: Google Maps ready")

                if (
                    !roomifyMapReady()
                ) {

                    println("Roomify: Creating Google Map")

                    initializeMap()

                    delay(300)
                }

                showMapLayer()

                delay(100)

                triggerMapResize()

                println("Roomify: Map layer displayed")

                return@LaunchedEffect
            }

            delay(250)
        }

        println("Roomify: Google Maps was not ready")
    }


    /*
     * ========================================================
     * UPDATE MARKERS
     * ========================================================
     */

    LaunchedEffect(rooms) {

        // Keep the persistent DOM layer visible while new room markers load.
        ensureMapContainer()
        showMapLayer()
        delay(100)
        triggerMapResize()

        if (
            rooms.isEmpty()
        ) {

            println("Roomify: no rooms available yet")

            return@LaunchedEffect
        }

        repeat(40) {

            if (
                !roomifyMapReady()
            ) {

                if (
                    googleMapsReady()
                ) {

                    initializeMap()
                }

                delay(250)

            } else {

                val roomsJson =
                    rooms.joinToString(
                        separator = ",",
                        prefix = "[",
                        postfix = "]"
                    ) { room ->

                        val title =
                            (
                                    room.title
                                        ?: "Room"
                                    )
                                .replace(
                                    "\\",
                                    "\\\\"
                                )
                                .replace(
                                    "\"",
                                    "\\\""
                                )
                                .replace(
                                    "\n",
                                    "\\n"
                                )
                                .replace(
                                    "\r",
                                    "\\r"
                                )

                        val status =
                            room.status
                                .replace(
                                    "\\",
                                    "\\\\"
                                )
                                .replace(
                                    "\"",
                                    "\\\""
                                )
                                .replace(
                                    "\n",
                                    "\\n"
                                )
                                .replace(
                                    "\r",
                                    "\\r"
                                )

                        val address =
                            (
                                    room.address
                                        ?: ""
                                    )
                                .replace(
                                    "\\",
                                    "\\\\"
                                )
                                .replace(
                                    "\"",
                                    "\\\""
                                )
                                .replace(
                                    "\n",
                                    "\\n"
                                )
                                .replace(
                                    "\r",
                                    "\\r"
                                )

                        val propertyType =
                            (
                                    room.propertyType
                                        ?: ""
                                    )
                                .replace(
                                    "\\",
                                    "\\\\"
                                )
                                .replace(
                                    "\"",
                                    "\\\""
                                )
                                .replace(
                                    "\n",
                                    "\\n"
                                )
                                .replace(
                                    "\r",
                                    "\\r"
                                )

                        val roomsCount =
                            room.roomsCount

                        val bathroomsCount =
                            room.bathroomsCount

                        val area =
                            room.area

                        """
                        {
                            "id":"${room.id}",
                            "lat":${room.latitude},
                            "lng":${room.longitude},
                            "title":"$title",
                            "status":"$status",
                            "price":${room.price},
                            "address":"$address",
                            "propertyType":"$propertyType",
                            "roomsCount":$roomsCount,
                            "bathroomsCount":$bathroomsCount,
                            "area":$area,
                            "image":"${room.firstImageUrl ?: ""}"
                        }
                        """.trimIndent()
                    }

                println("Roomify: sending ${rooms.size} rooms to map")

                updateMarkers(
                    roomsJson
                )

                showMapLayer()

                delay(100)

                triggerMapResize()

                return@LaunchedEffect
            }
        }

        println("Roomify: map was not ready for markers")
    }


    /*
     * ========================================================
     * MARKER CLICK EVENT
     * ========================================================
     */

    DisposableEffect(rooms) {

        val listener: (Event) -> Unit =
            { event ->

                val roomId =
                    getEventDetail(
                        event
                    )

                println("Roomify: marker interaction: $roomId")

                if (
                    !roomId.isNullOrBlank()
                ) {

                    val room =
                        rooms.find { candidate ->

                            candidate.id
                                ?.toString() ==
                                    roomId
                        }

                    room?.let {

                        println("Roomify: selecting room ${it.id}")

                        onRoomSelected(
                            it
                        )
                    }
                }
            }

        document.addEventListener(
            "roomMarkerClicked",
            listener
        )

        onDispose {

            document.removeEventListener(
                "roomMarkerClicked",
                listener
            )
        }
    }


    /*
     * ========================================================
     * VIEW DETAILS EVENT
     * ========================================================
     */

    DisposableEffect(rooms) {

        val listener: (Event) -> Unit =
            listener@{ event ->

                val roomId =
                    getEventDetail(
                        event
                    )

                println("Roomify: View Details event received: $roomId")

                if (
                    roomId.isNullOrBlank()
                ) {

                    println("Roomify: View Details event has no room ID")

                    return@listener
                }

                val room =
                    rooms.find { candidate ->

                        candidate.id
                            ?.toString() ==
                                roomId
                    }

                if (
                    room == null
                ) {

                    println("Roomify: View Details room not found: $roomId")

                    return@listener
                }

                println("Roomify: View Details -> room ${room.id}")

                // Trigger navigation first to ensure instant transition
                onViewProperty(
                    room
                )

                // Then hide the persistent DOM map
                hideMapLayer()
            }

        document.addEventListener(
            "roomViewDetails",
            listener
        )

        onDispose {

            document.removeEventListener(
                "roomViewDetails",
                listener
            )
        }
    }


    /*
     * ========================================================
     * MOVE TO SELECTED ROOM
     * ========================================================
     */

    LaunchedEffect(
        selectedRoom?.id
    ) {

        selectedRoom?.let { room ->

            if (
                room.latitude != 0.0 &&
                room.longitude != 0.0
            ) {

                repeat(20) {

                    if (
                        roomifyMapReady()
                    ) {

                        moveToRoom(
                            room.latitude,
                            room.longitude
                        )

                        return@LaunchedEffect
                    }

                    delay(250)
                }
            }
        }
    }


    /*
     * ========================================================
     * CLEANUP
     * ========================================================
     */

    DisposableEffect(Unit) {

        onDispose {

            println("Roomify: MapContent disposed")
        }
    }


    /*
     * ========================================================
     * TRANSPARENT COMPOSE LAYER
     * ========================================================
     */

    Box(
        modifier =
            Modifier.fillMaxSize()
    )
}
