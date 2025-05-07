package com.thirdgrade.cofee.service


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySession
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.thirdgrade.cofee.model.UserProfile

import java.nio.charset.StandardCharsets

/**
 * Manager class for Wi-Fi Aware operations
 */
@RequiresApi(Build.VERSION_CODES.O)
class WifiAwareManager(private val context: Context) {

    private val TAG = "WifiAwareManager"

    private val wifiAwareManager: WifiAwareManager by lazy {
        context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
    }

    private val handler = Handler(Looper.getMainLooper())
    private val gson = Gson()

    private var wifiAwareSession: WifiAwareSession? = null
    private var publishSession: PublishDiscoverySession? = null
    private var subscribeSession: SubscribeDiscoverySession? = null

    private var profileService: ProfileService = ProfileService(context)

    private var profileDiscoveryListener: ProfileDiscoveryListener? = null

    /**
     * Initialize Wi-Fi Aware
     */
    fun initialize(listener: ProfileDiscoveryListener) {
        this.profileDiscoveryListener = listener

        if (!wifiAwareManager.isAvailable) {
            Log.e(TAG, "Wi-Fi Aware is not available on this device")
            listener.onError("Wi-Fi Aware is not available on this device")
            return
        }

        // Attach to Wi-Fi Aware service
        wifiAwareManager.attach(object : AttachCallback() {
            override fun onAttached(session: WifiAwareSession) {
                Log.d(TAG, "Successfully attached to Wi-Fi Aware")
                wifiAwareSession = session
                startPublishing()
                startSubscribing()
            }

            override fun onAttachFailed() {
                Log.e(TAG, "Failed to attach to Wi-Fi Aware")
                listener.onError("Failed to connect to Wi-Fi Aware service")
            }
        }, handler)
    }

    /**
     * Start publishing user profile
     */
    @SuppressLint("MissingPermission")
    private fun startPublishing() {
        val userProfile = profileService.getProfile() ?: return

        // Create publish config
        val config = PublishConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()

        // Start publishing
        wifiAwareSession?.publish(config, object : DiscoverySessionCallback() {
            override fun onPublishStarted(session: PublishDiscoverySession) {
                Log.d(TAG, "Publish session started")
                publishSession = session
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle message requesting profile
                if (String(message) == MESSAGE_REQUEST_PROFILE) {
                    sendProfile(peerHandle)
                }
            }
        }, handler)
    }

    /**
     * Start subscribing for other profiles
     */
    @SuppressLint("MissingPermission")
    private fun startSubscribing() {
        // Create subscribe config
        val config = SubscribeConfig.Builder()
            .setServiceName(SERVICE_NAME)
            .build()

        // Start subscribing
        wifiAwareSession?.subscribe(config, object : DiscoverySessionCallback() {
            override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                Log.d(TAG, "Subscribe session started")
                subscribeSession = session
            }

            override fun onServiceDiscovered(peerHandle: PeerHandle,
                                             serviceSpecificInfo: ByteArray?,
                                             matchFilter: List<ByteArray>?) {
                Log.d(TAG, "Service discovered")
                requestProfile(peerHandle)
            }

            override fun onMessageReceived(peerHandle: PeerHandle, message: ByteArray) {
                // Handle received profile
                try {
                    val profileJson = String(message, StandardCharsets.UTF_8)
                    val profile = gson.fromJson(profileJson, UserProfile::class.java)

                    // Notify listener of discovered profile
                    profileDiscoveryListener?.onProfileDiscovered(profile)

                    // Save discovered profile
                    profileService.addDiscoveredProfile(profile)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing received profile", e)
                }
            }
        }, handler)
    }

    /**
     * Request profile from discovered peer
     */
    private fun requestProfile(peerHandle: PeerHandle) {
        subscribeSession?.sendMessage(
            peerHandle,
            MESSAGE_ID_REQUEST_PROFILE,
            MESSAGE_REQUEST_PROFILE.toByteArray(StandardCharsets.UTF_8)
        )
    }

    /**
     * Send profile to requesting peer
     */
    private fun sendProfile(peerHandle: PeerHandle) {
        val userProfile = profileService.getProfile() ?: return
        val profileJson = gson.toJson(userProfile)

        publishSession?.sendMessage(
            peerHandle,
            MESSAGE_ID_SEND_PROFILE,
            profileJson.toByteArray(StandardCharsets.UTF_8)
        )
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        publishSession?.close()
        subscribeSession?.close()
        wifiAwareSession?.close()
    }

    /**
     * Interface for profile discovery events
     */
    interface ProfileDiscoveryListener {
        fun onProfileDiscovered(profile: UserProfile)
        fun onError(message: String)
    }

    companion object {
        private const val SERVICE_NAME = "profile_share"
        private const val MESSAGE_ID_REQUEST_PROFILE = 1
        private const val MESSAGE_ID_SEND_PROFILE = 2
        private const val MESSAGE_REQUEST_PROFILE = "REQUEST_PROFILE"
    }
}