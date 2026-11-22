package org.com.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.events.Event
import org.w3c.dom.MessageEvent
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsNumber
import kotlin.js.JsString

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(message) => console.log(message)")
external fun consoleLog(message: String)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => ({})")
external fun createJsObject(): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(obj, key, value) => { obj[key] = value; }")
external fun setJsProperty(obj: JsAny, key: String, value: JsAny?)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(str) => encodeURIComponent(str)")
external fun jsEncodeURIComponent(str: String): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(s) => s")
external fun toJsString(s: String): JsString

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(n) => n")
external fun toJsNumber(n: Double): JsNumber

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(n) => Number(n)")
external fun fromJsNumber(n: JsNumber): Double

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(onSuccess, onError) => { " +
    "if (navigator.geolocation) { " +
    "  navigator.geolocation.getCurrentPosition( " +
    "    (pos) => onSuccess({ lat: pos.coords.latitude, lng: pos.coords.longitude }), " +
    "    (err) => onError(err.message) " +
    "  ); " +
    "} else { onError('Geolocation not supported'); } " +
    "}")
private external fun jsGetCurrentLocation(onSuccess: (JsAny) -> Unit, onError: (JsAny) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
external interface JsLocation : JsAny {
    val lat: JsNumber
    val lng: JsNumber
}

@OptIn(ExperimentalWasmJsInterop::class)
external interface MapMessageData : JsAny {
    val action: JsString?
    val lat: JsNumber?
    val lng: JsNumber?
    val address: JsString?
    val formattedAddress: JsString?
    val placeId: JsString?
    val message: JsString?
    val resultsJson: JsString?
    val name: JsString?
}

@Composable
actual fun createMapController(
    apiKey: String?,
    onLocationSelected: (AddressResult) -> Unit,
    onError: (String) -> Unit
): MapController {
    return remember {
        WebMapController(
            apiKey = apiKey ?: "AIzaSyBLyHdlQHjeCKntbZSyoTyzK-J160-Xbvw",
            onLocationSelected = onLocationSelected,
            onError = onError
        )
    }
}

@Composable
@OptIn(ExperimentalWasmJsInterop::class)
actual fun PlatformMap(
    controller: MapController,
    modifier: Modifier,
    onMapClicked: ((Double, Double) -> Unit)?,
    onMapReady: (() -> Unit)?,
    isVisible: Boolean
) {
    val webController = controller as? WebMapController ?: return
    val mapContainerId = remember { "map_${window.performance.now()}" }
    var elementX by remember { mutableStateOf(0f) }
    var elementY by remember { mutableStateOf(0f) }
    var elementWidth by remember { mutableStateOf(0f) }
    var elementHeight by remember { mutableStateOf(0f) }

    val container = remember {
        document.createElement("div").unsafeCast<HTMLDivElement>().apply {
            id = mapContainerId
            style.position = "absolute"
            style.zIndex = "10" 
            style.backgroundColor = "#e5e3df"
            style.display = "none" 
        }
    }

    DisposableEffect(Unit) {
        val composeTarget = document.getElementById("ComposeTarget")
        composeTarget?.appendChild(container)
        
        loadMapHtml(container, webController, onMapClicked, onMapReady)

        onDispose {
            container.remove()
        }
    }

    SideEffect {
        container.style.left = "${elementX}px"
        container.style.top = "${elementY}px"
        container.style.width = "${elementWidth}px"
        container.style.height = "${elementHeight}px"
        if (elementWidth > 0 && elementHeight > 0 && isVisible) {
            container.style.display = "block"
        } else {
            container.style.display = "none"
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { layoutCoordinates ->
            val position = layoutCoordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
            elementX = position.x
            elementY = position.y
            elementWidth = layoutCoordinates.size.width.toFloat()
            elementHeight = layoutCoordinates.size.height.toFloat()
        }
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun loadMapHtml(
    container: HTMLDivElement,
    controller: WebMapController,
    onMapClicked: ((Double, Double) -> Unit)?,
    onMapReady: (() -> Unit)?
) {
    val iframe = document.createElement("iframe").unsafeCast<HTMLIFrameElement>().apply {
        style.width = "100%"
        style.height = "100%"
        style.border = "none"
        style.position = "absolute"
        style.top = "0"
        style.left = "0"
        style.zIndex = "1"
        style.backgroundColor = "#e5e3df"

        val htmlContent = buildMapHtml(controller.apiKey)
        val dataUrl = "data:text/html;charset=utf-8,${jsEncodeURIComponent(htmlContent)}"
        this.src = dataUrl
    }

    controller.iframe = iframe
    container.appendChild(iframe)

    val messageHandler: (Event) -> Unit = { event ->
        try {
            val messageEvent = event.unsafeCast<MessageEvent>()
            val data = messageEvent.data?.unsafeCast<MapMessageData>()

            if (data != null) {
                val action = data.action?.toString()
                when (action) {
                    "mapClick" -> {
                        val lat = data.lat?.let { fromJsNumber(it) } ?: 0.0
                        val lng = data.lng?.let { fromJsNumber(it) } ?: 0.0
                        controller.handleMapClick(lat, lng)
                        onMapClicked?.invoke(lat, lng)
                    }
                    "addressFound" -> {
                        val address = AddressResult(
                            address = data.name?.toString() ?: data.address?.toString() ?: "Unknown address",
                            latitude = data.lat?.let { fromJsNumber(it) } ?: 0.0,
                            longitude = data.lng?.let { fromJsNumber(it) } ?: 0.0,
                            formattedAddress = data.formattedAddress?.toString() ?: "Unknown address",
                            placeId = data.placeId?.toString()
                        )
                        controller._selectedAddress.value = address
                        controller._selectedLocation.value = LocationResult(
                            latitude = address.latitude,
                            longitude = address.longitude
                        )
                        controller.onLocationSelected(address)
                    }
                    "mapReady" -> {
                        onMapReady?.invoke()
                    }
                    "error" -> {
                        controller.onError(data.message?.toString() ?: "Unknown error")
                    }
                }
            }
        } catch (e: Exception) {
            consoleLog("Message parsing error: ${e.message}")
        }
    }

    window.addEventListener("message", messageHandler, false)
}

private fun buildMapHtml(apiKey: String): String {
    return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Map</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                html, body { width: 100%; height: 100%; overflow: hidden; background: #e5e3df; }
                #map { width: 100%; height: 100%; }
            </style>
            <script src="https://maps.googleapis.com/maps/api/js?key=$apiKey&libraries=places&loading=async"></script>
        </head>
        <body>
            <div id="map"></div>
            <script>
                let map;
                let marker;
                let geocoder;
                let infoWindow;
                let directionsService;
                let directionsRenderer;
                
                function initMap() {
                    const defaultLocation = { lat: -6.7924, lng: 39.2083 };
                    map = new google.maps.Map(document.getElementById('map'), {
                        center: defaultLocation,
                        zoom: 13,
                        zoomControl: true,
                        mapTypeControl: false,
                        streetViewControl: true,
                        fullscreenControl: true
                    });
                    geocoder = new google.maps.Geocoder();
                    infoWindow = new google.maps.InfoWindow();
                    directionsService = new google.maps.DirectionsService();
                    directionsRenderer = new google.maps.DirectionsRenderer({
                        map: map, polylineOptions: { strokeColor: '#1A237E', strokeWeight: 5 }
                    });
                    
                    map.addListener('click', function(e) {
                        const lat = e.latLng.lat();
                        const lng = e.latLng.lng();
                        setMarker(lat, lng, 'Selected Location');
                        reverseGeocode(lat, lng);
                        sendMessage('mapClick', { lat: lat, lng: lng });
                    });
                }
                
                function setMarker(lat, lng, title) {
                    if (marker) marker.setMap(null);
                    marker = new google.maps.Marker({
                        position: { lat: lat, lng: lng }, map: map,
                        title: title || 'Selected Location', draggable: true
                    });
                    map.setCenter({ lat: lat, lng: lng });
                }
                
                function reverseGeocode(lat, lng) {
                    geocoder.geocode({ location: { lat: lat, lng: lng } }, function(results, status) {
                        if (status === 'OK' && results[0]) {
                            const result = results[0];
                            const address = result.formatted_address;
                            let name = "";
                            
                            // Try to find a better name from address components
                            for (let component of result.address_components) {
                                if (component.types.includes('point_of_interest') || 
                                    component.types.includes('establishment') ||
                                    component.types.includes('premise')) {
                                    name = component.long_name;
                                    break;
                                }
                            }
                            if (!name) name = address.split(',')[0];

                            sendMessage('addressFound', { 
                                name: name,
                                address: address, 
                                formattedAddress: address, 
                                lat: lat, 
                                lng: lng, 
                                placeId: result.place_id 
                            });
                        }
                    });
                }
                
                function sendMessage(action, data) {
                    window.parent.postMessage({ action: action, ...data }, '*');
                }
                
                window.initMap = initMap;
                
                window.addEventListener('message', function(event) {
                    const data = event.data;
                    if (!data || !data.action) return;
                    switch(data.action) {
                        case 'setCenter': map.setCenter({ lat: data.lat, lng: data.lng }); break;
                        case 'searchAddress':
                            if (!geocoder) return;
                            geocoder.geocode({ address: data.query }, function(results, status) {
                                if (status === 'OK') {
                                    const mapped = results.map(r => {
                                        let name = "";
                                        for (let component of r.address_components) {
                                            if (component.types.includes('point_of_interest') || 
                                                component.types.includes('establishment')) {
                                                name = component.long_name;
                                                break;
                                            }
                                        }
                                        if (!name) name = r.formatted_address.split(',')[0];

                                        return {
                                            address: name,
                                            formattedAddress: r.formatted_address,
                                            latitude: r.geometry.location.lat(),
                                            longitude: r.geometry.location.lng(),
                                            placeId: r.place_id
                                        };
                                    });
                                    sendMessage('searchResults', { resultsJson: JSON.stringify(mapped) });
                                } else {
                                    sendMessage('searchResults', { resultsJson: '[]' });
                                }
                            });
                            break;
                        case 'reverseGeocode': reverseGeocode(data.lat, data.lng); break;
                    }
                });
            </script>
        </body>
        </html>
    """.trimIndent()
}


class WebMapController(
    val apiKey: String,
    val onLocationSelected: (AddressResult) -> Unit,
    val onError: (String) -> Unit
) : MapController {

    var iframe: HTMLIFrameElement? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    internal val _selectedLocation = MutableStateFlow<LocationResult?>(null)
    override val selectedLocation: MutableStateFlow<LocationResult?> = _selectedLocation

    internal val _selectedAddress = MutableStateFlow<AddressResult?>(null)
    override val selectedAddress: MutableStateFlow<AddressResult?> = _selectedAddress

    fun handleMapClick(lat: Double, lng: Double) {
        _selectedLocation.value = LocationResult(lat, lng)
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun searchAddress(query: String): List<AddressResult> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val listener = object : (Event) -> Unit {
                    override fun invoke(event: Event) {
                        val messageEvent = event.unsafeCast<MessageEvent>()
                        val data = messageEvent.data?.unsafeCast<MapMessageData>()
                        if (data != null && data.action?.toString() == "searchResults") {
                            window.removeEventListener("message", this)
                            val json = data.resultsJson?.toString() ?: "[]"
                            try {
                                val results = kotlinx.serialization.json.Json.decodeFromString<List<AddressResult>>(json)
                                continuation.resume(results)
                            } catch (e: Exception) {
                                continuation.resume(emptyList())
                            }
                        }
                    }
                }
                window.addEventListener("message", listener)
                val message = createJsObject()
                setJsProperty(message, "action", toJsString("searchAddress"))
                setJsProperty(message, "query", toJsString(query))
                iframe?.contentWindow?.postMessage(message, "*")
                scope.launch {
                    delay(5000)
                    if (continuation.isActive) {
                        window.removeEventListener("message", listener)
                        continuation.resume(emptyList())
                    }
                }
            } catch (e: Exception) { continuation.resumeWithException(e) }
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override suspend fun reverseGeocode(lat: Double, lng: Double): AddressResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val listener = object : (Event) -> Unit {
                    override fun invoke(event: Event) {
                        val messageEvent = event.unsafeCast<MessageEvent>()
                        val data = messageEvent.data?.unsafeCast<MapMessageData>()
                        if (data != null && data.action?.toString() == "addressFound") {
                            window.removeEventListener("message", this)
                            val result = AddressResult(
                                address = data.name?.toString() ?: data.address?.toString() ?: "Selected Location",
                                latitude = data.lat?.let { fromJsNumber(it) } ?: lat,
                                longitude = data.lng?.let { fromJsNumber(it) } ?: lng,
                                formattedAddress = data.formattedAddress?.toString() ?: "Selected Location"
                            )
                            continuation.resume(result)
                        }
                    }
                }
                window.addEventListener("message", listener)
                val message = createJsObject()
                setJsProperty(message, "action", toJsString("reverseGeocode"))
                setJsProperty(message, "lat", toJsNumber(lat))
                setJsProperty(message, "lng", toJsNumber(lng))
                iframe?.contentWindow?.postMessage(message, "*")
                scope.launch {
                    delay(3000)
                    if (continuation.isActive) {
                        window.removeEventListener("message", listener)
                        continuation.resume(AddressResult("Selected Location", lat, lng, "Selected Location"))
                    }
                }
            } catch (e: Exception) { continuation.resumeWithException(e) }
        }
    }

    override suspend fun getCurrentLocation(): LocationResult {
        return suspendCancellableCoroutine { continuation ->
            jsGetCurrentLocation(
                { pos ->
                    val loc = pos.unsafeCast<JsLocation>()
                    continuation.resume(LocationResult(fromJsNumber(loc.lat), fromJsNumber(loc.lng)))
                },
                { err -> continuation.resumeWithException(Exception(err.toString())) }
            )
        }
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    override fun setMapCenter(lat: Double, lng: Double) {
        val message = createJsObject()
        setJsProperty(message, "action", toJsString("setCenter"))
        setJsProperty(message, "lat", toJsNumber(lat))
        setJsProperty(message, "lng", toJsNumber(lng))
        iframe?.contentWindow?.postMessage(message, "*")
    }

    override fun setMarker(lat: Double, lng: Double, title: String?) {}
    override fun clearMarkers() {}
    override fun showRoute(origin: org.com.ui.LatLng, destination: org.com.ui.LatLng) {}
    override fun clearRoute() {}
}

@Composable
@OptIn(ExperimentalWasmJsInterop::class)
actual fun PlatformLocationMap(
    latitude: String?,
    longitude: String?,
    modifier: Modifier,
    initialSearch: String?,
    onLocationSelected: (AddressResult) -> Unit,
    onLocationConfirmed: (AddressResult) -> Unit,
    onDismiss: () -> Unit
) {
    val lat = latitude?.toDoubleOrNull() ?: -6.7924
    val lng = longitude?.toDoubleOrNull() ?: 39.2083
    val mapId = remember { "full_map_picker_${window.performance.now().toInt()}" }
    val safeInitialSearch = (initialSearch ?: "")
        .replace("&", "&amp;")
        .replace("\"", "&quot;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")

    val container = remember {
        document.createElement("div").unsafeCast<HTMLDivElement>().apply {
            id = mapId
            style.position = "fixed"
            style.top = "0"
            style.left = "0"
            style.width = "100vw"
            style.height = "100vh"
            style.zIndex = "30000"
            style.backgroundColor = "#F8FAFC"
            style.display = "flex"
            style.flexDirection = "column"
        }
    }

    DisposableEffect(Unit) {
        document.body?.appendChild(container)

        container.innerHTML = """
            <style>
                .pac-container {
                    z-index: 40000 !important;
                    border-radius: 12px !important;
                    box-shadow: 0 12px 28px rgba(20, 33, 112, 0.18) !important;
                    border: 1px solid #D5E0F0 !important;
                    font-family: 'Inter', sans-serif !important;
                    margin-top: 4px !important;
                }
                .pac-item {
                    padding: 10px 14px !important;
                    cursor: pointer !important;
                    font-size: 13px !important;
                }
                .pac-item:hover {
                    background-color: #EEF2FF !important;
                }
                .pac-item-query {
                    font-size: 14px !important;
                    font-weight: 700 !important;
                    color: #142170 !important;
                }
            </style>
            <div id="${mapId}_header" style="padding: 14px 18px; background: white; border-bottom: 1px solid #E2E8F0; display: flex; flex-direction: column; gap: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.04); z-index: 2;">
                <div style="display: flex; align-items: center; justify-content: space-between;">
                    <div>
                        <h3 style="margin: 0; font-family: 'Inter', sans-serif; font-size: 18px; font-weight: 800; color: #142170;">Select Property Location</h3>
                        <div style="font-family: 'Inter', sans-serif; font-size: 12px; color: #64748B;">Search by place name, choose from live suggestions, or drag the map pin</div>
                    </div>
                    <button id="${mapId}_close" style="background: #F1F5F9; border: none; width: 36px; height: 36px; border-radius: 50%; font-size: 22px; cursor: pointer; color: #334155; display: flex; align-items: center; justify-content: center;">&times;</button>
                </div>
                <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                    <input id="${mapId}_search_input" type="text" value="$safeInitialSearch" placeholder="Type place name (e.g. Mikocheni, Sinza, Mlimani City, Kariakoo)..." 
                        style="flex: 1; min-width: 220px; height: 44px; padding: 0 16px; border: 1.5px solid #CBD5E1; border-radius: 10px; font-family: 'Inter', sans-serif; font-size: 14px; outline: none; color: #0F172A; font-weight: 600;">
                    <button id="${mapId}_search_btn" 
                        style="height: 44px; padding: 0 20px; background: #142170; color: white; border: none; border-radius: 10px; font-weight: 800; font-size: 13px; cursor: pointer;">
                        Search Place
                    </button>
                    <button id="${mapId}_gps_btn" 
                        style="height: 44px; padding: 0 14px; background: #EEF2FF; color: #142170; border: 1.5px solid #C7D2FE; border-radius: 10px; font-weight: 800; font-size: 12px; cursor: pointer; display: flex; align-items: center; gap: 6px;">
                        📍 My GPS
                    </button>
                </div>
                <div id="${mapId}_search_status" style="font-family: 'Inter', sans-serif; font-size: 12px; font-weight: 600; color: #64748B; display: none;"></div>
            </div>
            <div id="${mapId}_canvas" style="flex: 1; background: #e5e3df;"></div>
            <div id="${mapId}_footer" style="padding: 18px 20px; background: white; box-shadow: 0 -4px 18px rgba(20,33,112,0.10); border-top: 1px solid #E2E8F0; z-index: 2;">
                <div style="font-family: 'Inter', sans-serif; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.6px; color: #64748B; margin-bottom: 4px;">Pinned Location Name & Address</div>
                <div id="${mapId}_address" style="font-family: 'Inter', sans-serif; font-size: 15px; font-weight: 800; min-height: 22px; line-height: 1.35; color: #0F172A;">
                    Click on the map or search a place name above...
                </div>
                <div id="${mapId}_coords" style="font-family: monospace; font-size: 12px; color: #475569; margin: 6px 0 14px 0;"></div>
                <button id="${mapId}_confirm" disabled 
                    style="width: 100%; height: 50px; background: #142170; color: white; border: none; border-radius: 12px; font-weight: 800; font-size: 14px; cursor: pointer; transition: opacity 0.2s;">
                    CONFIRM THIS LOCATION
                </button>
            </div>
        """.trimIndent()

        val script = document.createElement("script")
        script.textContent = """
            (function() {
                const mapCanvas = document.getElementById('${mapId}_canvas');
                const confirmBtn = document.getElementById('${mapId}_confirm');
                const addressText = document.getElementById('${mapId}_address');
                const coordsText = document.getElementById('${mapId}_coords');
                const closeBtn = document.getElementById('${mapId}_close');
                const searchInput = document.getElementById('${mapId}_search_input');
                const searchBtn = document.getElementById('${mapId}_search_btn');
                const gpsBtn = document.getElementById('${mapId}_gps_btn');
                const searchStatus = document.getElementById('${mapId}_search_status');

                let selectedPos = { lat: $lat, lng: $lng };
                let formattedAddress = "";
                let knownName = "";

                const map = new google.maps.Map(mapCanvas, {
                    center: selectedPos,
                    zoom: 15,
                    zoomControl: true,
                    mapTypeControl: false,
                    streetViewControl: false,
                    fullscreenControl: false,
                    gestureHandling: "greedy"
                });

                const marker = new google.maps.Marker({
                    position: selectedPos,
                    map: map,
                    draggable: true,
                    animation: google.maps.Animation.DROP
                });

                const geocoder = new google.maps.Geocoder();

                // Attach Google Places Autocomplete directly to the search input
                if (google.maps.places && typeof google.maps.places.Autocomplete === "function") {
                    const autocomplete = new google.maps.places.Autocomplete(searchInput, {
                        fields: ["name", "geometry", "formatted_address", "address_components", "place_id"]
                    });
                    autocomplete.bindTo("bounds", map);
                    autocomplete.addListener("place_changed", function() {
                        const place = autocomplete.getPlace();
                        if (!place || !place.geometry || !place.geometry.location) {
                            performSearch();
                            return;
                        }
                        if (place.geometry.viewport) {
                            map.fitBounds(place.geometry.viewport);
                        } else {
                            map.setCenter(place.geometry.location);
                            map.setZoom(17);
                        }
                        const displayTitle = place.name || (place.formatted_address ? place.formatted_address.split(",")[0] : searchInput.value.trim());
                        const fullAddr = place.formatted_address || displayTitle;
                        searchStatus.style.display = "none";
                        updateLocation(place.geometry.location, false, displayTitle, fullAddr);
                    });
                }

                function updateLocation(latLng, skipGeocode = false, providedName = null, providedFormattedAddress = null) {
                    selectedPos = latLng;
                    marker.setPosition(latLng);
                    confirmBtn.disabled = false;
                    confirmBtn.style.opacity = "1";

                    const latVal = typeof latLng.lat === 'function' ? latLng.lat() : latLng.lat;
                    const lngVal = typeof latLng.lng === 'function' ? latLng.lng() : latLng.lng;
                    coordsText.innerText = "Coordinates: " + latVal.toFixed(6) + ", " + lngVal.toFixed(6);

                    if (skipGeocode) return;

                    if (providedName) {
                        knownName = providedName;
                        formattedAddress = providedFormattedAddress || providedName;
                        const displayCombined = (formattedAddress && formattedAddress.toLowerCase().indexOf(knownName.toLowerCase()) === -1)
                            ? (knownName + ", " + formattedAddress)
                            : formattedAddress;
                        addressText.innerText = displayCombined;
                        searchInput.value = displayCombined;
                        confirmBtn.disabled = false;
                        confirmBtn.style.opacity = "1";
                        window.postMessage({
                            action: 'locationUpdated',
                            lat: latVal,
                            lng: lngVal,
                            address: displayCombined,
                            formattedAddress: displayCombined
                        }, '*');
                        return;
                    }

                    confirmBtn.disabled = true;
                    confirmBtn.style.opacity = "0.5";
                    addressText.innerText = "Resolving place name...";

                    geocoder.geocode({ location: latLng }, (results, status) => {
                        confirmBtn.disabled = false;
                        confirmBtn.style.opacity = "1";

                        if (status === "OK" && results && results[0]) {
                            const result = results[0];
                            formattedAddress = result.formatted_address;

                            knownName = "";
                            const namePriority = [
                                'point_of_interest',
                                'establishment',
                                'natural_feature',
                                'neighborhood',
                                'sublocality_level_1',
                                'sublocality',
                                'locality',
                                'route'
                            ];

                            for (let type of namePriority) {
                                for (let component of (result.address_components || [])) {
                                    if (component.types && component.types.includes(type) && !component.long_name.includes('+')) {
                                        knownName = component.long_name;
                                        break;
                                    }
                                }
                                if (knownName) break;
                            }

                            if (!knownName) knownName = formattedAddress.split(',')[0];

                            const cleanDisplay = (formattedAddress && !formattedAddress.trim().startsWith('Coordinates:'))
                                ? formattedAddress
                                : knownName;

                            addressText.innerText = cleanDisplay;
                            searchInput.value = cleanDisplay;

                            window.postMessage({
                                action: 'locationUpdated',
                                lat: latVal,
                                lng: lngVal,
                                address: cleanDisplay,
                                formattedAddress: cleanDisplay
                            }, '*');
                        } else {
                            // Fallback to OpenStreetMap Nominatim reverse geocoding so it NEVER saves raw "Coordinates: ..."
                            fetch("https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=" + latVal + "&lon=" + lngVal)
                                .then(r => r.json())
                                .then(osm => {
                                    const osmName = (osm && (osm.name || (osm.address && (osm.address.suburb || osm.address.neighbourhood || osm.address.road || osm.address.city)))) || "Dar es Salaam, Tanzania";
                                    const osmFull = (osm && osm.display_name) ? osm.display_name.split(",").slice(0, 3).join(",").trim() : osmName;
                                    knownName = osmName;
                                    formattedAddress = osmFull;
                                    addressText.innerText = osmFull;
                                    searchInput.value = osmFull;
                                    window.postMessage({
                                        action: 'locationUpdated',
                                        lat: latVal,
                                        lng: lngVal,
                                        address: osmFull,
                                        formattedAddress: osmFull
                                    }, '*');
                                })
                                .catch(() => {
                                    knownName = searchInput.value.trim() || "Dar es Salaam, Tanzania";
                                    formattedAddress = knownName;
                                    addressText.innerText = formattedAddress;
                                    window.postMessage({
                                        action: 'locationUpdated',
                                        lat: latVal,
                                        lng: lngVal,
                                        address: formattedAddress,
                                        formattedAddress: formattedAddress
                                    }, '*');
                                });
                        }
                    });
                }

                function performSearch() {
                    const query = searchInput.value.trim();
                    if (!query) return;

                    searchStatus.innerText = "Searching for '" + query + "'...";
                    searchStatus.style.display = "block";
                    searchStatus.style.color = "#142170";

                    const tryPlacesFallback = function() {
                        if (google.maps.places && typeof google.maps.places.PlacesService === "function") {
                            const placesService = new google.maps.places.PlacesService(map);
                            placesService.findPlaceFromQuery({
                                query: query,
                                fields: ['name', 'geometry', 'formatted_address']
                            }, (pResults, pStatus) => {
                                if (pStatus === google.maps.places.PlacesServiceStatus.OK && pResults && pResults[0] && pResults[0].geometry) {
                                    const pResult = pResults[0];
                                    const loc = pResult.geometry.location;
                                    map.setCenter(loc);
                                    map.setZoom(17);
                                    updateLocation(loc, false, pResult.name || query, pResult.formatted_address || pResult.name || query);
                                    searchStatus.style.display = "none";
                                } else {
                                    tryNominatimSearchFallback();
                                }
                            });
                        } else {
                            tryNominatimSearchFallback();
                        }
                    };

                    const tryNominatimSearchFallback = function() {
                        fetch("https://nominatim.openstreetmap.org/search?format=jsonv2&limit=1&q=" + encodeURIComponent(query + " Tanzania"))
                            .then(r => r.json())
                            .then(list => {
                                if (Array.isArray(list) && list.length > 0) {
                                    const item = list[0];
                                    const loc = new google.maps.LatLng(Number(item.lat), Number(item.lon));
                                    const cleanTitle = (item.display_name || query).split(",").slice(0, 3).join(",").trim();
                                    map.setCenter(loc);
                                    map.setZoom(16);
                                    updateLocation(loc, false, cleanTitle, cleanTitle);
                                    searchStatus.style.display = "none";
                                } else {
                                    searchStatus.innerText = "Could not find '" + query + "'. Try adding city/area (e.g. " + query + ", Dar es Salaam).";
                                    searchStatus.style.color = "#DC2626";
                                }
                            })
                            .catch(() => {
                                searchStatus.innerText = "Could not find '" + query + "'. You can also click or drag the pin directly on the map.";
                                searchStatus.style.color = "#DC2626";
                            });
                    };

                    geocoder.geocode({ address: query }, (results, status) => {
                        if (status === "OK" && results && results[0] && results[0].geometry) {
                            const result = results[0];
                            const loc = result.geometry.location;
                            map.setCenter(loc);
                            map.setZoom(17);
                            updateLocation(loc, false, result.formatted_address.split(',')[0], result.formatted_address);
                            searchStatus.style.display = "none";
                        } else {
                            tryPlacesFallback();
                        }
                    });
                }

                map.addListener('click', (e) => updateLocation(e.latLng));
                marker.addListener('dragend', () => updateLocation(marker.getPosition()));

                searchBtn.onclick = performSearch;
                searchInput.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        performSearch();
                    }
                });

                if (gpsBtn) {
                    gpsBtn.onclick = () => {
                        if (!navigator.geolocation) return;
                        searchStatus.innerText = "Detecting your current GPS location...";
                        searchStatus.style.display = "block";
                        searchStatus.style.color = "#142170";
                        navigator.geolocation.getCurrentPosition(
                            (pos) => {
                                const loc = new google.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
                                map.setCenter(loc);
                                map.setZoom(17);
                                searchStatus.style.display = "none";
                                updateLocation(loc);
                            },
                            () => {
                                searchStatus.innerText = "Unable to read GPS location. Please allow browser location access or search by name.";
                                searchStatus.style.color = "#DC2626";
                            },
                            { enableHighAccuracy: true, timeout: 8000 }
                        );
                    };
                }

                confirmBtn.onclick = () => {
                    if (selectedPos) {
                        const latVal = typeof selectedPos.lat === 'function' ? selectedPos.lat() : selectedPos.lat;
                        const lngVal = typeof selectedPos.lng === 'function' ? selectedPos.lng() : selectedPos.lng;
                        const finalAddr = formattedAddress || knownName || searchInput.value.trim() || "Dar es Salaam, Tanzania";

                        window.postMessage({
                            action: 'modalLocationConfirmed',
                            lat: latVal,
                            lng: lngVal,
                            address: finalAddr,
                            formattedAddress: finalAddr
                        }, '*');
                    }
                };

                closeBtn.onclick = () => {
                    window.postMessage({ action: 'modalPickerClosed' }, '*');
                };

                if ($lat !== -6.7924 || $lng !== 39.2083) {
                    updateLocation(selectedPos);
                } else if (searchInput.value.trim()) {
                    performSearch();
                } else {
                    updateLocation(selectedPos);
                }
            })();
        """.trimIndent()
        document.body?.appendChild(script)

        onDispose {
            container.remove()
            script.remove()
        }
    }

    DisposableEffect(Unit) {
        val messageHandler: (Event) -> Unit = { event ->
            val messageEvent = event.unsafeCast<MessageEvent>()
            val data = messageEvent.data?.unsafeCast<MapMessageData>()
            if (data != null) {
                val action = data.action?.toString()
                val pLat = data.lat?.let { fromJsNumber(it) } ?: 0.0
                val pLng = data.lng?.let { fromJsNumber(it) } ?: 0.0
                val pAddrText = data.address?.toString() ?: ""
                val pFullAddr = data.formattedAddress?.toString() ?: pAddrText

                when (action) {
                    "modalLocationConfirmed" -> {
                        onLocationConfirmed(
                            AddressResult(
                                address = pAddrText.ifBlank { pFullAddr },
                                latitude = pLat,
                                longitude = pLng,
                                formattedAddress = pFullAddr.ifBlank { pAddrText }
                            )
                        )
                    }
                    "locationUpdated" -> {
                        onLocationSelected(
                            AddressResult(
                                address = pAddrText.ifBlank { pFullAddr },
                                latitude = pLat,
                                longitude = pLng,
                                formattedAddress = pFullAddr.ifBlank { pAddrText }
                            )
                        )
                    }
                    "modalPickerClosed" -> {
                        onDismiss()
                    }
                }
            }
        }
        window.addEventListener("message", messageHandler)
        onDispose { window.removeEventListener("message", messageHandler) }
    }

    Box(modifier = modifier)
}

actual fun isMapPickerSupported(): Boolean = true

actual fun isMapTabSupported(): Boolean = false
