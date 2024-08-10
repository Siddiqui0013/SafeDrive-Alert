package com.siddiqui.safedrivealert

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.siddiqui.safedrivealert.ui.main.SettingsManager
import kotlin.math.abs

class SpeedDetection : AppCompatActivity() {

    private lateinit var confirmation: MediaPlayer
    private lateinit var warning: MediaPlayer
    private lateinit var error: MediaPlayer
    private lateinit var speedTextView: TextView

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var isWarningPlaying = false

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Handle permission request results here
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                setupLocationClient()
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permissions are required for this app", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_detection)

        speedTextView = findViewById(R.id.speedTextView)
        confirmation = MediaPlayer.create(this, R.raw.confirmation)
        warning = MediaPlayer.create(this, R.raw.warning)
        error = MediaPlayer.create(this, R.raw.error)

        if (isLocationPermissionsGranted()) {
            setupLocationClient()
        } else {
            requestPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isLocationPermissionsGranted()) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        if (isWarningPlaying) {
            warning.pause()
            warning.seekTo(0)
            isWarningPlaying = false
        }
    }

    private fun isLocationPermissionsGranted(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocationGranted && coarseLocationGranted
    }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val speedLimit = SettingsManager.getSpeedLimit(this@SpeedDetection)
                for (location in locationResult.locations) {
                    val speed = location.speed * 3.6
                    val tolerance = 0.1f
                    speedTextView.text = getString(R.string.speed_text, speed)

                    if (abs(speed - speedLimit) < tolerance) {
                        speedTextView.apply {
                            setBackgroundResource(R.drawable.circle_red_background)
                            setTextColor(ContextCompat.getColor(context, R.color.white))
                        }

                        if (!isWarningPlaying) {
                            warning.start()
                            isWarningPlaying = true
                        }
                    }

                    else {
                        speedTextView.apply {
                            setBackgroundResource(R.drawable.circle_background)
                            setTextColor(ContextCompat.getColor(applicationContext, android.R.color.black))
                        }

                        if (isWarningPlaying) {
                            warning.pause()
                            warning.seekTo(0)
                            isWarningPlaying = false
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).apply {
            setMinUpdateIntervalMillis(1000)
        }.build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
