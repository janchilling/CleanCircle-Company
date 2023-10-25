package com.cleancirclecompany.wasteworker.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cleancirclecompany.wasteworker.R
import com.cleancirclecompany.wasteworker.activities.NavigationActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.util.concurrent.TimeUnit

class LocationManager(private val activity: NavigationActivity) {

    private var fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var mMap: GoogleMap
    private var mapReady = false

    init {
        getCurrentLocation()
    }

    fun getCurrentLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(activity) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        Toast.makeText(activity, "Unable to get Location", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                }
            } else {
                Toast.makeText(activity, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermission(): Boolean {
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                fineLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(activity, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true
        getCurrentLocation()
    }

    fun updateMapLocation(dLatitude: Double, dLongitude: Double) {
        getCurrentLocation()
        if (mapReady) {
            var location = LatLng(latitude, longitude)
            var destination = LatLng(dLatitude, dLongitude)
            val zoomLevel = 16.0f // Adjust the zoom level as needed
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, zoomLevel)
            // Remove the old marker and add the updated one
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(location).title("Your Location").icon(
                BitmapDescriptorFactory.fromBitmap(getCustomMarkerIcon(activity))))
            mMap.addMarker(MarkerOptions().position(destination).title("Your destination"))
            mMap.moveCamera(cameraUpdate)
        }
    }

    fun displayBestRoute(destination: LatLng) {
        val origin = LatLng(latitude, longitude)
        val dest = LatLng(destination.latitude, destination.longitude)

        val geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyCFSIAyys5kLnSu9jO5soSBvRe_jGwtF3I")
            .queryRateLimit(3)
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        try {
            val directionsResult: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin(origin.toString())
                .destination(dest.toString())
                .await()

            if (mapReady) {
                val points = directionsResult.routes[0].overviewPolyline.decodePath()
                val polylineOptions = PolylineOptions()
                for (point in points) {
                    polylineOptions.add(LatLng(point.lat, point.lng))
                }
                polylineOptions.width(5f)
                polylineOptions.color(Color.BLUE)
                mMap.addPolyline(polylineOptions)
            }
        } catch (e: Exception) {
            Log.e("Directions API Error", e.message ?: "Unknown error")
            Toast.makeText(activity, "Failed to get directions", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getCustomMarkerIcon(context: Context): Bitmap {
        val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.location_car)
        val bitmap = Bitmap.createBitmap(drawable?.intrinsicWidth ?: 96, drawable?.intrinsicHeight ?: 96, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}
