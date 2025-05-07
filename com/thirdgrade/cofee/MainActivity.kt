package com.thirdgrade.cofee

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.thirdgrade.cofee.service.ProfileService

class MainActivity : AppCompatActivity() {

    private lateinit var profileService: ProfileService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        profileService = ProfileService(this)

        // Check if profile exists
        if (profileService.hasProfile()) {
            // If profile exists, go to home screen
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            // If no profile, set up button to create profile
            findViewById<Button>(R.id.btnCreateProfile).setOnClickListener {
                startActivity(Intent(this, ProfileCreationActivity::class.java))
            }
        }
    }
}