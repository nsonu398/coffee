package com.thirdgrade.cofee.service


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thirdgrade.cofee.model.UserProfile

/**
 * Service class to manage user profile data
 */
class ProfileService(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PROFILE_PREFS, Context.MODE_PRIVATE
    )

    private val gson = Gson()

    /**
     * Check if user has created a profile
     */
    fun hasProfile(): Boolean {
        return sharedPreferences.contains(KEY_USER_PROFILE)
    }

    /**
     * Save user profile
     */
    fun saveProfile(profile: UserProfile) {
        val profileJson = gson.toJson(profile)
        sharedPreferences.edit {
            putString(KEY_USER_PROFILE, profileJson)
        }
    }

    /**
     * Get the current user profile
     */
    fun getProfile(): UserProfile? {
        val profileJson = sharedPreferences.getString(KEY_USER_PROFILE, null)
        return if (profileJson != null) {
            gson.fromJson(profileJson, UserProfile::class.java)
        } else {
            null
        }
    }

    /**
     * Save discovered profiles
     */
    fun saveDiscoveredProfiles(profiles: List<UserProfile>) {
        val profilesJson = gson.toJson(profiles)
        sharedPreferences.edit {
            putString(KEY_DISCOVERED_PROFILES, profilesJson)
        }
    }

    /**
     * Add a newly discovered profile to the list
     */
    fun addDiscoveredProfile(profile: UserProfile) {
        val profiles = getDiscoveredProfiles().toMutableList()

        // Check if profile already exists and remove it
        profiles.removeIf { it.userId == profile.userId }

        // Add the new profile
        profiles.add(profile)

        // Save the updated list
        saveDiscoveredProfiles(profiles)
    }

    /**
     * Get all discovered profiles
     */
    fun getDiscoveredProfiles(): List<UserProfile> {
        val profilesJson = sharedPreferences.getString(KEY_DISCOVERED_PROFILES, null)
        return if (profilesJson != null) {
            val type = object : TypeToken<List<UserProfile>>() {}.type
            gson.fromJson(profilesJson, type)
        } else {
            emptyList()
        }
    }

    /**
     * Clear all discovered profiles
     */
    fun clearDiscoveredProfiles() {
        sharedPreferences.edit {
            remove(KEY_DISCOVERED_PROFILES)
        }
    }

    companion object {
        private const val PROFILE_PREFS = "profile_preferences"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_DISCOVERED_PROFILES = "discovered_profiles"
    }
}