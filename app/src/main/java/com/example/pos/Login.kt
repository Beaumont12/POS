package com.example.pos

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var idField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var seePasswordButton: ImageButton
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = FirebaseDatabase.getInstance()

        idField = findViewById(R.id.id_field)
        emailField = findViewById(R.id.email_field)
        passwordField = findViewById(R.id.password_field)
        seePasswordButton = findViewById(R.id.see_password)
        loginButton = findViewById(R.id.login_button)

        // Initially set the password field to use PasswordTransformationMethod
        passwordField.transformationMethod = PasswordTransformationMethod.getInstance()

        loginButton.setOnClickListener {
            val staffId = idField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (staffId.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(staffId, email, password)
            } else {
                Toast.makeText(this, "Please enter staff ID, email, and password", Toast.LENGTH_SHORT).show()
            }
        }

        seePasswordButton.setOnClickListener {
            // Toggle password visibility
            if (passwordField.transformationMethod == null) {
                // Password is visible, hide it
                passwordField.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                // Password is hidden, show it
                passwordField.transformationMethod = null
            }
        }
    }

    private fun loginUser(staffId: String, email: String, password: String) {

        Log.d("Login", "loginUser function called")

        val reference: DatabaseReference = database.reference.child("staffs")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                Log.d("Login", "Number of children: ${snapshot.childrenCount}")
                var loggedIn = false
                snapshot.children.forEach { staffSnapshot ->
                    val staffKey = staffSnapshot.key // Get the staff key (e.g., staff_1)
                    if (staffKey == staffId) {
                        val staffEmail = staffSnapshot.child("Email").value.toString()
                        val staffPassword = staffSnapshot.child("Password").value.toString()
                        if (email == staffEmail && password == staffPassword) {
                            // Email and password match, login successful
                            loggedIn = true
                            // Retrieve other user data if needed
                            val age = staffSnapshot.child("Age").value.toString()
                            val name = staffSnapshot.child("Name").value.toString()

                            val intent = Intent(this@Login, MainActivity::class.java)
                            intent.putExtra("loggedInUserName", name)
                            intent.putExtra("loggedInUserAge", age)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                if (!loggedIn) {
                    // No staff found with matching staff ID, email, and password
                    Toast.makeText(baseContext, "Invalid staff ID, email, or password.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(baseContext, "Failed to read data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveLoggedInUserName(name: String) {
        val editor = sharedPreferences.edit()
        editor.putString("loggedInUserName", name)
        editor.apply()
    }
}