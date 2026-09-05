package org.com.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.remember

class JsMapController : MapController {
    override suspend fun searchAddress(query: String): List<AddressResult> {
        return emptyList<AddressResult>()
    }
    override suspend fun reverseGeocode(lat: Double, lng: Double): AddressResult {
        return AddressResult("Location", lat, lng, "Location")
    }
    override suspend fun getCurrentLocation(): LocationResult {
        return LocationResult(0.0, 0.0)
    }
    override fun setMapCenter(lat: Double, lng: Double) {}
    override fun setMarker(lat: Double, lng: Double, title: String?) {}
    override fun clearMarkers() {}
    override fun showRoute(origin: LatLng, destination: LatLng) {}
    override fun clearRoute() {}
    override val selectedLocation = MutableStateFlow<LocationResult?>(null)
    override val selectedAddress = MutableStateFlow<AddressResult?>(null)
}

@Composable
actual fun PlatformMap(
    controller: MapController,
    modifier: Modifier,
    onMapClicked: ((Double, Double) -> Unit)?,
    onMapReady: (() -> Unit)?,
    isVisible: Boolean
) {
}

@Composable
actual fun createMapController(
    apiKey: String?,
    onLocationSelected: (AddressResult) -> Unit,
    onError: (String) -> Unit
): MapController {
    return remember { JsMapController() }
}

@Composable
actual fun PlatformLocationMap(
    latitude: String?,
    longitude: String?,
    modifier: Modifier,
    onLocationSelected: (Double, Double) -> Unit,
    onLocationConfirmed: (AddressResult) -> Unit,
    onDismiss: () -> Unit
) {
}

actual fun isMapPickerSupported(): Boolean = false
actual fun isMapTabSupported(): Boolean = false
