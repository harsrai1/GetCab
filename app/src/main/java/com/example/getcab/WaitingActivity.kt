//package com.example.getcab
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.database.*
//import com.google.android.gms.maps.model.LatLng
//import kotlin.math.*
//
//class WaitingActivity : AppCompatActivity() {
//
//    private var selectedOption: String? = null
//    private var price: String? = null
//    private var distance: Double = 0.0
//    private var currentLatitude: Double = 0.0
//    private var currentLongitude: Double = 0.0
//    private var dropLatitude: Double = 0.0
//    private var dropLongitude: Double = 0.0
//    private var driverList = mutableListOf<Driver>()
//    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("drivers") // Firebase reference
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_waiting)
//
//        // Retrieve the data passed from SelectVehicleActivity
//        selectedOption = intent.getStringExtra("SELECTED_OPTION") ?: "Unknown Option"
//        price = intent.getStringExtra("PRICE") ?: "₹0.00"
//        distance = intent.getDoubleExtra("DISTANCE", 0.0)
//        currentLatitude = intent.getDoubleExtra("CURRENT_LATITUDE", 0.0)
//        currentLongitude = intent.getDoubleExtra("CURRENT_LONGITUDE", 0.0)
//        dropLatitude = intent.getDoubleExtra("DROP_LATITUDE", 0.0)
//        dropLongitude = intent.getDoubleExtra("DROP_LONGITUDE", 0.0)
//
//        // Initialize the TextViews to display data
//        val selectedOptionText = findViewById<TextView>(R.id.selected_option_text)
//        val priceText = findViewById<TextView>(R.id.price_text)
//        val distanceText = findViewById<TextView>(R.id.distance_text)
//
//        // Set the data to the TextViews
//        selectedOptionText.text = "Selected Option: $selectedOption"
//        priceText.text = "Price: $price"
//        distanceText.text = "Distance: %.2f km".format(distance)
//
//        // Start fetching driver locations from Firebase
//        startFetchingDriverLocations()
//    }
//
//    private fun startFetchingDriverLocations() {
//        // Fetch driver data from Firebase Realtime Database
//        database.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (!snapshot.exists()) {
//                    Toast.makeText(this@WaitingActivity, "No drivers found", Toast.LENGTH_SHORT).show()
//                    return
//                }
//
//                driverList.clear() // Clear any existing driver data
//
//                for (child in snapshot.children) {
//                    val driver = child.getValue(Driver::class.java)
//                    if (driver != null) {
//                        driverList.add(driver) // Add driver to list
//                    }
//                }
//
//                // Automatically show the nearest driver's details after fetching the data
//                openNearestDriverDetails()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@WaitingActivity, "Error fetching driver data: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//    private fun openNearestDriverDetails() {
//        if (driverList.isEmpty()) {
//            Toast.makeText(this, "No drivers available", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Variables to keep track of the nearest driver
//        var nearestDriver: Driver? = null
//        var shortestDistance = Double.MAX_VALUE
//
//        // Loop through the driver list to find the nearest driver
//        for (driver in driverList) {
//            // Convert latitude and longitude (from String) to Double
//            val driverLatitude = driver.latitude // no need for conversion
//            val driverLongitude = driver.longitude // no need for conversion
//
//
//            // Create LatLng objects for the current user location and the driver's location
//            val userLatLng = LatLng(currentLatitude, currentLongitude)
//            val driverLatLng = LatLng(driverLatitude, driverLongitude)
//
//            // Calculate the distance between the user and the driver
//            val calculatedDistance = calculateDistance(userLatLng, driverLatLng)
//
//            // Check if this driver is closer than the previous one
//            if (calculatedDistance < shortestDistance) {
//                shortestDistance = calculatedDistance
//                nearestDriver = driver
//            }
//        }
//
//        // If a nearest driver is found, open the details
//        nearestDriver?.let {
//            val intent = Intent(this, DriverDetailsActivity::class.java).apply {
//                putExtra("driverName", it.name)
//                putExtra("driverLat", it.latitude)
//                putExtra("driverLng", it.longitude)
//                putExtra("distance", shortestDistance)  // Pass the calculated distance
//                putExtra("carName", it.car_name)
//                putExtra("carNumber", it.car_number)
//                putExtra("phoneNumber", it.phone_number)
//            }
//            startActivity(intent)
//        } ?: run {
//            Toast.makeText(this, "No nearby drivers found", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun calculateDistance(userLatLng: LatLng, driverLatLng: LatLng): Double {
//        val earthRadius = 6371 // Radius of the Earth in km
//        val dLat = Math.toRadians(driverLatLng.latitude - userLatLng.latitude)
//        val dLng = Math.toRadians(driverLatLng.longitude - userLatLng.longitude)
//        val a = Math.sin(dLat / 2).pow(2.0) + Math.cos(Math.toRadians(userLatLng.latitude)) *
//                Math.cos(Math.toRadians(driverLatLng.latitude)) * Math.sin(dLng / 2).pow(2.0)
//        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
//        return earthRadius * c
//    }
//}
//
//
//

package com.example.getcab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class WaitingActivity : AppCompatActivity() {

    private var selectedOption: String? = null
    private var price: String? = null
    private var distance: Double = 0.0
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var dropLatitude: Double = 0.0
    private var dropLongitude: Double = 0.0
    private var otp: String? = null
    private lateinit var rideRequestRef: DatabaseReference
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("fetchingdriver")
    private val driversDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference.child("drivers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        selectedOption = intent.getStringExtra("SELECTED_OPTION") ?: "Unknown Option"
        price = intent.getStringExtra("PRICE") ?: "₹0.00"
        distance = intent.getDoubleExtra("DISTANCE", 0.0)
        currentLatitude = intent.getDoubleExtra("CURRENT_LATITUDE", 0.0)
        currentLongitude = intent.getDoubleExtra("CURRENT_LONGITUDE", 0.0)
        dropLatitude = intent.getDoubleExtra("DROP_LATITUDE", 0.0)
        dropLongitude = intent.getDoubleExtra("DROP_LONGITUDE", 0.0)

        val selectedOptionText = findViewById<TextView>(R.id.selected_option_text)
        val priceText = findViewById<TextView>(R.id.price_text)
        val distanceText = findViewById<TextView>(R.id.distance_text)
        val cancelTripButton = findViewById<Button>(R.id.cancel_trip_button)

        selectedOptionText.text = "Selected Option: $selectedOption"
        priceText.text = "Price: $price"
        distanceText.text = "Distance: %.2f km".format(distance)

        createRideRequest()
        monitorRideStatus()

        cancelTripButton.setOnClickListener {
            cancelRideRequest()
        }
    }

    private fun createRideRequest() {
        rideRequestRef = database.push()

        val rideData = mapOf(
            "driverId" to null,
            "driverName" to null,
            "cabDriverName" to null,
            "carNo" to null,
            "price" to price,
            "distance" to distance.toString(),
            "pickupAddress" to "Pickup Address",
            "dropAddress" to "Drop Address",
            "status" to "pending",
            "otp" to otp
        )

        rideRequestRef.setValue(rideData)
            .addOnSuccessListener { Log.d("Firebase", "Ride request created successfully") }
            .addOnFailureListener { e -> Log.e("Firebase", "Error creating ride request: ${e.message}") }
    }

    private fun monitorRideStatus() {
        rideRequestRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                val driverName = snapshot.child("driverName").getValue(String::class.java)
                val carNo = snapshot.child("carNo").getValue(String::class.java)
                otp = snapshot.child("otp").getValue(String::class.java) ?: "0000"

                if (status == "accepted" && driverName != null && carNo != null) {
                    fetchDriverDetails(driverName, carNo, otp!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error monitoring ride status: ${error.message}")
            }
        })
    }

    private fun fetchDriverDetails(driverName: String, carNo: String, otp: String) {
        driversDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (driverSnapshot in snapshot.children) {
                    val name = driverSnapshot.child("name").getValue(String::class.java)
                    val carNumber = driverSnapshot.child("car_number").getValue(String::class.java)

                    if (name == driverName && carNumber == carNo) {
                        val carName = driverSnapshot.child("car_name").getValue(String::class.java)
                        val latitude = driverSnapshot.child("latitude").getValue(Double::class.java) ?: 0.0
                        val longitude = driverSnapshot.child("longitude").getValue(Double::class.java) ?: 0.0
                        val phoneNumber = driverSnapshot.child("phone_number").getValue(String::class.java)
                        val driverImage = driverSnapshot.child("driver_image").getValue(String::class.java)

                        val intent = Intent(this@WaitingActivity, DriverDetailsActivity::class.java).apply {
                            putExtra("driverName", driverName)
                            putExtra("driverLat", latitude)
                            putExtra("driverLng", longitude)
                            putExtra("distance", distance)
                            putExtra("carName", carName)
                            putExtra("carNumber", carNo)
                            putExtra("phoneNumber", phoneNumber)
                            putExtra("driverImage", driverImage)
                            putExtra("otp", otp)
                        }
                        startActivity(intent)
                        finish()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error fetching driver details: ${error.message}")
            }
        })
    }

    private fun cancelRideRequest() {
        rideRequestRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Trip canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error canceling trip: ${e.message}")
                Toast.makeText(this, "Failed to cancel trip", Toast.LENGTH_SHORT).show()
            }
    }
}
