package com.cleancirclecompany.wasteworker.activities

import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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

        binding.btnShowMap.setOnClickListener {
            val message : String? = "Are you sure you want to pickup this wastage"
            showPopupMessage(message)
        }
    }

    private fun showPopupMessage(message: String?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_popup_dialog)

        val tvMessage : TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes : Button = dialog.findViewById(R.id.btnYes)
        val btnNo : Button = dialog.findViewById(R.id.btnNo)

        tvMessage.text = message

        btnYes.setOnClickListener {
            // Get the current camera position
            val cameraPosition = mMap.cameraPosition.target
            // Save the latitude and longitude to Firebase
            saveLocationToFirebase(cameraPosition.latitude, cameraPosition.longitude)

            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        dfReference = FirebaseDatabase.getInstance().getReference("CheckRequest")

        dfReference.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val selectedWastageType = intent.getStringExtra("wastageType")
                val newStatus = "Open"
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-mm-dd HH:mm:ss.SSS")
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

    private fun saveLocationToFirebase(latitude: Double, longitude: Double) {
        val locationReference = FirebaseDatabase.getInstance().getReference("TestAddDestinations")

        // Create a query to find the existing location with similar coordinates (rounded to 6 decimal places)
        val query = locationReference.orderByChild("latitude").startAt(roundTo6DecimalPlaces(latitude))
            .endAt(roundTo6DecimalPlaces(latitude))

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check if there is an existing location with similar latitude
                var locationExists = false
                var locationKey: String? = null

                for (locationSnapshot in dataSnapshot.children) {
                    val existingLongitude = locationSnapshot.child("longitude").getValue(Double::class.java)
                    if (existingLongitude != null && roundTo6DecimalPlaces(existingLongitude) == roundTo6DecimalPlaces(longitude)) {
                        // Location with similar latitude and longitude found, no need to update
                        locationExists = true
                        break
                    }
                    locationKey = locationSnapshot.key
                }

                if (!locationExists) {
                    // No existing location with similar latitude and longitude found, create a new one
                    if (locationKey == null) {
                        // If no location with the same latitude exists, use a new key
                        locationKey = locationReference.push().key
                    }

                    val locationData = HashMap<String, Any>()
                    locationData["latitude"] = roundTo6DecimalPlaces(latitude)
                    locationData["longitude"] = roundTo6DecimalPlaces(longitude)

                    locationReference.child(locationKey!!).setValue(locationData)

                    //success popup
                    showSuccessMessage()
                } else {
                    //failed popup
                    showFailedMessage()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event here if needed
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
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.failed_dialog)

        val buttonFailed : Button = dialog.findViewById(R.id.buttonFailed)

        buttonFailed.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun roundTo6DecimalPlaces(value: Double): Double {
        return String.format("%.6f", value).toDouble()
    }
}

