package com.tj.carplayer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.tj.carplayer.service.USBDetectionService

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            
            // Auto-launch preference
            val autoLaunchPreference = findPreference<SwitchPreferenceCompat>("auto_launch")
            autoLaunchPreference?.setOnPreferenceChangeListener { _, newValue ->
                val isEnabled = newValue as Boolean
                if (isEnabled) {
                    USBDetectionService.startService(requireContext())
                    Toast.makeText(requireContext(), "Auto-launch enabled", Toast.LENGTH_SHORT).show()
                } else {
                    USBDetectionService.stopService(requireContext())
                    Toast.makeText(requireContext(), "Auto-launch disabled", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // Battery optimization preference
            val batteryOptimizationPreference = findPreference<androidx.preference.Preference>("battery_optimization")
            batteryOptimizationPreference?.setOnPreferenceClickListener {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:${requireContext().packageName}")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Could not open battery optimization settings", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // Notification settings preference
            val notificationPreference = findPreference<androidx.preference.Preference>("notification_settings")
            notificationPreference?.setOnPreferenceClickListener {
                try {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Could not open notification settings", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }
}
