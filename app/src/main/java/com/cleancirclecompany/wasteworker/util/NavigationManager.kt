package com.cleancirclecompany.wasteworker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NavigationManager(private val context: Context, private val callback: DataLoadedCallback) {

    private val destinations = mutableListOf<Pair<Double, Double>>()
    private var currentDestinationIndex = 0


    init {
        getAllDestinations(callback)
    }


// Get all destinations function
    private fun getAllDestinations(callback: DataLoadedCallback) {
        val database = FirebaseDatabase.getInstance().getReference("TestAddDestinations")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("Snap...........???????", snapshot.toString())
                for (childSnapshot in snapshot.children) {
                    val latitude = childSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = childSnapshot.child("longitude").getValue(Double::class.java)
                    Log.i("Latitude.....???????", latitude.toString())
                    if (latitude != null && longitude != null) {
                        destinations.add(Pair(latitude, longitude))
                    }
                }
                callback.onDataLoaded()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Database Error: ${error.message}")
            }
        })
    }


// Navigation to the next location
    fun startNavigationToNextDestination() {
        if (currentDestinationIndex < destinations.size) {
            val destination = destinations[currentDestinationIndex]
            val gmmIntentUri = Uri.parse("google.navigation:q=${destination.first},${destination.second}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
        } else {
            showToast("All destinations have been reached")
        }
    }


    fun updateDestinationLocation() {
        currentDestinationIndex++
    }


// Sending the current destination details
    fun getDestinationDetails(): Pair<Double, Double>? {
        return if (destinations.isNotEmpty() && currentDestinationIndex < destinations.size) {
            destinations[currentDestinationIndex]
        } else {
            null
        }
    }


    interface DataLoadedCallback {
        fun onDataLoaded()
    }


    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
