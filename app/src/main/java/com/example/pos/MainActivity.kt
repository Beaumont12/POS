package com.example.pos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R. id.bot_nav)

        // Retrieve loggedInUserName from intent with default value
        val loggedInUserName = intent.getStringExtra("loggedInUserName") ?: ""

        // Pass loggedInUserName to LandingFragment when creating an instance
        val landingFragment = LandingFragment(loggedInUserName)

        // Display LandingFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, landingFragment)
            .commit()

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.orders -> {
                    replaceFragment(Order())
                    true
                }
                R.id.history -> {
                    replaceFragment(History())
                    true
                }
                R.id.menu -> {
                    replaceFragment(LandingFragment(loggedInUserName))
                    true
                }

                R.id.settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(LandingFragment(loggedInUserName))
    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}