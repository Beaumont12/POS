package com.example.pos

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class SettingsFragment : Fragment(), View.OnClickListener {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var staffNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireContext().getSharedPreferences("user_account", MODE_PRIVATE)
        database = FirebaseDatabase.getInstance().reference

        profileImageView = view.findViewById(R.id.user_image)
        staffNameTextView = view.findViewById(R.id.staff_name)

        // Replace the default fragment with InfoFragment
        replaceFragment(InfoFragment())

        // Set click listeners for the buttons
        view.findViewById<AppCompatButton>(R.id.info_button).setOnClickListener(this)
        view.findViewById<AppCompatButton>(R.id.developers_button).setOnClickListener(this)
        view.findViewById<AppCompatButton>(R.id.about_button).setOnClickListener(this)
        view.findViewById<AppCompatButton>(R.id.logout_button).setOnClickListener(this)

        // Fetch user details and set profile image and staff name
        fetchUserDetails()

        return view
    }

    private fun fetchUserDetails() {
        val loggedInUserName = sharedPreferences.getString("loggedInUserName", "")

        // Query the database for the user details
        val query: Query = database.child("staffs").orderByChild("Name").equalTo(loggedInUserName)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (staffSnapshot in dataSnapshot.children) {
                    val user = staffSnapshot.getValue(InfoFragment.User::class.java)
                    if (user != null) {
                        // Set profile image
                        if (user.ImageUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(user.ImageUrl)
                                .into(profileImageView)
                        }

                        // Set staff name
                        staffNameTextView.text = user.Name
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("SettingsFragment", "Database error: ${databaseError.message}")
            }
        })
    }

    private fun logout() {
        // Clear the stored user data
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Redirect the user to the login screen
        val intent = Intent(requireContext(), Login::class.java)
        startActivity(intent)
        requireActivity().finish() // Optional: finish the current activity

        Log.d("Logout", "Logout button clicked. Logging out user.")
    }

    override fun onClick(v: View?) {
        // Replace the fragment in the frame layout based on the button clicked
        when (v?.id) {
            R.id.info_button -> replaceFragment(InfoFragment())
            R.id.developers_button -> replaceFragment(StatsFragment())
            R.id.about_button -> replaceFragment(AboutFragment())
            R.id.logout_button -> logout()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.settings_frame, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}