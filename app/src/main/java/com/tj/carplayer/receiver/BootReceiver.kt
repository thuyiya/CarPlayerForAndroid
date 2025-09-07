package com.tj.carplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tj.carplayer.service.USBDetectionService

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Device boot completed or app updated, starting USB detection service")
                USBDetectionService.startService(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "User unlocked device, ensuring USB detection service is running")
                USBDetectionService.startService(context)
            }
        }
    }
}
