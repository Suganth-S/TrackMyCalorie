package com.suganth.trackmycalorie.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.suganth.trackmycalorie.R
import com.suganth.trackmycalorie.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.suganth.trackmycalorie.other.TrackingUtility
import com.suganth.trackmycalorie.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment:Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun requestPermissions() {
        /**
         * requireContext = > we are inside of a fragment , we just dont have access to the real context of activity,
         * so the activity,we refer to here is nullable, so that we instead use requireContext(), that just make sure
         * that the context is not equal to null here
         */
        if(TrackingUtility.hasLocationPermissions(requireContext())){
            return
        }
        /** if user deny the permission, here we again ask him to agree
         *
         */
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }else{
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        }
    }

    /**
     * Easypermission will also helps to handle those results easily , becoz in android queue we have
     * that option if we deny the permission and the permissions are requested again and we deny again
     * then we have that option to deny them permanently basically so that the app cannot request them
     * again , and in that case with easy permission library , we can show the dialog to user that tells
     * that he permanently denies the permissions and he can only enable them in his app settings and that
     * dialogue will let him move to dialog settings,for that we implement a callback for those resluts
     */
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}