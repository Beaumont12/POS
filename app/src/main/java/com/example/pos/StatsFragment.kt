package com.example.pos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatsFragment : Fragment() {

    private lateinit var devlink1: TextView
    private lateinit var devlink11: TextView
    private lateinit var devlink2: TextView
    private lateinit var devlink22: TextView
    private lateinit var devlink3: TextView
    private lateinit var devlink33: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TextViews
        devlink1 = view.findViewById(R.id.devlink1)
        devlink11 = view.findViewById(R.id.devlink11)
        devlink2 = view.findViewById(R.id.devlink2)
        devlink22 = view.findViewById(R.id.devlink22)
        devlink3 = view.findViewById(R.id.devlink3)
        devlink33 = view.findViewById(R.id.devlink33)

        // Set click listeners for TextViews
        devlink1.setOnClickListener {
            openUrl("https://www.linkedin.com/in/imrelgin/")
        }

        devlink11.setOnClickListener {
            openUrl("https://github.com/Beaumont12")
        }

        devlink2.setOnClickListener {
            openUrl("https://www.linkedin.com/feed/")
        }

        devlink22.setOnClickListener {
            openUrl("https://github.com/06flynn")
        }

        devlink3.setOnClickListener {
            openUrl("https://www.linkedin.com/in/sheena-mechaela-basiga-a31336296/")
        }

        devlink33.setOnClickListener {
            openUrl("https://github.com/Beaumont12")
        }
    }

    // Method to open URL
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}