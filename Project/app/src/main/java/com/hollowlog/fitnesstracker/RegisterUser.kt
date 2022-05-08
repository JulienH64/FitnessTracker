package com.hollowlog.fitnesstracker

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterUser : AppCompatActivity() {

    private lateinit var nameField: TextView
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var verifyPasswordField: EditText
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var mDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_user)

        nameField = findViewById(R.id.register_full_name)
        emailField = findViewById(R.id.register_email)
        passwordField = findViewById(R.id.register_password)
        verifyPasswordField = findViewById(R.id.verify_register_password)
        registerButton = findViewById(R.id.register_screen_button)
        progressBar = findViewById(R.id.progress_bar)

        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener { registerUser() }
    }

    /**
     * Verify that the user typed in all of the text fields correctly.
     */
    private fun registerUser() {
        val name = nameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val verifyPassword = verifyPasswordField.text.toString().trim()

        // check if the user entered a name
        if (name.isEmpty()) {
            nameField.error = "Name is required"
            nameField.requestFocus()
            return
        }

        // check if the user entered an email
        if (email.isEmpty()) {
            emailField.error = "Email is required"
            emailField.requestFocus()
            return
        }

        // check if the user entered a valid email address
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Please provide valid email"
            emailField.requestFocus()
            return
        }

        // check if the user entered a password
        if (password.isEmpty()) {
            passwordField.error = "Password is required"
            passwordField.requestFocus()
            return
        }

        // Check if the password is greater than 6 characters
        if (password.length < 6) {
            passwordField.error = "Minimum length is 6 characters"
            passwordField.requestFocus()
            return
        }

        // check if the user verified their password
        if (verifyPassword.isEmpty()) {
            verifyPasswordField.error = "Please verify your password"
            verifyPasswordField.requestFocus()
            return
        }

        // check if the passwords match each other
        if (password != verifyPassword) {
            verifyPasswordField.error = "Passwords do not match"
            verifyPasswordField.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                val user = User(name, email)

                mDatabase.child("User").child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .setValue(user).addOnSuccessListener {
                        Toast.makeText(this, "User registered successfully", Toast.LENGTH_LONG)
                            .show()

                        loadBasicUserDatabase()

                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to register user", Toast.LENGTH_LONG).show()
                        progressBar.visibility = View.GONE
                    }
            } else {
                Toast.makeText(this, "Failed to register user", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
            }
        }
    }

    // Used to load the newly created user with the basic routines, exercises, etc.
    private fun loadBasicUserDatabase() {

        mDatabase.child("Initial").get().addOnSuccessListener {
            val map = it.value as HashMap<*, *>

            val routines = map["Routines"]
            val exercises = map["Exercises"]
            val muscles = map["Muscle Groups"]

            mDatabase.child("User").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Routines").setValue(routines)
            mDatabase.child("User").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Exercises").setValue(exercises)
            mDatabase.child("User").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Muscle Groups").setValue(muscles)
        }

        progressBar.visibility = View.GONE
        setResult(RESULT_OK)
        finish()
    }
}