//package com.example.getcab
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.squareup.picasso.Picasso
//
//class DriverDetailsActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private var driverLat: Double = 0.0
//    private var driverLng: Double = 0.0
//    private lateinit var googleMap: GoogleMap
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_driver_details)
//
//        // Get driver details from intent
//        val driverName = intent.getStringExtra("driverName") ?: "Unknown Driver"
//        driverLat = intent.getDoubleExtra("driverLat", 0.0)
//        driverLng = intent.getDoubleExtra("driverLng", 0.0)
//        val distance = intent.getDoubleExtra("distance", 0.0)
//        val carName = intent.getStringExtra("carName") ?: "Unknown Car"
//        val carNumber = intent.getStringExtra("carNumber") ?: "Unknown Number"
//        val phoneNumber = intent.getStringExtra("phoneNumber") ?: "Unknown Phone"
//        val driverImage = intent.getStringExtra("driverImage")
//        val driverRating = intent.getStringExtra("driverRating") ?: "Not Rated"
//
//        // Initialize views
//        val driverNameText = findViewById<TextView>(R.id.driver_name)
//        val driverRatingText = findViewById<TextView>(R.id.driver_rating)
//        val carNameText = findViewById<TextView>(R.id.driver_car_details)
//        val carNumberText = findViewById<TextView>(R.id.driver_car_number)
//        val phoneNumberText = findViewById<TextView>(R.id.driver_phone)
//        val etaText = findViewById<TextView>(R.id.eta)
//        val pickupPointText = findViewById<TextView>(R.id.pickup_point)
//        val driverImageView = findViewById<ImageView>(R.id.driver_profile_image)
//        val contactDriverButton = findViewById<Button>(R.id.contact_driver_button)
//        val messageDriverButton = findViewById<Button>(R.id.message_driver_button)
//
//        // Set text values
//        driverNameText.text = driverName
//        driverRatingText.text = "$driverRating ★"
//        carNameText.text = carName
//        carNumberText.text = carNumber
//        phoneNumberText.text = phoneNumber
//        etaText.text = "${distance} min"
//        pickupPointText.text = "Meet at the pickup point"
//
//        // Load driver image
//        if (!driverImage.isNullOrEmpty()) {
//            Picasso.get().load(driverImage).into(driverImageView)
//        } else {
//            driverImageView.setImageResource(R.drawable.ic_driver_placeholder)
//        }
//
//        // Handle calling the driver
//        contactDriverButton.setOnClickListener {
//            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
//            startActivity(callIntent)
//        }
//
//        // Handle messaging the driver
//        messageDriverButton.setOnClickListener {
//            val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
//            startActivity(smsIntent)
//        }
//
//        // Initialize the map
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//    }
//
//    override fun onMapReady(map: GoogleMap) {
//        googleMap = map
//        val driverLocation = LatLng(driverLat, driverLng)
//        googleMap.addMarker(MarkerOptions().position(driverLocation).title("Driver's Location"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15f))
//    }
//}

package com.example.getcab

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class DriverDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var driverLat: Double = 0.0
    private var driverLng: Double = 0.0
    private lateinit var googleMap: GoogleMap
    private var driverId: String? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var databaseReference: DatabaseReference

    private lateinit var driverNameText: TextView
    private lateinit var driverRatingText: TextView
    private lateinit var carNameText: TextView
    private lateinit var carNumberText: TextView
    private lateinit var phoneNumberText: TextView
    private lateinit var etaText: TextView
    private lateinit var pickupPointText: TextView
    private lateinit var driverImageView: ImageView
    private lateinit var contactDriverButton: Button
    private lateinit var messageDriverButton: Button
    private lateinit var cancelButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_details)

        sharedPreferences = getSharedPreferences("RidePrefs", MODE_PRIVATE)

        // Retrieve driver details from intent or SharedPreferences
        driverId = intent.getStringExtra("driverId") ?: sharedPreferences.getString("driverId", null)
        var driverName = intent.getStringExtra("driverName") ?: sharedPreferences.getString("driverName", "Unknown")
        driverLat = intent.getDoubleExtra("driverLat", sharedPreferences.getFloat("driverLat", 0.0f).toDouble())
        driverLng = intent.getDoubleExtra("driverLng", sharedPreferences.getFloat("driverLng", 0.0f).toDouble())
        val distance = intent.getDoubleExtra("distance", 0.0)
        var carName = intent.getStringExtra("carName") ?: sharedPreferences.getString("carName", "Unknown")
        var carNumber = intent.getStringExtra("carNumber") ?: sharedPreferences.getString("carNumber", "Unknown")
        var phoneNumber = intent.getStringExtra("phoneNumber") ?: sharedPreferences.getString("phoneNumber", "Unknown")
        val driverImage = intent.getStringExtra("driverImage")
        val driverRating = intent.getStringExtra("driverRating") ?: "Not Rated"

        // Initialize views
        driverNameText = findViewById(R.id.driver_name)
        driverRatingText = findViewById(R.id.driver_rating)
        carNameText = findViewById(R.id.driver_car_details)
        carNumberText = findViewById(R.id.driver_car_number)
        phoneNumberText = findViewById(R.id.driver_phone)
        etaText = findViewById(R.id.eta)
        pickupPointText = findViewById(R.id.pickup_point)
        driverImageView = findViewById(R.id.driver_profile_image)
        contactDriverButton = findViewById(R.id.contact_driver_button)
        messageDriverButton = findViewById(R.id.message_driver_button)
        cancelButton = findViewById(R.id.cancel_button)

        // Load details into UI
        driverNameText.text = driverName
        driverRatingText.text = "$driverRating ★"
        carNameText.text = carName
        carNumberText.text = carNumber
        phoneNumberText.text = phoneNumber
        etaText.text = "${distance} min"
        pickupPointText.text = "Meet at the pickup point"

        if (!driverImage.isNullOrEmpty()) {
            Picasso.get().load(driverImage).into(driverImageView)
        } else {
            driverImageView.setImageResource(R.drawable.ic_driver_placeholder)
        }

        // Fetch details from Firebase if any are missing
        if (driverName == "Unknown" || carName == "Unknown" || carNumber == "Unknown" || phoneNumber == "Unknown") {
            fetchDriverDetailsFromDatabase()
        }

        // Contact driver button
        contactDriverButton.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(callIntent)
        }

        // Message driver button
        messageDriverButton.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
            startActivity(smsIntent)
        }

        // Cancel button to return to main screen
        cancelButton.setOnClickListener {
            val intent = Intent(this@DriverDetailsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Load map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val driverLocation = LatLng(driverLat, driverLng)
        googleMap.addMarker(MarkerOptions().position(driverLocation).title("Driver's Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 15f))
    }

    private fun fetchDriverDetailsFromDatabase() {
        driverId?.let { id ->
            databaseReference = FirebaseDatabase.getInstance().reference.child("drivers").child(id)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java) ?: "Unknown"
                        val car = snapshot.child("car_name").getValue(String::class.java) ?: "Unknown"
                        val carNo = snapshot.child("car_number").getValue(String::class.java) ?: "Unknown"
                        val phone = snapshot.child("phone_number").getValue(String::class.java) ?: "Unknown"

                        // Save to SharedPreferences
                        sharedPreferences.edit().apply {
                            putString("driverId", id)
                            putString("driverName", name)
                            putString("carName", car)
                            putString("carNumber", carNo)
                            putString("phoneNumber", phone)
                            apply()
                        }

                        // Update UI
                        driverNameText.text = name
                        carNameText.text = car
                        carNumberText.text = carNo
                        phoneNumberText.text = phone
                    } else {
                        Toast.makeText(this@DriverDetailsActivity, "Driver details not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DriverDetailsActivity, "Error fetching details", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val intent = Intent(this@DriverDetailsActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

