package com.thirdgrade.cofee


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thirdgrade.cofee.adapter.ProfileAdapter
import com.thirdgrade.cofee.model.UserProfile
import com.thirdgrade.cofee.service.ProfileService
import com.thirdgrade.cofee.service.WifiAwareManager

class HomeActivity : AppCompatActivity(), WifiAwareManager.ProfileDiscoveryListener {

    private lateinit var profileService: ProfileService
    private lateinit var wifiAwareManager: WifiAwareManager
    private lateinit var profileAdapter: ProfileAdapter

    private lateinit var emptyView: TextView
    private lateinit var recyclerView: RecyclerView

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        profileService = ProfileService(this)
        wifiAwareManager = WifiAwareManager(this)

        // Initialize views
        emptyView = findViewById(R.id.tvEmptyView)
        recyclerView = findViewById(R.id.rvProfiles)

        // Set up RecyclerView
        profileAdapter = ProfileAdapter { profile ->
            // Handle profile click
            val intent = Intent(this, ProfileDetailActivity::class.java)
            intent.putExtra("PROFILE_ID", profile.userId)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = profileAdapter

        // Set up FAB
        findViewById<FloatingActionButton>(R.id.fabEditProfile).setOnClickListener {
            startActivity(Intent(this, ProfileCreationActivity::class.java))
        }

        // Check permissions before starting discovery
        checkLocationPermission()

        // Load discovered profiles
        updateProfileList()
    }

    override fun onResume() {
        super.onResume()
        updateProfileList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        wifiAwareManager.cleanup()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // Clear and restart discovery
                profileService.clearDiscoveredProfiles()
                updateProfileList()
                checkLocationPermission()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startDiscovery()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDiscovery()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required for Wi-Fi Aware discovery",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startDiscovery() {
        wifiAwareManager.initialize(this)
    }

    private fun updateProfileList() {
        val profiles = profileService.getDiscoveredProfiles().sortedByDescending { it.discoveryTimestamp }
        profileAdapter.submitList(profiles)

        // Show/hide empty view
        if (profiles.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // WifiAwareManager.ProfileDiscoveryListener implementation
    override fun onProfileDiscovered(profile: UserProfile) {
        runOnUiThread {
            Toast.makeText(this, "Discovered profile: ${profile.name}", Toast.LENGTH_SHORT).show()
            updateProfileList()
        }
    }

    override fun onError(message: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
        }
    }
}