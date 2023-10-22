package com.cleancirclecompany.wasteworker.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cleancirclecompany.wasteworker.databinding.ActivityNavigationBinding
import com.cleancirclecompany.wasteworker.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.cleancirclecompany.wasteworker.util.LocationManager
import com.cleancirclecompany.wasteworker.util.NavigationManager

class NavigationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var locationManager: LocationManager
    private lateinit var navigationManager: NavigationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = LocationManager(this)
        navigationManager = NavigationManager(this)

        binding.navigate.setOnClickListener {
            navigationManager.startNavigationToNextDestination()
        }

        binding.collectedButton.setOnClickListener {
            navigationManager.showNextDestinationDetails()
        }

        binding.notCollectedButton.setOnClickListener {
            navigationManager.showNextDestinationDetails()
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
}
