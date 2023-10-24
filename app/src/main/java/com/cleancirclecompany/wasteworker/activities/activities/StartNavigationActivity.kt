package com.cleancirclecompany.wasteworker.activities.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cleancirclecompany.wasteworker.databinding.ActivityStartNavigationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StartNavigationActivity: AppCompatActivity() {

    private lateinit var binding : ActivityStartNavigationBinding
    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var dlatitude : String
    private lateinit var dlongitude : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the content view that renders the map.
        binding = ActivityStartNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getAllDestinations()

        binding.navigate.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=$dlatitude,$dlongitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    private fun getAllDestinations(){
        val database = FirebaseDatabase.getInstance().getReference("PickupDestinations")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstChild = snapshot.children.first()

                dlatitude = firstChild.child("latitude").value.toString()
                dlongitude = firstChild.child("longitude").value.toString()
                Log.d("Lets go","Latitude: $dlatitude, Longitude: $dlongitude")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors
                Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_LONG).show()
            }

        })
    }

}