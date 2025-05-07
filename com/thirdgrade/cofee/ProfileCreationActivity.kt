package com.thirdgrade.cofee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.thirdgrade.cofee.model.UserProfile
import com.thirdgrade.cofee.service.ProfileService

class ProfileCreationActivity : AppCompatActivity() {

    private lateinit var profileService: ProfileService
    private var selectedImageUri: Uri? = null

    // Register for image picker result
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            findViewById<ImageView>(R.id.imgProfilePic).setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_creation)

        profileService = ProfileService(this)

        // Load existing profile if available
        val existingProfile = profileService.getProfile()
        existingProfile?.let { profile ->
            findViewById<EditText>(R.id.etName).setText(profile.name)
            findViewById<EditText>(R.id.etBio).setText(profile.bio)
            findViewById<EditText>(R.id.etInterests).setText(profile.interests.joinToString(", "))
            profile.profilePicUri?.let { uri ->
                selectedImageUri = Uri.parse(uri)
                findViewById<ImageView>(R.id.imgProfilePic).setImageURI(selectedImageUri)
            }
        }

        // Set up image picker
        findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            pickImage.launch("image/*")
        }

        // Set up save button
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val nameEditText = findViewById<EditText>(R.id.etName)
        val bioEditText = findViewById<EditText>(R.id.etBio)
        val interestsEditText = findViewById<EditText>(R.id.etInterests)

        val name = nameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()
        val interestsText = interestsEditText.text.toString().trim()

        // Validate inputs
        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return
        }

        if (bio.isEmpty()) {
            bioEditText.error = "Bio is required"
            return
        }

        if (interestsText.isEmpty()) {
            interestsEditText.error = "Interests are required"
            return
        }

        // Parse interests
        val interests = interestsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Create profile
        val profile = UserProfile(
            name = name,
            bio = bio,
            interests = interests,
            profilePicUri = selectedImageUri?.toString(),
            isCurrentUser = true
        )

        // Save profile
        profileService.saveProfile(profile)

        // Navigate to home screen
        Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}