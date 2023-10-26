package com.cleancirclecompany.wasteworker.activities

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.*
import com.cleancirclecompany.wasteworker.R
import com.cleancirclecompany.wasteworker.databinding.ActivityNavigationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.cleancirclecompany.wasteworker.util.LocationManager
import com.cleancirclecompany.wasteworker.util.NavigationManager
import com.google.android.gms.maps.model.LatLng

class NavigationActivity : AppCompatActivity(), NavigationManager.DataLoadedCallback {
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var locationManager: LocationManager
    private lateinit var navigationManager: NavigationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigationManager = NavigationManager(this, object : NavigationManager.DataLoadedCallback {
            override fun onDataLoaded() {
                val destination = navigationManager.getDestinationDetails()

                if (destination != null) {
                    locationManager.updateMapLocation(destination.first, destination.second)
                } else {
                    Toast.makeText(this@NavigationActivity, "Unable to get Destination", Toast.LENGTH_SHORT).show()
                }
            }
        })

        locationManager = LocationManager(this)


        binding.navigate.setOnClickListener {
            navigationManager.startNavigationToNextDestination()
        }

        binding.collectedButton.setOnClickListener {
            showConfirmationDialog("Collected",
                "Have you collected the waste at this destination?", DialogInterface.BUTTON_POSITIVE)
        }

        binding.notCollectedButton.setOnClickListener {
           showConfirmationDialogNotCollected("Not Collected",
               "Have you collected the waste at this destination?", DialogInterface.BUTTON_POSITIVE)
        }

        binding.btnBackHome.setOnClickListener {
            val intent = Intent(this, WorkerHomeActivity::class.java)
            startActivity(intent)
        }

        // Initialize and set up map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            locationManager.onMapReady(googleMap)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManager.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onDataLoaded() {
        updateMap()
    }

    private fun showConfirmationDialog(title: String, message: String, result: Int) {
        val builder = Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                // User confirmed
                if (result == DialogInterface.BUTTON_POSITIVE) {
                    showToast("Waste collected")
                } else {
                    showToast("Waste not collected")
                }
                navigationManager.updateDestinationLocation()
                updateMap()
            }
            .setNegativeButton("No") { _, _ ->
                // User canceled
                showToast("Action canceled")
            }
            .create()
            .show()
    }

    private fun showConfirmationDialogNotCollected(title: String, message: String, result: Int) {
        val builder = Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm not collected") { _, _ ->
                // User confirmed
                if (result == DialogInterface.BUTTON_POSITIVE) {
                    showToast("Waste collected")
                } else {
                    showToast("Waste not collected")
                }
                navigationManager.updateDestinationLocation()
                updateMap()
            }
            .setNegativeButton("Back") { _, _ ->
                // User canceled
                showToast("Action canceled")
            }
            .create()
            .show()
    }

    private fun updateMap() {
        val destination = navigationManager.getDestinationDetails()

        if (destination != null) {
            locationManager.updateMapLocation(destination.first, destination.second)
        } else {
            Toast.makeText(this, "All destinations have been reached", Toast.LENGTH_LONG).show()

                // All destinations are reached, navigate to WorkerHomeActivity
                val intent = Intent(this, WorkerHomeActivity::class.java)
                startActivity(intent)

        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
