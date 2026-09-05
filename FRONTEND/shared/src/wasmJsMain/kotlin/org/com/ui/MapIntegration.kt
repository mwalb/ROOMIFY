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
            apiKey = apiKey ?: "AIzaSyCez1-gQmVYnJoIf6xZS1m-VhXb6tyZ0so",
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
    
    val container = remember {
        document.createElement("div").unsafeCast<HTMLDivElement>().apply {
            id = mapId
            style.position = "fixed"
            style.top = "0"
            style.left = "0"
            style.width = "100vw"
            style.height = "100vh"
            style.zIndex = "3000" 
            style.backgroundColor = "white"
            style.display = "flex"
            style.flexDirection = "column"
        }
    }

    DisposableEffect(Unit) {
        document.body?.appendChild(container)
        
        container.innerHTML = """
            <div id="${mapId}_header" style="padding: 12px 16px; background: white; border-bottom: 1px solid #eee; display: flex; flex-direction: column; gap: 12px;">
                <div style="display: flex; align-items: center; justify-content: space-between;">
                    <h3 style="margin: 0; font-family: sans-serif; font-size: 18px; color: #1A237E;">Select Property Location</h3>
                    <button id="${mapId}_close" style="background: none; border: none; font-size: 28px; cursor: pointer; color: #666;">&times;</button>
                </div>
                <div style="display: flex; gap: 8px;">
                    <input id="${mapId}_search_input" type="text" value="${initialSearch ?: ""}" placeholder="Search for area, street or landmark..." 
                        style="flex: 1; height: 44px; padding: 0 16px; border: 1px solid #ddd; border-radius: 8px; font-family: sans-serif; font-size: 14px; outline: none;">
                    <button id="${mapId}_search_btn" 
                        style="height: 44px; padding: 0 20px; background: #1A237E; color: white; border: none; border-radius: 8px; font-weight: bold; cursor: pointer;">
                        Search
                    </button>
                </div>
                <div id="${mapId}_search_status" style="font-family: sans-serif; font-size: 12px; color: #666; display: none;"></div>
            </div>
            <div id="${mapId}_canvas" style="flex: 1; background: #e5e3df;"></div>
            <div id="${mapId}_footer" style="padding: 20px; background: white; box-shadow: 0 -4px 15px rgba(0,0,0,0.1); border-top: 1px solid #eee;">
                <div style="font-family: sans-serif; font-size: 12px; color: #666; margin-bottom: 4px;">Selected location:</div>
                <div id="${mapId}_address" style="font-family: sans-serif; font-size: 14px; font-weight: 600; min-height: 36px; line-height: 1.4; color: #1A1A1A;">
                    Loading address...
                </div>
                <div id="${mapId}_coords" style="font-family: monospace; font-size: 11px; color: #888; margin: 8px 0 16px 0;"></div>
                <button id="${mapId}_confirm" disabled 
                    style="width: 100%; height: 52px; background: #1A237E; color: white; border: none; border-radius: 12px; font-weight: bold; font-size: 15px; cursor: pointer; transition: opacity 0.2s;">
                    CONFIRM LOCATION
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
                    fullscreenControl: false
                });
                
                const marker = new google.maps.Marker({
                    position: selectedPos,
                    map: map,
                    draggable: true,
                    animation: google.maps.Animation.DROP
                });

                const geocoder = new google.maps.Geocoder();

                function updateLocation(latLng, skipGeocode = false, providedName = null) {
                    selectedPos = latLng;
                    marker.setPosition(latLng);
                    confirmBtn.disabled = false;
                    confirmBtn.style.opacity = "1";
                    
                    const latVal = typeof latLng.lat === 'function' ? latLng.lat() : latLng.lat;
                    const lngVal = typeof latLng.lng === 'function' ? latLng.lng() : latLng.lng;
                    coordsText.innerText = "Lat: " + latVal.toFixed(6) + " | Lng: " + lngVal.toFixed(6);

                    if (skipGeocode) return;
                    
                    confirmBtn.disabled = true;
                    confirmBtn.style.opacity = "0.5";

                    if (providedName) {
                        knownName = providedName;
                        addressText.innerText = knownName;
                        confirmBtn.disabled = false;
                        confirmBtn.style.opacity = "1";
                        window.postMessage({ 
                            action: 'locationUpdated', 
                            lat: latVal, 
                            lng: lngVal, 
                            address: knownName,
                            formattedAddress: knownName
                        }, '*');
                        return;
                    }

                    addressText.innerText = "Resolving address...";
                    geocoder.geocode({ location: latLng }, (results, status) => {
                        confirmBtn.disabled = false;
                        confirmBtn.style.opacity = "1";
                        
                        if (status === "OK" && results[0]) {
                            const result = results[0];
                            formattedAddress = result.formatted_address;
                            
                            knownName = "";
                            // Improved logic to find the real name of the location
                            const namePriority = [
                                'point_of_interest', 
                                'establishment', 
                                'natural_feature', 
                                'neighborhood', 
                                'sublocality_level_1',
                                'sublocality',
                                'locality'
                            ];

                            for (let type of namePriority) {
                                for (let component of result.address_components) {
                                    if (component.types.includes(type)) {
                                        knownName = component.long_name;
                                        break;
                                    }
                                }
                                if (knownName) break;
                            }
                            
                            if (!knownName) {
                                for (let component of result.address_components) {
                                    if (component.types.includes('route')) {
                                        knownName = component.long_name;
                                        break;
                                    }
                                }
                            }
                            if (!knownName) knownName = formattedAddress.split(',')[0];

                            addressText.innerText = formattedAddress;
                            
                            window.postMessage({ 
                                action: 'locationUpdated', 
                                lat: latVal, 
                                lng: lngVal, 
                                address: knownName,
                                formattedAddress: formattedAddress
                            }, '*');
                        } else {
                            const latVal = typeof latLng.lat === 'function' ? latLng.lat() : latLng.lat;
                            const lngVal = typeof latLng.lng === 'function' ? latLng.lng() : latLng.lng;
                            
                            formattedAddress = "Coordinates: " + latVal.toFixed(6) + ", " + latVal.toFixed(6);
                            knownName = "Pinned Location";
                            addressText.innerText = formattedAddress;
                            
                            window.postMessage({ 
                                action: 'locationUpdated', 
                                lat: latVal, 
                                lng: lngVal, 
                                address: knownName,
                                formattedAddress: formattedAddress
                            }, '*');
                        }
                    });
                }

                function performSearch() {
                    const query = searchInput.value.trim();
                    if (!query) return;

                    console.log("Roomify Search: " + query);
                    searchStatus.innerText = "Searching...";
                    searchStatus.style.display = "block";
                    searchStatus.style.color = "#666";

                    geocoder.geocode({ address: query }, (results, status) => {
                        console.log("Roomify Geocode status: " + status);
                        if (status === "OK" && results[0]) {
                            const result = results[0];
                            const loc = result.geometry.location;
                            console.log("Roomify Geocode success:", result.formatted_address);
                            
                            map.setCenter(loc);
                            map.setZoom(17);
                            updateLocation(loc, false, result.formatted_address.split(',')[0]);
                            searchStatus.style.display = "none";
                        } else {
                            // Fallback to Places Service if Geocoding is denied or fails
                            console.log("Roomify: Geocoding failed with " + status + ", trying Places Service...");
                            
                            try {
                                const placesService = new google.maps.places.PlacesService(mapCanvas);
                                placesService.findPlaceFromQuery({
                                    query: query,
                                    fields: ['name', 'geometry', 'formatted_address', 'address_components']
                                }, (pResults, pStatus) => {
                                    console.log("Roomify Places status: " + pStatus);
                                    if (pStatus === google.maps.places.PlacesServiceStatus.OK && pResults[0]) {
                                        const pResult = pResults[0];
                                        const loc = pResult.geometry.location;
                                        console.log("Roomify Places success:", pResult.name);
                                        
                                        map.setCenter(loc);
                                        map.setZoom(17);
                                        updateLocation(loc, false, pResult.name);
                                        searchStatus.style.display = "none";
                                    } else {
                                        searchStatus.innerText = "Location not found. Ensure Geocoding, Places and Maps JS APIs are enabled for this key.";
                                        searchStatus.style.color = "#f44336";
                                        console.error("Roomify Search error: Geocoding=" + status + ", Places=" + pStatus);
                                    }
                                });
                            } catch (e) {
                                console.error("Places service error:", e);
                                searchStatus.innerText = "Search error. Try more specific terms.";
                                searchStatus.style.color = "#f44336";
                            }
                        }
                    });
                }

                map.addListener('click', (e) => updateLocation(e.latLng));
                marker.addListener('dragend', () => updateLocation(marker.getPosition()));
                
                searchBtn.onclick = performSearch;
                searchInput.onkeypress = (e) => { if (e.key === 'Enter') performSearch(); };

                confirmBtn.onclick = () => {
                    console.log("Roomify confirm clicked");
                    if (selectedPos) {
                        const latVal = typeof selectedPos.lat === 'function' ? selectedPos.lat() : selectedPos.lat;
                        const lngVal = typeof selectedPos.lng === 'function' ? selectedPos.lng() : selectedPos.lng;
                        
                        console.log("Roomify confirming: " + latVal + ", " + lngVal);
                        
                        window.postMessage({ 
                            action: 'modalLocationConfirmed', 
                            lat: latVal, 
                            lng: lngVal, 
                            address: knownName,
                            formattedAddress: formattedAddress 
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
                val pFullAddr = data.formattedAddress?.toString() ?: ""

                when(action) {
                    "modalLocationConfirmed" -> {
                        onLocationConfirmed(AddressResult(
                            address = pAddrText,
                            latitude = pLat,
                            longitude = pLng,
                            formattedAddress = pFullAddr
                        ))
                    }
                    "locationUpdated" -> {
                        onLocationSelected(AddressResult(
                            address = pAddrText,
                            latitude = pLat,
                            longitude = pLng,
                            formattedAddress = pFullAddr
                        ))
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
