package com.tj.carplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log
import com.tj.carplayer.MainActivity
import com.tj.carplayer.service.USBDetectionService

class USBStateReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "USBStateReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "android.hardware.usb.action.USB_STATE" -> {
                val connected = intent.getBooleanExtra("connected", false)
                val hostConnected = intent.getBooleanExtra("host_connected", false)
                val configured = intent.getBooleanExtra("configured", false)
                
                Log.d(TAG, "USB State changed - Connected: $connected, Host: $hostConnected, Configured: $configured")
                
                if (connected && hostConnected) {
                    Log.d(TAG, "USB host connected, triggering camera detection")
                    // Trigger immediate camera detection
                    USBDetectionService.startService(context)
                    
                    // Also try to launch app immediately
                    try {
                        val launchIntent = Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                                   Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                   Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra("auto_launch", true)
                            putExtra("usb_state_changed", true)
                            putExtra("timestamp", System.currentTimeMillis())
                        }
                        context.startActivity(launchIntent)
                        Log.d(TAG, "MainActivity launched due to USB state change")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch MainActivity from USB state receiver", e)
                    }
                }
            }
            
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d(TAG, "Power connected - might be USB camera")
                // Delay a bit to allow USB enumeration
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    USBDetectionService.startService(context)
                }, 2000)
            }
        }
    }
}
