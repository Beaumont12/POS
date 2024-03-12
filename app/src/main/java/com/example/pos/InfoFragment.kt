package com.example.pos

import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class InfoFragment : Fragment(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var loggedInUserName: String
    private lateinit var seePasswordButton: ImageButton
    private lateinit var uploadImageButton: ImageButton
    private lateinit var profileImageUri: Uri
    private var isEditing = false
    private lateinit var nameTextInputEditText: TextInputEditText
    private lateinit var ageTextInputEditText: TextInputEditText
    private lateinit var emailTextInputEditText: TextInputEditText
    private lateinit var phoneNumberTextInputEditText: TextInputEditText
    private lateinit var birthdayDateTextInputEditText: TextInputEditText
    private lateinit var birthdayMonthTextInputEditText: TextInputEditText
    private lateinit var birthdayYearTextInputEditText: TextInputEditText
    private lateinit var passwordTextInputEditText: TextInputEditText

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Handle the picked image
            profileImageUri = uri
            // Update the profile image button with the selected image
            uploadImageButton.setImageURI(profileImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_info, container, false)

        // Initialize Firebase Authentication, Realtime Database, and Storage
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance().reference

        // Retrieve logged-in user's name from shared preferences
        loggedInUserName = requireActivity().getSharedPreferences("user_account", MODE_PRIVATE)
            .getString("loggedInUserName", "") ?: ""

        // Initialize TextInputEditText fields
        ageTextInputEditText = view.findViewById(R.id.age)
        emailTextInputEditText = view.findViewById(R.id.email_field)
        phoneNumberTextInputEditText = view.findViewById(R.id.phone_number)
        birthdayDateTextInputEditText = view.findViewById(R.id.bdate)
        birthdayMonthTextInputEditText = view.findViewById(R.id.bmonth)
        birthdayYearTextInputEditText = view.findViewById(R.id.byear)
        passwordTextInputEditText = view.findViewById(R.id.password_field)
        nameTextInputEditText = view.findViewById(R.id.name)
        seePasswordButton = view.findViewById(R.id.see_password)
        uploadImageButton = view.findViewById(R.id.image_upload)

        passwordTextInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()

        // Set click listener for the edit button
        view.findViewById<Button>(R.id.edit_button).setOnClickListener(this)
        view.findViewById<Button>(R.id.confirm1_button).setOnClickListener(this)
        uploadImageButton.setOnClickListener(this)

        enableFields(false)

        // Retrieve and display user information
        retrieveUserInfo()

        seePasswordButton.setOnClickListener {
            // Toggle password visibility
            if (passwordTextInputEditText.transformationMethod == null) {
                // Password is visible, hide it
                passwordTextInputEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {
                // Password is hidden, show it
                passwordTextInputEditText.transformationMethod = null
            }
        }

        return view
    }

    private fun retrieveUserInfo() {
        val query: Query = database.child("staffs").orderByChild("Name").equalTo(loggedInUserName)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (staffSnapshot in dataSnapshot.children) {
                    val user = staffSnapshot.getValue() as? Map<String, Any>
                    if (user != null) {
                        nameTextInputEditText.setText(user["Name"].toString())
                        ageTextInputEditText.setText(user["Age"].toString())
                        emailTextInputEditText.setText(user["Email"].toString())
                        phoneNumberTextInputEditText.setText(user["Phone"].toString())
                        val birthday = user["Birthday"] as? Map<*, *>
                        if (birthday != null) {
                            birthdayDateTextInputEditText.setText(birthday["Date"].toString())
                            birthdayMonthTextInputEditText.setText(birthday["Month"].toString())
                            birthdayYearTextInputEditText.setText(birthday["Year"].toString())
                        }
                        passwordTextInputEditText.setText(user["Password"].toString())
                        val imageUrl = user["ImageUrl"].toString()
                        // Load profile image from Firebase Storage and set it to the button
                        // You can use any image loading library like Glide or Picasso here
                        // For simplicity, I'm skipping the image loading part
                        // You can add it based on your preference
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.edit_button -> {
                if (!isEditing) {
                    // Enable all fields for editing
                    enableFields(true)
                } else {
                    // Disable all fields after saving
                    enableFields(false)
                }
                // Toggle editing flag
                isEditing = !isEditing
            }
            R.id.confirm1_button -> {
                if (isEditing) {
                    // Save updated info
                    saveUpdatedInfo()
                    // Disable all fields after saving
                    enableFields(false)
                    // Toggle editing flag
                    isEditing = false
                }
            }
            R.id.image_upload -> {
                // Open the gallery to pick an image
                getContent.launch("image/*")
            }
        }
    }

    private fun enableFields(enabled: Boolean) {
        ageTextInputEditText.isEnabled = enabled
        emailTextInputEditText.isEnabled = enabled
        phoneNumberTextInputEditText.isEnabled = enabled
        birthdayDateTextInputEditText.isEnabled = enabled
        birthdayMonthTextInputEditText.isEnabled = enabled
        birthdayYearTextInputEditText.isEnabled = enabled
        passwordTextInputEditText.isEnabled = enabled
        uploadImageButton.isEnabled = enabled
    }

    private fun saveUpdatedInfo() {
        Log.d("InfoFragment", "saveUpdatedInfo() called")
        val name = nameTextInputEditText.text.toString()
        val age = ageTextInputEditText.text.toString().toInt()
        val email = emailTextInputEditText.text.toString()
        val phone = phoneNumberTextInputEditText.text.toString().toLong()
        val password = passwordTextInputEditText.text.toString()

        val date = birthdayDateTextInputEditText.text.toString().toLongOrNull()
        val month = birthdayMonthTextInputEditText.text.toString().toLongOrNull()
        val year = birthdayYearTextInputEditText.text.toString().toLongOrNull()

        // Check if profileImageUri is initialized
        if (::profileImageUri.isInitialized) {
            // Upload the profile image to Firebase Storage if profileImageUri is initialized
            uploadImageToFirebaseStorage(profileImageUri) { imageUrl ->
                // Image uploaded successfully, now save the updated user info including imageUrl
                val updatedUserInfo = User(
                    Name = name,
                    Age = age,
                    Birthday = mapOf("Date" to date, "Month" to month, "Year" to year).filterValues { it != null } as Map<String, Long>,
                    Email = email,
                    Password = password,
                    Phone = phone,
                    ImageUrl = imageUrl
                )

                updateUserInDatabase(updatedUserInfo)
            }
        } else {
            // Profile image is not selected, retain the existing image URL
            getImageUrlFromDatabase { imageUrl ->

                val updatedUserInfo = User(
                    Name = name,
                    Age = age,
                    Birthday = mapOf("Date" to date, "Month" to month, "Year" to year).filterValues { it != null } as Map<String, Long>,
                    Email = email,
                    Password = password,
                    Phone = phone,
                    ImageUrl = imageUrl
                )
                updateUserInDatabase(updatedUserInfo)
            }
        }
    }

    private fun updateUserInDatabase(user: User) {
        val userQuery = database.child("staffs").orderByChild("Name").equalTo(loggedInUserName)
        userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    userSnapshot.ref.setValue(user.toMap()) // Convert User object to map with proper keys
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun getImageUrlFromDatabase(callback: (String) -> Unit) {
        val query: Query = database.child("staffs").orderByChild("Name").equalTo(loggedInUserName)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (staffSnapshot in dataSnapshot.children) {
                    val user = staffSnapshot.getValue() as? Map<String, Any>
                    if (user != null) {
                        val imageUrl = user["ImageUrl"].toString()
                        callback(imageUrl) // Pass the imageUrl to the callback function
                        return // Exit the loop after finding the first match
                    }
                }
                callback("") // If no match is found, pass an empty string as the imageUrl
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun uploadImageToFirebaseStorage(uri: Uri, callback: (String) -> Unit) {
        val fileName = loggedInUserName // You can use any unique identifier here
        val ref = storage.child("images/$fileName")

        ref.putFile(uri)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                ref.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString()) // Pass the imageUrl to the callback function
                }
            }
            .addOnFailureListener { exception: java.lang.Exception ->
                // Handle unsuccessful uploads
                Log.e("InfoFragment", "Upload failed: $exception")
            }
    }

    data class User(
        val Name: String = "",
        val Age: Int = 0,
        val Birthday: Map<String, Long> = emptyMap(),
        val Email: String = "",
        val Password: String = "",
        val Phone: Long = 0,
        val ImageUrl: String = ""
    )

    fun User.toMap(): Map<String, Any?> {
        return mapOf(
            "Name" to this.Name,
            "Age" to this.Age,
            "Birthday" to this.Birthday,
            "Email" to this.Email,
            "Password" to this.Password,
            "Phone" to this.Phone,
            "ImageUrl" to this.ImageUrl
        )
    }
}