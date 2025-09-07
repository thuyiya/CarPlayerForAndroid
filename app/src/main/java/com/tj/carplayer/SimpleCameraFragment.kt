package com.tj.carplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiangdg.ausbc.MultiCameraClient
import com.jiangdg.ausbc.base.CameraFragment
import com.jiangdg.ausbc.callback.ICameraStateCallBack
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.render.env.RotateType
import com.jiangdg.ausbc.utils.ToastUtils
import com.jiangdg.ausbc.widget.IAspectRatio
import com.jiangdg.ausbc.widget.AspectRatioTextureView
import com.tj.carplayer.databinding.FragmentSimpleCameraBinding

/**
 * Simple fullscreen camera fragment - just shows camera feed
 * No UI controls, no capture features, just pure camera view
 */
class SimpleCameraFragment : CameraFragment(), ICameraStateCallBack {
    
    companion object {
        private const val TAG = "SimpleCameraFragment"
    }
    
    private lateinit var mViewBinding: FragmentSimpleCameraBinding
    
    override fun getRootView(inflater: LayoutInflater, container: ViewGroup?): View? {
        mViewBinding = FragmentSimpleCameraBinding.inflate(inflater, container, false)
        return mViewBinding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "SimpleCameraFragment created - fullscreen camera view")
        
        // Hide the UVC logo initially
        mViewBinding.uvcLogoIv.visibility = View.GONE
        
        // Set up camera with 720p resolution
        setupCamera()
    }
    
    private fun setupCamera() {
        try {
            Log.d(TAG, "Camera setup completed - 720p resolution")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup camera", e)
            ToastUtils.show("Failed to start camera: ${e.message}")
        }
    }
    
    override fun getCameraViewContainer(): ViewGroup {
        return mViewBinding.cameraViewContainer
    }
    
    override fun getCameraView(): IAspectRatio {
        return AspectRatioTextureView(requireContext())
    }
    
    override fun getGravity(): Int {
        return android.view.Gravity.CENTER
    }
    
    override fun getCameraRequest(): CameraRequest {
        return CameraRequest.Builder()
            .setPreviewWidth(1280)
            .setPreviewHeight(720)
            .setRenderMode(CameraRequest.RenderMode.OPENGL)
            .setDefaultRotateType(RotateType.ANGLE_0)
            .setAudioSource(CameraRequest.AudioSource.SOURCE_SYS_MIC)
            .setPreviewFormat(CameraRequest.PreviewFormat.FORMAT_MJPEG)
            .setAspectRatioShow(true)
            .setCaptureRawImage(false)
            .setRawPreviewData(false)
            .create()
    }
    
    override fun onCameraState(self: MultiCameraClient.ICamera, code: ICameraStateCallBack.State, msg: String?) {
        when (code) {
            ICameraStateCallBack.State.OPENED -> {
                Log.d(TAG, "Camera opened successfully")
                mViewBinding.uvcLogoIv.visibility = View.GONE
                mViewBinding.frameRateTv.visibility = View.VISIBLE
                ToastUtils.show("Camera opened")
            }
            ICameraStateCallBack.State.CLOSED -> {
                Log.d(TAG, "Camera closed")
                mViewBinding.uvcLogoIv.visibility = View.VISIBLE
                mViewBinding.frameRateTv.visibility = View.GONE
                ToastUtils.show("Camera closed")
            }
            ICameraStateCallBack.State.ERROR -> {
                Log.e(TAG, "Camera error: $msg")
                mViewBinding.uvcLogoIv.visibility = View.VISIBLE
                mViewBinding.frameRateTv.visibility = View.GONE
                ToastUtils.show("Camera error: $msg")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "SimpleCameraFragment destroyed")
    }
}
