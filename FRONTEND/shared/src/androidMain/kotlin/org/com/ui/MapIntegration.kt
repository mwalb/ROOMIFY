package org.com.ui

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class AndroidMapController(
    private val context: Context,
    val onLocationSelected: (AddressResult) -> Unit,
    val onError: (String) -> Unit
) : MapController {

    val cameraPositionState = CameraPositionState(
        position = CameraPosition.fromLatLngZoom(LatLng(-6.7924, 39.2083), 13f)
    )
    
    val markerState = MarkerState(
        position = LatLng(-6.7924, 39.2083)
    )

    val routePoints = mutableStateListOf<LatLng>()

    override val selectedLocation = MutableStateFlow<LocationResult?>(null)
    override val selectedAddress = MutableStateFlow<AddressResult?>(null)

    override suspend fun searchAddress(query: String): List<AddressResult> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 5)
                if (!addresses.isNullOrEmpty()) {
                    val results = addresses.map { addr ->
                        AddressResult(
                            address = addr.featureName ?: addr.getAddressLine(0) ?: query,
                            latitude = addr.latitude,
                            longitude = addr.longitude,
                            formattedAddress = addr.getAddressLine(0) ?: query
                        )
                    }
                    results
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Unknown error")
                }
                emptyList()
            }
        }
    }

    override suspend fun reverseGeocode(lat: Double, lng: Double): AddressResult {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    AddressResult(
                        address = addr.featureName ?: addr.getAddressLine(0) ?: "Selected Location",
                        latitude = lat,
                        longitude = lng,
                        formattedAddress = addr.getAddressLine(0) ?: "Selected Location"
                    )
                } else {
                    AddressResult("Selected Location", lat, lng, "Selected Location")
                }
            } catch (e: Exception) {
                AddressResult("Selected Location", lat, lng, "Selected Location")
            }
        }
    }

    override suspend fun getCurrentLocation(): LocationResult {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    @Suppress("MissingPermission")
                    val task = fusedLocationClient.lastLocation
                    task.addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(LocationResult(location.latitude, location.longitude, location.accuracy))
                        } else {
                            continuation.resume(LocationResult(-6.7924, 39.2083))
                        }
                    }.addOnFailureListener {
                        continuation.resume(LocationResult(-6.7924, 39.2083))
                    }
                } catch (e: SecurityException) {
                    continuation.resume(LocationResult(-6.7924, 39.2083))
                }
            }
        }
    }

    override fun setMapCenter(lat: Double, lng: Double) {
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
    }

    override fun setMarker(lat: Double, lng: Double, title: String?) {
        val position = LatLng(lat, lng)
        markerState.position = position
        selectedLocation.value = LocationResult(lat, lng)
    }

    override fun clearMarkers() {
    }

    override fun showRoute(origin: org.com.ui.LatLng, destination: org.com.ui.LatLng) {
        routePoints.clear()
        routePoints.add(LatLng(origin.latitude, origin.longitude))
        routePoints.add(LatLng(destination.latitude, destination.longitude))
    }

    override fun clearRoute() {
        routePoints.clear()
    }
}

@Composable
actual fun PlatformMap(
    controller: MapController,
    modifier: Modifier,
    onMapClicked: ((Double, Double) -> Unit)?,
    onMapReady: (() -> Unit)?,
    isVisible: Boolean
) {
    if (!isVisible) return
    
    val androidController = controller as? AndroidMapController ?: return
    val context = LocalContext.current

    GoogleMap(
        modifier = modifier,
        cameraPositionState = androidController.cameraPositionState,
        onMapClick = { latLng ->
            androidController.setMarker(latLng.latitude, latLng.longitude, "Selected Location")
            onMapClicked?.invoke(latLng.latitude, latLng.longitude)
            
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val result = AddressResult(
                        address = addr.featureName ?: addr.getAddressLine(0) ?: "Selected Location",
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        formattedAddress = addr.getAddressLine(0) ?: "Selected Location"
                    )
                    androidController.selectedAddress.value = result
                    androidController.onLocationSelected(result)
                }
            } catch (e: Exception) {
            }
        },
        onMapLoaded = {
            onMapReady?.invoke()
        }
    ) {
        Marker(
            state = androidController.markerState,
            title = "Selected Location"
        )
        
        if (androidController.routePoints.isNotEmpty()) {
            Polyline(
                points = androidController.routePoints.toList(),
                color = Color(0xFF1A237E),
                width = 5f
            )
        }
    }
}

@Composable
actual fun createMapController(
    apiKey: String?,
    onLocationSelected: (AddressResult) -> Unit,
    onError: (String) -> Unit
): MapController {
    val context = LocalContext.current
    return remember {
        AndroidMapController(context, onLocationSelected, onError)
    }
}

@Composable
actual fun PlatformLocationMap(
    latitude: String?,
    longitude: String?,
    modifier: Modifier,
    initialSearch: String?,
    onLocationSelected: (AddressResult) -> Unit,
    onLocationConfirmed: (AddressResult) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lat = latitude?.toDoubleOrNull() ?: -6.7924
    val lng = longitude?.toDoubleOrNull() ?: 39.2083
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 15f)
    }
    
    val markerState = rememberMarkerState(position = LatLng(lat, lng))
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(lat, lng) {
        markerState.position = LatLng(lat, lng)
        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 15f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markerState.position = latLng
            
            // Perform reverse geocoding on click to get "known name"
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    onLocationSelected(AddressResult(
                        address = addr.featureName ?: addr.getAddressLine(0) ?: "Selected Location",
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        formattedAddress = addr.getAddressLine(0) ?: "Selected Location"
                    ))
                } else {
                    onLocationSelected(AddressResult(
                        address = "Selected Location",
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        formattedAddress = "Selected Location"
                    ))
                }
            } catch (e: Exception) {
                onLocationSelected(AddressResult(
                    address = "Selected Location",
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    formattedAddress = "Selected Location"
                ))
            }
        }
    ) {
        Marker(
            state = markerState,
            title = "Selected Location"
        )
    }
}

actual fun isMapPickerSupported(): Boolean = true
actual fun isMapTabSupported(): Boolean = true
