/*
 * Copyright 2017-2022 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tj.carplayer

import android.Manifest.permission.*
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
// import com.gyf.immersionbar.ImmersionBar // Commented out due to dependency issues
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.utils.Utils
import com.tj.carplayer.databinding.ActivityMainBinding
import com.tj.carplayer.service.USBDetectionService

/**
 * Demos of camera usage
 *
 * @author Created by jiangdg on 2021/12/27
 */
class MainActivity : AppCompatActivity() {
    private var mWakeLock: PowerManager.WakeLock? = null
    // private var immersionBar: ImmersionBar? = null // Commented out due to dependency issues
    private lateinit var viewBinding: ActivityMainBinding
    
    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CAMERA = 0
        private const val REQUEST_STORAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBar()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        
        // Handle auto-launch from USB detection
        handleAutoLaunch(intent)
        
        // Start USB detection service
        startUSBDectionService()
        
        // Setup debug button
        viewBinding.debugButton.setOnClickListener {
            val intent = Intent(this, DebugActivity::class.java)
            startActivity(intent)
        }
        
//        replaceDemoFragment(DemoMultiCameraFragment())
        replaceDemoFragment(DemoFragment())
//        replaceDemoFragment(GlSurfaceFragment())
    }

    override fun onStart() {
        super.onStart()
        mWakeLock = Utils.wakeLock(this)
    }

    override fun onStop() {
        super.onStop()
        mWakeLock?.apply {
            Utils.wakeUnLock(this)
        }
    }

    private fun replaceDemoFragment(fragment: Fragment) {
        val hasCameraPermission = PermissionChecker.checkSelfPermission(this, CAMERA)
        val hasStoragePermission =
            PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
        if (hasCameraPermission != PermissionChecker.PERMISSION_GRANTED || hasStoragePermission != PermissionChecker.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)) {
                ToastUtils.show(R.string.permission_tip)
            }
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO),
                REQUEST_CAMERA
            )
            return
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commitAllowingStateLoss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA -> {
                val hasCameraPermission = PermissionChecker.checkSelfPermission(this, CAMERA)
                if (hasCameraPermission == PermissionChecker.PERMISSION_DENIED) {
                    ToastUtils.show(R.string.permission_tip)
                    return
                }
//                replaceDemoFragment(DemoMultiCameraFragment())
                replaceDemoFragment(DemoFragment())
//                replaceDemoFragment(GlSurfaceFragment())
            }
            REQUEST_STORAGE -> {
                val hasCameraPermission =
                    PermissionChecker.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                if (hasCameraPermission == PermissionChecker.PERMISSION_DENIED) {
                    ToastUtils.show(R.string.permission_tip)
                    return
                }
                // todo
            }
            else -> {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // immersionBar= null // Commented out due to dependency issues
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleAutoLaunch(it) }
    }
    
    private fun handleAutoLaunch(intent: Intent?) {
        val isAutoLaunch = intent?.getBooleanExtra("auto_launch", false) ?: false
        val isUSBCameraDetected = intent?.getBooleanExtra("usb_camera_detected", false) ?: false
        
        if (isAutoLaunch && isUSBCameraDetected) {
            Log.d(TAG, "App auto-launched due to USB camera detection")
            ToastUtils.show("USB camera detected - CarPlayer launched automatically")
            
            // Bring app to foreground
            val bringToFrontIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(bringToFrontIntent)
        }
    }
    
    private fun startUSBDectionService() {
        try {
            USBDetectionService.startService(this)
            Log.d(TAG, "USB detection service started")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start USB detection service", e)
        }
    }

    private fun setStatusBar() {
        // Commented out due to ImmersionBar dependency issues
        // immersionBar = ImmersionBar.with(this)
        //     .statusBarDarkFont(false)
        //     .statusBarColor(R.color.black)
        //     .navigationBarColor(R.color.black)
        //     .fitsSystemWindows(true)
        //     .keyboardEnable(true)
        // immersionBar?.init()
    }

}