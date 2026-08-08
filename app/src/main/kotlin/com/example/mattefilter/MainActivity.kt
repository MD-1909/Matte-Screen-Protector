package com.example.mattefilter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mattefilter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("matte_prefs", MODE_PRIVATE)

        val savedIntensity = prefs.getInt("intensity", 40)
        binding.intensitySeekBar.progress = savedIntensity
        binding.intensityLabel.text = getString(R.string.intensity_label, savedIntensity)

        binding.intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.intensityLabel.text = getString(R.string.intensity_label, progress)
                prefs.edit().putInt("intensity", progress).apply()
                if (OverlayService.isRunning) {
                    val updateIntent = Intent(this@MainActivity, OverlayService::class.java).apply {
                        action = OverlayService.ACTION_UPDATE_INTENSITY
                        putExtra(OverlayService.EXTRA_INTENSITY, progress)
                    }
                    startService(updateIntent)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.grantPermissionButton.setOnClickListener {
            requestOverlayPermission()
        }

        binding.toggleButton.setOnClickListener {
            if (!hasOverlayPermission()) {
                Toast.makeText(this, "Grant overlay permission first", Toast.LENGTH_SHORT).show()
                requestOverlayPermission()
                return@setOnClickListener
            }
            if (OverlayService.isRunning) {
                stopService(Intent(this, OverlayService::class.java))
            } else {
                val intent = Intent(this, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_START
                    putExtra(OverlayService.EXTRA_INTENSITY, binding.intensitySeekBar.progress)
                }
                ContextCompat.startForegroundService(this, intent)
            }
            updateUiState()
        }

        updateUiState()
    }

    override fun onResume() {
        super.onResume()
        updateUiState()
    }

    private fun updateUiState() {
        val granted = hasOverlayPermission()
        binding.permissionStatus.text = if (granted) {
            getString(R.string.permission_granted)
        } else {
            getString(R.string.permission_not_granted)
        }
        binding.grantPermissionButton.isEnabled = !granted
        binding.toggleButton.text = if (OverlayService.isRunning) {
            getString(R.string.turn_off)
        } else {
            getString(R.string.turn_on)
        }
    }

    private fun hasOverlayPermission(): Boolean = Settings.canDrawOverlays(this)

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }
}
