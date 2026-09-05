package org.com.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

interface MapController {
    suspend fun searchAddress(query: String): List<AddressResult>
    suspend fun reverseGeocode(lat: Double, lng: Double): AddressResult
    suspend fun getCurrentLocation(): LocationResult
    fun setMapCenter(lat: Double, lng: Double)
    fun setMarker(lat: Double, lng: Double, title: String?)
    fun clearMarkers()
    fun showRoute(origin: LatLng, destination: LatLng)
    fun clearRoute()

    val selectedLocation: MutableStateFlow<LocationResult?>
    val selectedAddress: MutableStateFlow<AddressResult?>
}

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class AddressResult(
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String,
    val placeId: String? = null
)

@Serializable
data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null
)

@Composable
expect fun PlatformMap(
    controller: MapController,
    modifier: Modifier = Modifier,
    onMapClicked: ((Double, Double) -> Unit)? = null,
    onMapReady: (() -> Unit)? = null,
    isVisible: Boolean = true
)

@Composable
expect fun createMapController(
    apiKey: String? = null,
    onLocationSelected: (AddressResult) -> Unit,
    onError: (String) -> Unit
): MapController

@Composable
expect fun PlatformLocationMap(
    latitude: String?,
    longitude: String?,
    modifier: Modifier,
    initialSearch: String? = null,
    onLocationSelected: (AddressResult) -> Unit,
    onLocationConfirmed: (AddressResult) -> Unit = {},
    onDismiss: () -> Unit = {}
)

expect fun isMapPickerSupported(): Boolean

expect fun isMapTabSupported(): Boolean
