package com.hollowlog.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginScreen : AppCompatActivity() {

    companion object {
        private const val ADD_USER = 0
    }

    private lateinit var welcomeText: TextView
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        welcomeText = findViewById(R.id.welcome_text)
        emailField = findViewById(R.id.login_email)
        passwordField = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        mAuth = FirebaseAuth.getInstance()

        val loginRequired = intent.getStringExtra("login")

        if (loginRequired != null) {
            Toast.makeText(this, "You must login to use this application", Toast.LENGTH_LONG).show()
        }

        if (mAuth.currentUser != null) {
            loginButton.text = "Log Out"
            welcomeText.visibility = View.VISIBLE
            welcomeText.text = "Signed in as ${mAuth.currentUser!!.email}"
            emailField.visibility = View.GONE
            passwordField.visibility = View.GONE
            registerButton.visibility = View.GONE
        }

        loginButton.setOnClickListener { login() }
        registerButton.setOnClickListener { registerUser() }
    }

    private fun login() {

        if (mAuth.currentUser != null) {
            mAuth.signOut()
            loginButton.text = "Login"
            welcomeText.visibility = View.GONE
            emailField.visibility = View.VISIBLE
            passwordField.visibility = View.VISIBLE
            registerButton.visibility = View.VISIBLE
            Toast.makeText(this, "User has been signed out", Toast.LENGTH_LONG).show()
            return
        }

        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        if (email.isEmpty()) {
            emailField.error = "Please enter your email"
            emailField.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Please enter a valid email"
            emailField.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordField.error = "Please enter your password"
            passwordField.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordField.error = "Minimum length is 6 characters"
            passwordField.requestFocus()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Successfully logged in", Toast.LENGTH_LONG).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Email or password is incorrect", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun registerUser() {
        val intent = Intent(this@LoginScreen, RegisterUser::class.java)
        startActivityForResult(intent, ADD_USER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_USER) {
                Toast.makeText(this, "Successfully logged in", Toast.LENGTH_LONG).show()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

}