package com.tj.carplayer.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tj.carplayer.MainActivity
import com.tj.carplayer.R
import kotlinx.coroutines.*

class USBDetectionService : Service() {
    
    companion object {
        private const val TAG = "USBDetectionService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "usb_detection_channel"
        private const val CHECK_INTERVAL = 1000L // Check every 1 second for faster detection
        
        fun startService(context: Context) {
            val intent = Intent(context, USBDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, USBDetectionService::class.java)
            context.stopService(intent)
        }
    }
    
    private val binder = LocalBinder()
    private var isServiceRunning = false
    private var detectionJob: Job? = null
    private var lastConnectedDevices = mutableSetOf<String>()
    
    inner class LocalBinder : Binder() {
        fun getService(): USBDetectionService = this@USBDetectionService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "USBDetectionService created")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "USBDetectionService started")
        startForeground(NOTIFICATION_ID, createNotification())
        startUSBDection()
        return START_STICKY // Restart service if killed
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "USBDetectionService destroyed")
        stopUSBDection()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "USB Camera Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors USB camera connections"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CarPlayer Active")
            .setContentText("Monitoring USB camera connections")
            .setSmallIcon(R.mipmap.ic_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun startUSBDection() {
        if (isServiceRunning) return
        
        isServiceRunning = true
        detectionJob = CoroutineScope(Dispatchers.IO).launch {
            while (isServiceRunning) {
                try {
                    checkForUSBCameras()
                    delay(CHECK_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in USB detection loop", e)
                    delay(CHECK_INTERVAL)
                }
            }
        }
    }
    
    private fun stopUSBDection() {
        isServiceRunning = false
        detectionJob?.cancel()
        detectionJob = null
    }
    
    private fun checkForUSBCameras() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        
        Log.d(TAG, "Checking USB devices. Total devices: ${deviceList.size}")
        
        // Log all connected USB devices for debugging
        deviceList.values.forEach { device ->
            Log.d(TAG, "USB Device: ${device.deviceName} - VID:${device.vendorId.toString(16)} PID:${device.productId.toString(16)} - Interfaces: ${device.interfaceCount}")
            for (i in 0 until device.interfaceCount) {
                val usbInterface = device.getInterface(i)
                Log.d(TAG, "  Interface $i: Class=${usbInterface.interfaceClass}, Subclass=${usbInterface.interfaceSubclass}, Protocol=${usbInterface.interfaceProtocol}")
            }
        }
        
        val currentDevices = deviceList.values
            .filter { isUSBCamera(it) }
            .map { "${it.vendorId}:${it.productId}" }
            .toSet()
        
        Log.d(TAG, "Detected cameras: $currentDevices")
        Log.d(TAG, "Previously connected: $lastConnectedDevices")
        
        // Check for newly connected cameras
        val newlyConnected = currentDevices - lastConnectedDevices
        if (newlyConnected.isNotEmpty()) {
            Log.d(TAG, "New USB camera detected: $newlyConnected")
            launchMainActivity()
        }
        
        // Check for disconnected cameras
        val disconnected = lastConnectedDevices - currentDevices
        if (disconnected.isNotEmpty()) {
            Log.d(TAG, "USB camera disconnected: $disconnected")
        }
        
        lastConnectedDevices = currentDevices.toMutableSet()
    }
    
    private fun isUSBCamera(device: UsbDevice): Boolean {
        Log.d(TAG, "Checking if device is camera: ${device.deviceName} (VID:${device.vendorId.toString(16)} PID:${device.productId.toString(16)})")
        
        // Check if device is a UVC (USB Video Class) camera
        // UVC cameras typically have these characteristics:
        // - Interface class 14 (Video)
        // - Interface subclass 1 (Video Control) or 2 (Video Streaming)
        
        var hasVideoInterface = false
        var hasVideoControl = false
        var hasVideoStreaming = false
        
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            Log.d(TAG, "  Interface $i: Class=${usbInterface.interfaceClass}, Subclass=${usbInterface.interfaceSubclass}, Protocol=${usbInterface.interfaceProtocol}")
            
            if (usbInterface.interfaceClass == 14) { // Video class
                hasVideoInterface = true
                when (usbInterface.interfaceSubclass) {
                    1 -> hasVideoControl = true
                    2 -> hasVideoStreaming = true
                }
                Log.d(TAG, "Found video interface: Class=${usbInterface.interfaceClass}, Subclass=${usbInterface.interfaceSubclass}")
            }
        }
        
        // UVC camera should have both video control and streaming interfaces
        if (hasVideoInterface && (hasVideoControl || hasVideoStreaming)) {
            Log.d(TAG, "Confirmed UVC camera: ${device.deviceName} (${device.vendorId}:${device.productId})")
            return true
        }
        
        // Also check for common camera vendor IDs as fallback
        val cameraVendorIds = setOf(
            0x046d, // Logitech
            0x0bda, // Realtek
            0x0ac8, // Z-Star Microelectronics
            0x1e4e, // Cubeternet
            0x1871, // Aveo Technology
            0x0c45, // Sonix Technology
            0x1bcf, // Sunplus Innovation Technology
            0x05a9, // OmniVision Technologies
            0x0c46, // Microdia
            0x0ac8, // Z-Star Microelectronics
            0x1bcf, // Sunplus Innovation Technology
            0x05a9, // OmniVision Technologies
            0x0c45, // Sonix Technology
            0x1e4e, // Cubeternet
            0x1871, // Aveo Technology
            0x0bda, // Realtek Semiconductor
            0x0ac8, // Z-Star Microelectronics
            0x1bcf, // Sunplus Innovation Technology
            0x05a9, // OmniVision Technologies
            0x0c45, // Sonix Technology
            0x1e4e, // Cubeternet
            0x1871, // Aveo Technology
            0x0bda, // Realtek Semiconductor
            0x0ac8, // Z-Star Microelectronics
            0x1bcf, // Sunplus Innovation Technology
            0x05a9, // OmniVision Technologies
            0x0c45, // Sonix Technology
            0x1e4e, // Cubeternet
            0x1871  // Aveo Technology
        )
        
        val isKnownCamera = cameraVendorIds.contains(device.vendorId)
        if (isKnownCamera) {
            Log.d(TAG, "Found known camera vendor: ${device.deviceName} (VID:${device.vendorId.toString(16)})")
        }
        
        return isKnownCamera
    }
    
    private fun launchMainActivity() {
        try {
            Log.d(TAG, "Attempting to launch MainActivity...")
            
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP or
                       Intent.FLAG_ACTIVITY_SINGLE_TOP or
                       Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                putExtra("auto_launch", true)
                putExtra("usb_camera_detected", true)
                putExtra("timestamp", System.currentTimeMillis())
            }
            
            startActivity(intent)
            Log.d(TAG, "MainActivity launched successfully due to USB camera detection")
            
            // Also try to bring app to foreground
            val bringToFrontIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("bring_to_front", true)
            }
            startActivity(bringToFrontIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch MainActivity", e)
            e.printStackTrace()
        }
    }
    
    fun isMonitoring(): Boolean = isServiceRunning
    
    // Method to manually trigger camera detection (useful for testing)
    fun triggerCameraDetection() {
        Log.d(TAG, "Manual camera detection triggered")
        checkForUSBCameras()
    }
    
    // Method to get current USB devices for debugging
    fun getCurrentUSBDevices(): List<String> {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        return deviceList.values.map { "${it.deviceName} (VID:${it.vendorId.toString(16)} PID:${it.productId.toString(16)})" }
    }
}
