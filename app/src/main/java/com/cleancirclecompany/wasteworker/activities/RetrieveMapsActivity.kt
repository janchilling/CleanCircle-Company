package com.cleancirclecompany.wasteworker.activities

import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
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

class RetrieveMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityRetrieveMapsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var reportRequest: DatabaseReference
    private lateinit var pickUpRequest : DatabaseReference

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

        binding.btnBackHome.setOnClickListener {
            val intent = Intent(this, WorkerHomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        pickUpRequest = FirebaseDatabase.getInstance().getReference("PickupRequests")
        reportRequest = FirebaseDatabase.getInstance().getReference("Reports")
        val testAddDestinationsReference = FirebaseDatabase.getInstance().getReference("TestAddDestinations")

        pickUpRequest.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val selectedWastageType = intent.getStringExtra("wasteType")
                val newStatus = "Open"
                for (pickupSnapshot in dataSnapshot.children) {

                    val locationWastageType = pickupSnapshot.child("wasteType").getValue(String::class.java)
                    val weight = pickupSnapshot.child("estimatedWeight").getValue(String::class.java)
                    val status = pickupSnapshot.child("status").getValue(String::class.java)

                    if (locationWastageType == selectedWastageType && status == newStatus ) {
                        val latitude = pickupSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = pickupSnapshot.child("longitude").getValue(Double::class.java)

                        val location = LatLng(latitude ?: 6.861939, longitude ?: 79.971998)

                        val marker = mMap.addMarker(MarkerOptions().position(location).title("$selectedWastageType").snippet("Weight: $weight"))

                        // Add a click listener to the marker
                        marker?.tag = Pair(latitude, longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10F))

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event here if needed
            }
        })

        reportRequest.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val selectedWastageType = intent.getStringExtra("wasteType")
                val newStatus = "Open"
                for (pickupSnapshot in dataSnapshot.children) {

                    val locationWastageType = pickupSnapshot.child("wasteType").getValue(String::class.java)
                    val weight = pickupSnapshot.child("estimateWeight").getValue(String::class.java)
                    val status = pickupSnapshot.child("status").getValue(String::class.java)

                    if (locationWastageType == selectedWastageType && status == newStatus ) {
                        val latitude = pickupSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = pickupSnapshot.child("longitude").getValue(Double::class.java)

                        val location = LatLng(latitude ?: 6.861939, longitude ?: 79.971998)

                        val marker = mMap.addMarker(MarkerOptions().position(location).title("$selectedWastageType").snippet("Weight: $weight"))

                        // Add a click listener to the marker
                        marker?.tag = Pair(latitude, longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10F))

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event here if needed
            }
        })

        // Add a marker click listener to save the latitude and longitude
        mMap.setOnMarkerClickListener { marker ->
            val tag = marker.tag
            if (tag is Pair<*, *>) {
                val latitude = tag.first as? Double
                val longitude = tag.second as? Double

                if (latitude != null && longitude != null) {
                    // Check if the latitude and longitude already exist in the database
                    testAddDestinationsReference.orderByChild("latitude").equalTo(latitude).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var alreadyExists = false
                            for (childSnapshot in snapshot.children) {
                                val existingLongitude = childSnapshot.child("longitude").getValue(Double::class.java)
                                if (existingLongitude == longitude) {
                                    alreadyExists = true
                                    break
                                }
                            }

                            if (!alreadyExists) {
                                // Save the latitude and longitude to the TestAddDestinations database
                                val destinationKey = testAddDestinationsReference.push().key
                                if (destinationKey != null) {
                                    testAddDestinationsReference.child(destinationKey).child("latitude").setValue(latitude)
                                    testAddDestinationsReference.child(destinationKey).child("longitude").setValue(longitude)
                                    showSuccessMessage()
                                    changeStatus()
                                }
                            } else {
                                // Show message
                                showFailedMessage()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle the database error
                        }
                    })
                }
            }

            // Return false to indicate that the default behavior (opening the marker's info window) should also occur.
            return@setOnMarkerClickListener false
        }
    }

    private fun changeStatus(){
        pickUpRequest = FirebaseDatabase.getInstance().getReference("PickupRequests")
        reportRequest = FirebaseDatabase.getInstance().getReference("Reports")
        val testAddDestinationsReference = FirebaseDatabase.getInstance().getReference("TestAddDestinations")

        pickUpRequest.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (pickupSnapshot in snapshot.children){
                    val latitude = pickupSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = pickupSnapshot.child("longitude").getValue(Double::class.java)

                    // Check if the location is already in TestAddDestinations
                    if (latitude != null) {
                        testAddDestinationsReference.orderByChild("latitude").equalTo(latitude).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var alreadyExists = false
                                for (childSnapshot in snapshot.children) {
                                    val existingLongitude = childSnapshot.child("longitude").getValue(Double::class.java)
                                    if (existingLongitude == longitude) {
                                        alreadyExists = true
                                        break
                                    }
                                }

                                if (alreadyExists) {
                                    // Change the status to "Selected" in the CheckRequest database
                                    pickUpRequest.child(pickupSnapshot.key!!).child("status")
                                        .setValue("Selected")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        reportRequest.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (pickupSnapshot in snapshot.children){
                    val latitude = pickupSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = pickupSnapshot.child("longitude").getValue(Double::class.java)

                    // Check if the location is already in TestAddDestinations
                    if (latitude != null) {
                        testAddDestinationsReference.orderByChild("latitude").equalTo(latitude).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var alreadyExists = false
                                for (childSnapshot in snapshot.children) {
                                    val existingLongitude = childSnapshot.child("longitude").getValue(Double::class.java)
                                    if (existingLongitude == longitude) {
                                        alreadyExists = true
                                        break
                                    }
                                }

                                if (alreadyExists) {
                                    // Change the status to "Selected" in the CheckRequest database
                                    reportRequest.child(pickupSnapshot.key!!).child("status")
                                        .setValue("Selected")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun showSuccessMessage() {
        val successDialog = Dialog(this)
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        successDialog.setCancelable(false)
        successDialog.setContentView(R.layout.success_dialog)

        val buttonOk: Button = successDialog.findViewById(R.id.buttonOk)

        buttonOk.setOnClickListener {
            successDialog.dismiss() // Dismiss the success dialog
        }
        // Show the success dialog
        successDialog.show()
    }


    private fun showFailedMessage(){
        val failedDialog = Dialog(this)
        failedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        failedDialog.setCancelable(false)
        failedDialog.setContentView(R.layout.failed_dialog)

        val buttonFailed : Button = failedDialog.findViewById(R.id.buttonFailed)

        buttonFailed.setOnClickListener {
            failedDialog.dismiss()
        }
        failedDialog.show()
    }
}

