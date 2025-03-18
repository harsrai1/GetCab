//package com.example.getcab
//
//import android.app.ProgressDialog
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.location.Geocoder
//import android.os.Bundle
//import android.widget.Button
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.LatLngBounds
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.android.gms.maps.model.PolylineOptions
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.firebase.database.*
//import com.google.maps.android.SphericalUtil
//import java.util.Locale
//
//class SelectVehicleActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var googleMap: GoogleMap
//    private lateinit var currentLocation: String
//    private lateinit var dropLocation: String
//    private var distanceInKilometers: Double = 0.0
//
//    // UI Elements
//    private lateinit var autoPriceText: TextView
//    private lateinit var cabEconomyPriceText: TextView
//    private lateinit var cabPremiumPriceText: TextView
//    private lateinit var bookAutoButton: Button
//
//    // For highlighting selected option
//    private lateinit var autoOptionLayout: LinearLayout
//    private lateinit var cabEconomyOptionLayout: LinearLayout
//    private lateinit var cabPremiumOptionLayout: LinearLayout
//
//    private var selectedOption: String? = null
//
//    // Store prices for each transport option in a map
//    private val priceMap = mutableMapOf<String, Double>()
//
//    // Variable for current location
//    private var currentLatLng: LatLng? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_select_vehicle)
//
//        // Retrieve location data from the intent
//        currentLocation = intent.getStringExtra("CURRENT_LOCATION").orEmpty()
//        dropLocation = intent.getStringExtra("DROP_LOCATION").orEmpty()
//
//        // Initialize UI Elements
//        autoPriceText = findViewById(R.id.auto_price_text)
//        cabEconomyPriceText = findViewById(R.id.cab_economy_price_text)
//        cabPremiumPriceText = findViewById(R.id.cab_premium_price_text)
//        bookAutoButton = findViewById(R.id.book_auto_button)
//
//        // Set up the map fragment
//        val mapFragment = supportFragmentManager
//            .findFragmentById(R.id.map_fragment) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        // Initialize layouts for transport options
//        autoOptionLayout = findViewById(R.id.auto_option)
//        cabEconomyOptionLayout = findViewById(R.id.cab_economy_option)
//        cabPremiumOptionLayout = findViewById(R.id.cab_premium_option)
//
//        // Set click listeners for transport options
//        autoOptionLayout.setOnClickListener {
//            selectOption("Auto")
//            highlightOption(autoOptionLayout)
//        }
//
//        cabEconomyOptionLayout.setOnClickListener {
//            selectOption("Cab Economy")
//            highlightOption(cabEconomyOptionLayout)
//        }
//
//        cabPremiumOptionLayout.setOnClickListener {
//            selectOption("Cab Premium")
//            highlightOption(cabPremiumOptionLayout)
//        }
//    }
//
//    override fun onMapReady(map: GoogleMap) {
//        googleMap = map
//
//        // Geocode addresses to get LatLng
//        val currentLatLng = geocodeAddress(currentLocation) ?: LatLng(0.0, 0.0) // Default to (0.0, 0.0)
//        val dropLatLng = geocodeAddress(dropLocation) ?: LatLng(0.0, 0.0)
//
//        // Get the full addresses
//        val currentAddress = getAddressFromLatLng(currentLatLng)
//        val dropAddress = getAddressFromLatLng(dropLatLng)
//
//        // Add custom markers with the full address as title
//        googleMap.addMarker(MarkerOptions()
//            .position(currentLatLng)
//            .title(currentAddress)
//            .icon(BitmapDescriptorFactory.fromBitmap(getScaledBitmap(R.drawable.pinstart, 100, 100))))  // Resize and set custom marker for current location
//
//        googleMap.addMarker(MarkerOptions()
//            .position(dropLatLng)
//            .title(dropAddress)
//            .icon(BitmapDescriptorFactory.fromBitmap(getScaledBitmap(R.drawable.pindestination, 100, 100))))  // Resize and set custom marker for destination location
//
//        // Draw a route between the locations
//        val polylineOptions = PolylineOptions().add(currentLatLng).add(dropLatLng)
//        googleMap.addPolyline(polylineOptions)
//
//        // Calculate bounds to include both the current and destination points
//        val bounds = LatLngBounds.Builder()
//            .include(currentLatLng)
//            .include(dropLatLng)
//            .build()
//
//        // Move camera to show both locations, adjusting the zoom level automatically to fit them in view
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
//
//        // Calculate the distance between the current and drop locations in kilometers
//        distanceInKilometers = calculateDistance(currentLatLng, dropLatLng)
//
//        // Calculate the price for each transport option
//        calculatePrices()
//    }
//
//    // Function to scale the image to a specific width and height
//    private fun getScaledBitmap(resourceId: Int, width: Int, height: Int): Bitmap {
//        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
//        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
//    }
//
//    // Geocode the address and get LatLng
//    private fun geocodeAddress(address: String): LatLng? {
//        return try {
//            val geocoder = Geocoder(this, Locale.getDefault())
//            val addresses = geocoder.getFromLocationName(address, 1)
//
//            addresses?.firstOrNull()?.let {
//                LatLng(it.latitude, it.longitude)
//            } ?: run {
//                null
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//
//    // Get full address from LatLng
//    private fun getAddressFromLatLng(latLng: LatLng): String {
//        return try {
//            val geocoder = Geocoder(this, Locale.getDefault())
//            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
//
//            if (!addresses.isNullOrEmpty()) {
//                addresses[0].getAddressLine(0) ?: "Unknown Address"
//            } else {
//                "Unknown Address"
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            "Unable to fetch address"
//        }
//    }
//
//    // Calculate the distance between two LatLng points in kilometers
//    private fun calculateDistance(start: LatLng, end: LatLng): Double {
//        // Use SphericalUtil to calculate the distance in meters
//        val distanceInMeters = SphericalUtil.computeDistanceBetween(start, end)
//        // Convert distance to kilometers and return
//        return distanceInMeters / 1000.0
//    }
//
//    // Calculate the prices for each transport option based on distance
//    private fun calculatePrices() {
//        // Price per kilometer (in ₹)
//        val autoPricePerKm = 10.0
//        val cabEconomyPricePerKm = 13.0
//        val cabPremiumPricePerKm = 16.0
//
//        // Calculate the prices and store them in the map
//        priceMap["Auto"] = autoPricePerKm * distanceInKilometers
//        priceMap["Cab Economy"] = cabEconomyPricePerKm * distanceInKilometers
//        priceMap["Cab Premium"] = cabPremiumPricePerKm * distanceInKilometers
//
//        // Update the UI with the calculated prices
//        autoPriceText.text = "₹${"%.2f".format(priceMap["Auto"])}"
//        cabEconomyPriceText.text = "₹${"%.2f".format(priceMap["Cab Economy"])}"
//        cabPremiumPriceText.text = "₹${"%.2f".format(priceMap["Cab Premium"])}"
//    }
//
//    // Handle the selection of the transport option
//// Handle the selection of the transport option
//    private fun selectOption(option: String) {
//        if (selectedOption == option) return // Prevent unnecessary updates
//
//        selectedOption = option
//        bookAutoButton.setOnClickListener {
//            Toast.makeText(this, "Booking $option", Toast.LENGTH_SHORT).show()
//
//            // Show progress dialog while searching for the nearest driver
//            val progressDialog = ProgressDialog(this)
//            progressDialog.setMessage("Searching for nearest driver...")
//            progressDialog.setCancelable(false)
//            progressDialog.show()
//
//            // Fetch nearest driver
//            val database = FirebaseDatabase.getInstance().getReference("drivers")
//            database.orderByChild("status").equalTo("on").addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val drivers = mutableListOf<Driver>()
//                    for (driverSnapshot in snapshot.children) {
//                        val driver = driverSnapshot.getValue(Driver::class.java)
//                        if (driver != null) drivers.add(driver)
//                    }
//
//                    if (drivers.isNotEmpty()) {
//                        val nearestDriver = findNearestDriver(drivers)
//                        progressDialog.dismiss()
//
//                        if (nearestDriver != null) {
//                            // Add driver's car on the map
//                            val carIcon = BitmapDescriptorFactory.fromBitmap(getScaledBitmap(R.drawable.car, 100, 100))
//                            val driverLatLng = LatLng(nearestDriver.latitude, nearestDriver.longitude)
//                            googleMap.addMarker(
//                                MarkerOptions()
//                                    .position(driverLatLng)
//                                    .title("Driver: ${nearestDriver.name}")
//                                    .snippet("Car: ${nearestDriver.car_name}")
//                                    .icon(carIcon)
//                            )
//
//                            // Open DriverDetailsActivity with driver details
//                            val intent = Intent(this@SelectVehicleActivity, DriverDetailsActivity::class.java).apply {
//                                putExtra("DRIVER_NAME", nearestDriver.name)
//                                putExtra("DRIVER_PHONE", nearestDriver.phone_number)
//                                putExtra("DRIVER_CAR_NAME", nearestDriver.car_name)
//                                putExtra("DRIVER_CAR_NUMBER", nearestDriver.car_number)
//                                putExtra("DISTANCE", calculateDistance(currentLatLng!!, driverLatLng))
//                            }
//                            startActivity(intent)
//                        } else {
//                            Toast.makeText(this@SelectVehicleActivity, "No drivers available nearby.", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        progressDialog.dismiss()
//                        Toast.makeText(this@SelectVehicleActivity, "No available drivers nearby.", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    progressDialog.dismiss()
//                    Toast.makeText(this@SelectVehicleActivity, "Error occurred. Try again later.", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//    }
//
//
//    // Find the nearest driver
//    private fun findNearestDriver(drivers: List<Driver>): Driver? {
//        var nearestDriver: Driver? = null
//        var minDistance = Double.MAX_VALUE
//
//        // Loop through the list of drivers to find the nearest one
//        for (driver in drivers) {
//            val driverLocation = LatLng(driver.latitude, driver.longitude)
//            val userLocation = currentLatLng ?: continue  // Ensure currentLatLng is non-null
//
//            // Calculate the distance between the user and the driver
//            val distance = calculateDistance(userLocation, driverLocation)
//
//            // Find the nearest driver
//            if (distance < minDistance) {
//                minDistance = distance
//                nearestDriver = driver
//            }
//        }
//        return nearestDriver
//    }
//
//    // Highlight selected option
//    private fun highlightOption(selectedOption: LinearLayout) {
//        // Reset the border of all options
//        resetBorders()
//
//        // Set black border around the selected option
//        selectedOption.setBackgroundResource(R.drawable.black_border)
//    }
//
//    // Reset borders of all options
//    private fun resetBorders() {
//        autoOptionLayout.setBackgroundResource(0)
//        cabEconomyOptionLayout.setBackgroundResource(0)
//        cabPremiumOptionLayout.setBackgroundResource(0)
//    }
//}
//
//
//
//
//
//
//
//
//
//







package com.example.getcab

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.*
import com.google.maps.android.SphericalUtil
import java.util.*


class SelectVehicleActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var currentLocation: String
    private lateinit var dropLocation: String
    private var distanceInKilometers: Double = 0.0

    private lateinit var autoPriceText: TextView
    private lateinit var cabEconomyPriceText: TextView
    private lateinit var cabPremiumPriceText: TextView
    private lateinit var bookButton: Button

    private lateinit var autoOptionLayout: LinearLayout
    private lateinit var cabEconomyOptionLayout: LinearLayout
    private lateinit var cabPremiumOptionLayout: LinearLayout

    private var selectedOption: String? = null

    // Firebase Database reference
    private lateinit var database: DatabaseReference

    // For driver locations
    private val driverList = mutableListOf<Driver>()
    private val handler = Handler()
    private val updateInterval: Long = 5000 // Interval to update driver data

    // Variable for current location
    private var currentLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_vehicle)

        currentLocation = intent.getStringExtra("CURRENT_LOCATION").orEmpty()
        dropLocation = intent.getStringExtra("DROP_LOCATION").orEmpty()



        window.apply {
            decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = android.graphics.Color.WHITE
        }



        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance().getReference("drivers")

        autoPriceText = findViewById(R.id.auto_price_text)
        cabEconomyPriceText = findViewById(R.id.cab_economy_price_text)
        cabPremiumPriceText = findViewById(R.id.cab_premium_price_text)
        bookButton = findViewById(R.id.book_auto_button)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        autoOptionLayout = findViewById(R.id.auto_option)
        cabEconomyOptionLayout = findViewById(R.id.cab_economy_option)
        cabPremiumOptionLayout = findViewById(R.id.cab_premium_option)

        // Set click listeners for transport options
        autoOptionLayout.setOnClickListener {
            selectOption("Auto")
            highlightOption(autoOptionLayout)
        }

        cabEconomyOptionLayout.setOnClickListener {
            selectOption("Cab Economy")
            highlightOption(cabEconomyOptionLayout)
        }

        cabPremiumOptionLayout.setOnClickListener {
            selectOption("Cab Premium")
            highlightOption(cabPremiumOptionLayout)
        }

        // Start fetching driver data
        startFetchingDriverLocations()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Geocode addresses to get LatLng
        val currentLatLng = geocodeAddress(currentLocation) ?: LatLng(0.0, 0.0) // Default to (0.0, 0.0)
        val dropLatLng = geocodeAddress(dropLocation) ?: LatLng(0.0, 0.0)

        // Get the full addresses
        val currentAddress = getAddressFromLatLng(currentLatLng)
        val dropAddress = getAddressFromLatLng(dropLatLng)

        // Add custom markers with the full address as title
        googleMap.addMarker(MarkerOptions()
            .position(currentLatLng)
            .title(currentAddress)
            .icon(BitmapDescriptorFactory.fromBitmap(getScaledBitmap(R.drawable.pinstart, 100, 100))))  // Custom marker for current location

        googleMap.addMarker(MarkerOptions()
            .position(dropLatLng)
            .title(dropAddress)
            .icon(BitmapDescriptorFactory.fromBitmap(getScaledBitmap(R.drawable.pindestination, 100, 100))))  // Custom marker for drop location

        // Move camera to the current location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

        // Draw a route between the locations
        val polylineOptions = PolylineOptions().add(currentLatLng).add(dropLatLng)
        googleMap.addPolyline(polylineOptions)

        // Calculate bounds to include both the current and destination points
        val bounds = LatLngBounds.Builder()
            .include(currentLatLng)
            .include(dropLatLng)
            .build()

        // Move camera to show both locations, adjusting the zoom level automatically to fit them in view
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

        // Calculate the distance between the current and drop locations in kilometers
        distanceInKilometers = calculateDistance(currentLatLng, dropLatLng)

        // Calculate the price for each transport option
        calculatePrices()
    }

    // Function to scale the image to a specific width and height
    private fun getScaledBitmap(resourceId: Int, width: Int, height: Int): Bitmap {
        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    }

    // Get full address from LatLng
    private fun getAddressFromLatLng(latLng: LatLng): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Unknown Address"
            } else {
                "Unknown Address"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Unable to fetch address"
        }
    }

    // Calculate the distance between two LatLng points in kilometers
    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        // Use SphericalUtil to calculate the distance in meters
        val distanceInMeters = SphericalUtil.computeDistanceBetween(start, end)
        // Convert distance to kilometers and return
        return distanceInMeters / 1000.0
    }

    private fun geocodeAddress(address: String): LatLng? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val locationList = geocoder.getFromLocationName(address, 1)
            if (!locationList.isNullOrEmpty()) {
                LatLng(locationList[0].latitude, locationList[0].longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculatePrices() {
        val autoPrice = distanceInKilometers * 10
        val economyPrice = distanceInKilometers * 15
        val premiumPrice = distanceInKilometers * 20

        autoPriceText.text = "₹%.2f".format(autoPrice)
        cabEconomyPriceText.text = "₹%.2f".format(economyPrice)
        cabPremiumPriceText.text = "₹%.2f".format(premiumPrice)

        bookButton.setOnClickListener {
            if (selectedOption != null) {
                val intent = Intent(this, WaitingActivity::class.java)

                // Pass the selected option
                intent.putExtra("SELECTED_OPTION", selectedOption)

                // Pass the current location (latitude and longitude)
                intent.putExtra("CURRENT_LOCATION_STRING", currentLocation)
                intent.putExtra("CURRENT_LATITUDE", currentLatLng?.latitude)
                intent.putExtra("CURRENT_LONGITUDE", currentLatLng?.longitude)

                // Pass the destination location (latitude and longitude)
                intent.putExtra("DROP_LOCATION_STRING", dropLocation)
                val dropLatLng = geocodeAddress(dropLocation) // Use the same geocoding logic to get drop location LatLng
                intent.putExtra("DROP_LATITUDE", dropLatLng?.latitude)
                intent.putExtra("DROP_LONGITUDE", dropLatLng?.longitude)

                // Pass the price for the selected option
                when (selectedOption) {
                    "Auto" -> intent.putExtra("PRICE", autoPriceText.text.toString())
                    "Cab Economy" -> intent.putExtra("PRICE", cabEconomyPriceText.text.toString())
                    "Cab Premium" -> intent.putExtra("PRICE", cabPremiumPriceText.text.toString())
                }

                // Pass the distance to the WaitingActivity
                intent.putExtra("DISTANCE", distanceInKilometers)

                // Start the WaitingActivity with all the data
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select a vehicle option!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun selectOption(option: String) {
        selectedOption = option
        Toast.makeText(this, "$option Selected", Toast.LENGTH_SHORT).show()
    }

    // Highlight selected option
    private fun highlightOption(selectedOption: LinearLayout) {
        // Reset the border of all options
        resetBorders()

        // Set black border around the selected option
        selectedOption.setBackgroundResource(R.drawable.black_border)
    }

    // Reset borders of all options
    private fun resetBorders() {
        autoOptionLayout.setBackgroundResource(0)
        cabEconomyOptionLayout.setBackgroundResource(0)
        cabPremiumOptionLayout.setBackgroundResource(0)
    }

    // Start fetching driver locations periodically
    private fun startFetchingDriverLocations() {
        handler.post(object : Runnable {
            override fun run() {
                loadDriverData()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    // Fetch driver data from Firebase Realtime Database
    private fun loadDriverData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@SelectVehicleActivity, "No drivers found", Toast.LENGTH_SHORT).show()
                    return
                }

                driverList.clear()

                for (child in snapshot.children) {
                    val driver = child.getValue(Driver::class.java)
                    if (driver != null) {
                        driverList.add(driver)
                    }
                }

                showDriverLocations()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SelectVehicleActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Show driver locations on the map
    private fun showDriverLocations() {
        // Use a separate call to ensure the current and drop location markers are not cleared
        val carBitmap = BitmapFactory.decodeResource(resources, R.drawable.car)
        val scaledCarBitmap = Bitmap.createScaledBitmap(carBitmap, 100, 100, false)
        val carIcon = BitmapDescriptorFactory.fromBitmap(scaledCarBitmap)

        for (driver in driverList) {
            val driverLatLng = LatLng(driver.latitude, driver.longitude)

            googleMap.addMarker(
                MarkerOptions()
                    .position(driverLatLng)
                    .title(driver.name)
                    .snippet(driver.car_name)
                    .icon(carIcon)
            )
        }
    }
}


