package com.example.getcab

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class SelectLocationActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocationInput: EditText
    private lateinit var dropLocationInput: EditText
    private lateinit var nextButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        currentLocationInput = findViewById(R.id.current_location_input)
        dropLocationInput = findViewById(R.id.drop_location_input)
        nextButton = findViewById(R.id.next_button)
        backButton = findViewById(R.id.back_button)

        if (hasLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
        window.apply {
            decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = android.graphics.Color.WHITE
        }
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clears the stack and brings MainActivity to front
            startActivity(intent)
            finish()
        }

        nextButton.setOnClickListener {
            val currentLocation = currentLocationInput.text.toString()
            val dropLocation = dropLocationInput.text.toString()

            if (currentLocation.isNotEmpty() && dropLocation.isNotEmpty()) {
                val intent = Intent(this, SelectVehicleActivity::class.java).apply {
                    putExtra("CURRENT_LOCATION", currentLocation)
                    putExtra("DROP_LOCATION", dropLocation)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter both locations", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to fetch the current location.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    currentLocationInput.setText(addresses[0].getAddressLine(0))
                } else {
                    currentLocationInput.setText(getString(R.string.location_not_found))
                }
            } else {
                currentLocationInput.setText(getString(R.string.unable_to_fetch_location))
            }
        }.addOnFailureListener {
            currentLocationInput.setText(getString(R.string.location_fetch_failed))
        }
    }
}
