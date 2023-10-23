package com.cleancirclecompany.wasteworker.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NavigationManager(private val context: Context) {

    private val destinations = mutableListOf<Pair<Double, Double>>()
    private var currentDestinationIndex = 0

    init {
        getAllDestinations()
    }

    private fun getAllDestinations() {
        val database = FirebaseDatabase.getInstance().getReference("CheckRequest")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val latitude = childSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = childSnapshot.child("longitude").getValue(Double::class.java)

                    if (latitude != null && longitude != null) {
                        destinations.add(Pair(latitude, longitude))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Database Error: ${error.message}")
            }
        })
    }

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

    fun showNextDestinationDetails() {
        if (currentDestinationIndex < destinations.size) {
            // Show destination details and update UI here
        } else {
            showToast("All destinations have been reached")
        }
        currentDestinationIndex++
    }

    // Add other methods as needed

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
