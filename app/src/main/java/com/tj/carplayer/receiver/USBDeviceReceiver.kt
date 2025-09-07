package com.tj.carplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.tj.carplayer.MainActivity
import com.tj.carplayer.service.USBDetectionService

class USBDeviceReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "USBDeviceReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if (device != null) {
                    Log.d(TAG, "USB device attached: ${device.deviceName} (${device.vendorId}:${device.productId})")
                    
                    if (isUSBCamera(device)) {
                        Log.d(TAG, "UVC camera detected, launching app")
                        launchMainActivity(context)
                    }
                }
            }
            
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                if (device != null) {
                    Log.d(TAG, "USB device detached: ${device.deviceName} (${device.vendorId}:${device.productId})")
                }
            }
        }
    }
    
    private fun isUSBCamera(device: UsbDevice): Boolean {
        Log.d(TAG, "USBDeviceReceiver: Checking if device is camera: ${device.deviceName} (VID:${device.vendorId.toString(16)} PID:${device.productId.toString(16)})")
        
        // Check if device is a UVC (USB Video Class) camera
        var hasVideoInterface = false
        var hasVideoControl = false
        var hasVideoStreaming = false
        
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            Log.d(TAG, "USBDeviceReceiver: Interface $i: Class=${usbInterface.interfaceClass}, Subclass=${usbInterface.interfaceSubclass}, Protocol=${usbInterface.interfaceProtocol}")
            
            if (usbInterface.interfaceClass == 14) { // Video class
                hasVideoInterface = true
                when (usbInterface.interfaceSubclass) {
                    1 -> hasVideoControl = true
                    2 -> hasVideoStreaming = true
                }
                Log.d(TAG, "USBDeviceReceiver: Found video interface: Class=${usbInterface.interfaceClass}, Subclass=${usbInterface.interfaceSubclass}")
            }
        }
        
        // UVC camera should have both video control and streaming interfaces
        if (hasVideoInterface && (hasVideoControl || hasVideoStreaming)) {
            Log.d(TAG, "USBDeviceReceiver: Confirmed UVC camera: ${device.deviceName}")
            return true
        }
        
        // Check for common camera vendor IDs as fallback
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
            Log.d(TAG, "USBDeviceReceiver: Found known camera vendor: ${device.deviceName} (VID:${device.vendorId.toString(16)})")
        }
        
        return isKnownCamera
    }
    
    private fun launchMainActivity(context: Context) {
        try {
            Log.d(TAG, "USBDeviceReceiver: Attempting to launch MainActivity...")
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TOP or
                       Intent.FLAG_ACTIVITY_SINGLE_TOP or
                       Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                putExtra("auto_launch", true)
                putExtra("usb_camera_detected", true)
                putExtra("timestamp", System.currentTimeMillis())
                putExtra("source", "usb_device_receiver")
            }
            
            context.startActivity(intent)
            Log.d(TAG, "USBDeviceReceiver: MainActivity launched successfully due to USB camera attachment")
            
            // Also try to bring app to foreground
            val bringToFrontIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("bring_to_front", true)
                putExtra("source", "usb_device_receiver")
            }
            context.startActivity(bringToFrontIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "USBDeviceReceiver: Failed to launch MainActivity", e)
            e.printStackTrace()
        }
    }
}
