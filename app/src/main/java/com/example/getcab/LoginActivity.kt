package com.example.getcab

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.getcab.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Gender Spinner
        val genders = arrayOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.genderSpinner.adapter = adapter

        // Handle Continue Button Click
        binding.continueButton.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val phoneNumber = binding.phoneNumberInput.text.toString().trim()
            val selectedGender = binding.genderSpinner.selectedItem.toString()

            // Validate Inputs
            if (name.isEmpty()) {
                binding.name.error = "Name is required"
                return@setOnClickListener
            }

            if (selectedGender == "Select Gender") {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phoneNumber.isEmpty() || phoneNumber.length < 10) {
                binding.phoneNumberInput.error = "Enter a valid phone number"
                return@setOnClickListener
            }

            // Simulating Login Process (Replace this with OTP verification logic)
            Toast.makeText(this, "Logging in as $name ($selectedGender)", Toast.LENGTH_SHORT).show()
        }
    }
}

