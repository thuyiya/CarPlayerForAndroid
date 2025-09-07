package com.tj.carplayer

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tj.carplayer.service.USBDetectionService

class DebugActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DebugActivity"
    }
    
    private lateinit var logTextView: TextView
    private lateinit var scrollView: ScrollView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        
        logTextView = findViewById(R.id.log_text)
        scrollView = findViewById(R.id.scroll_view)
        
        findViewById<Button>(R.id.btn_check_usb).setOnClickListener {
            checkUSBDevices()
        }
        
        findViewById<Button>(R.id.btn_trigger_detection).setOnClickListener {
            triggerDetection()
        }
        
        findViewById<Button>(R.id.btn_check_service).setOnClickListener {
            checkServiceStatus()
        }
        
        findViewById<Button>(R.id.btn_clear_log).setOnClickListener {
            logTextView.text = ""
        }
        
        // Initial check
        checkUSBDevices()
        checkServiceStatus()
    }
    
    private fun log(message: String) {
        Log.d(TAG, message)
        runOnUiThread {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            logTextView.append("[$timestamp] $message\n")
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }
    
    private fun checkUSBDevices() {
        log("=== Checking USB Devices ===")
        
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = usbManager.deviceList
        
        log("Total USB devices: ${deviceList.size}")
        
        if (deviceList.isEmpty()) {
            log("No USB devices found")
            return
        }
        
        deviceList.values.forEach { device ->
            log("Device: ${device.deviceName}")
            log("  VID: 0x${device.vendorId.toString(16)}")
            log("  PID: 0x${device.productId.toString(16)}")
            log("  Interfaces: ${device.interfaceCount}")
            
            for (i in 0 until device.interfaceCount) {
                val usbInterface = device.getInterface(i)
                log("    Interface $i: Class=${usbInterface.interfaceClass}, Subclass=${usbInterface.interfaceSubclass}, Protocol=${usbInterface.interfaceProtocol}")
                
                if (usbInterface.interfaceClass == 14) {
                    log("    *** VIDEO INTERFACE DETECTED ***")
                }
            }
            
            // Check if it's a known camera
            val cameraVendorIds = setOf(
                0x046d, 0x0bda, 0x0ac8, 0x1e4e, 0x1871, 0x0c45, 0x1bcf, 0x05a9, 0x0c46
            )
            
            if (cameraVendorIds.contains(device.vendorId)) {
                log("  *** KNOWN CAMERA VENDOR ***")
            }
            
            log("")
        }
    }
    
    private fun triggerDetection() {
        log("=== Triggering Manual Detection ===")
        
        try {
            val serviceIntent = Intent(this, USBDetectionService::class.java)
            val service = bindService(serviceIntent, object : android.content.ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: android.os.IBinder?) {
                    val usbService = (service as USBDetectionService.LocalBinder).getService()
                    usbService.triggerCameraDetection()
                    log("Manual detection triggered")
                    unbindService(this)
                }
                
                override fun onServiceDisconnected(name: ComponentName?) {}
            }, Context.BIND_AUTO_CREATE)
            
            if (!service) {
                log("Failed to bind to USB detection service")
            }
        } catch (e: Exception) {
            log("Error triggering detection: ${e.message}")
        }
    }
    
    private fun checkServiceStatus() {
        log("=== Checking Service Status ===")
        
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        
        val usbService = runningServices.find { 
            it.service.className == USBDetectionService::class.java.name 
        }
        
        if (usbService != null) {
            log("USB Detection Service is running")
            log("  PID: ${usbService.pid}")
            log("  Started: ${usbService.started}")
        } else {
            log("USB Detection Service is NOT running")
        }
        
        // Also check if service is bound
        try {
            val serviceIntent = Intent(this, USBDetectionService::class.java)
            val service = bindService(serviceIntent, object : android.content.ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: android.os.IBinder?) {
                    val usbService = (service as USBDetectionService.LocalBinder).getService()
                    log("Service is monitoring: ${usbService.isMonitoring()}")
                    log("Current USB devices: ${usbService.getCurrentUSBDevices()}")
                    unbindService(this)
                }
                
                override fun onServiceDisconnected(name: ComponentName?) {}
            }, Context.BIND_AUTO_CREATE)
            
            if (!service) {
                log("Failed to bind to service for status check")
            }
        } catch (e: Exception) {
            log("Error checking service status: ${e.message}")
        }
    }
}
