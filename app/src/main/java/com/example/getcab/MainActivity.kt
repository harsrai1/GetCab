package com.example.getcab

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set status bar color to white and icons to black
        window.apply {
            decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = android.graphics.Color.WHITE
        }
    }

    // Function to handle the search bar click
    fun openSelectLocation(view: View) {
        // Start SelectLocationActivity when the button or view is clicked
        val intent = Intent(this, SelectLocationActivity::class.java)
        startActivity(intent)
    }
}
