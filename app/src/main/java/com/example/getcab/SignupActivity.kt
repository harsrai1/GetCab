package com.example.getcab

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.getcab.databinding.ActivitySignupBinding
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Gender dropdown
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        binding.genderSpinner.adapter = adapter

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // replace with your web client ID
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Button Listeners
        binding.btnSignup.setOnClickListener { registerUser() }
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnGoogleSignUp.setOnClickListener { signInWithGoogle() }
    }

    private fun registerUser() {
        val name = binding.name.text.toString().trim()
        val gender = binding.genderSpinner.selectedItem.toString()
        val phoneNumber = binding.phoneNumberInput.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (name.isEmpty() || gender == "Select Gender" || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()?.addOnSuccessListener {
                        saveUserData(user.uid, name, gender, phoneNumber, email)

                        Toast.makeText(this, "Verification link sent to your email.", Toast.LENGTH_LONG).show()
                        startEmailVerificationWatcher(user)

                    }?.addOnFailureListener {
                        Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startEmailVerificationWatcher(user: FirebaseUser) {
        val handler = android.os.Handler(mainLooper)
        val interval: Long = 3000 // check every 3 seconds
        val maxAttempts = 60 // for 3 minutes max (3*60 = 180 sec)

        var attempts = 0

        val checkRunnable = object : Runnable {
            override fun run() {
                user.reload().addOnSuccessListener {
                    if (user.isEmailVerified) {
                        Toast.makeText(this@SignupActivity, "Email verified!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                        finish()
                    } else {
                        attempts++
                        if (attempts < maxAttempts) {
                            handler.postDelayed(this, interval)
                        } else {
                            Toast.makeText(this@SignupActivity, "Verification timeout. Try again.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    }
                }
            }
        }

        handler.postDelayed(checkRunnable, interval)
    }



    private fun saveUserData(uid: String, name: String, gender: String, phone: String, email: String) {
        val userMap = hashMapOf(
            "userId" to uid,
            "name" to name,
            "gender" to gender,
            "phoneNumber" to phone,
            "email" to email
        )
        firestore.collection("users").document(uid).set(userMap)
            .addOnSuccessListener {
                Log.d("Signup", "User data stored successfully")
            }
            .addOnFailureListener {
                Log.e("Signup", "Failed to store user data: ${it.message}")
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val isNew = task.result?.additionalUserInfo?.isNewUser ?: false
                        if (isNew) {
                            saveUserData(
                                uid = user.uid,
                                name = user.displayName ?: "",
                                gender = "Not Provided",
                                phone = user.phoneNumber ?: "N/A",
                                email = user.email ?: ""
                            )
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Google Auth failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
