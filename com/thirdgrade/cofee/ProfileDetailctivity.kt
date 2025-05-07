package com.thirdgrade.cofee


import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.thirdgrade.cofee.service.ProfileService

class ProfileDetailActivity : AppCompatActivity() {

    private lateinit var profileService: ProfileService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        profileService = ProfileService(this)

        // Get profile ID from intent
        val profileId = intent.getStringExtra("PROFILE_ID")
        if (profileId == null) {
            finish()
            return
        }

        // Find profile
        val profile = profileService.getDiscoveredProfiles().find { it.userId == profileId }
        profile?.let {
            // Display profile information
            findViewById<TextView>(R.id.tvName).text = it.name
            findViewById<TextView>(R.id.tvBio).text = it.bio
            findViewById<TextView>(R.id.tvInterests).text = "Interests: ${it.interests.joinToString(", ")}"

            // Display profile image if available
            it.profilePicUri?.let { uri ->
                findViewById<ImageView>(R.id.imgProfilePic).setImageURI(Uri.parse(uri))
            }
        } ?: run {
            // Profile not found, close activity
            finish()
        }

        // Set up back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}