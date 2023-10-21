package com.cleancirclecompany.wasteworker.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.cleancirclecompany.wasteworker.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.cleancirclecompany.wasteworker.databinding.ActivityRetrieveMapsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class RetrieveMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRetrieveMapsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dfReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRetrieveMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and get the locations
        firebaseAuth = FirebaseAuth.getInstance()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        dfReference = FirebaseDatabase.getInstance().getReference("PickupDestinations")

        dfReference.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val selectedWastageType = intent.getStringExtra("wastageType")
                val newStatus = "Open"
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd-mm-yyyy")
                val formatted = current.format(formatter)
                for (pickupSnapshot in dataSnapshot.children) {

                    val locationWastageType = pickupSnapshot.child("wastageType").getValue(String::class.java)
                    val weight = pickupSnapshot.child("weight").getValue(Double::class.java)
                    val status = pickupSnapshot.child("status").getValue(String::class.java)
                    val date = pickupSnapshot.child("date").getValue(String::class.java)

                    if (locationWastageType == selectedWastageType && status == newStatus && formatted >= date.toString()) {
                        val latitude = pickupSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = pickupSnapshot.child("longitude").getValue(Double::class.java)

                        val location = LatLng(latitude ?: 6.861939, longitude ?: 79.971998)

                        mMap.addMarker(MarkerOptions().position(location).title("$selectedWastageType").snippet("Weight: $weight kg"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10F))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event here if needed
            }
        })
    }
}

