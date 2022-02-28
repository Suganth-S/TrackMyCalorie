package com.suganth.trackmycalorie.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.suganth.trackmycalorie.R
import com.suganth.trackmycalorie.other.Constants.ACTION_PAUSE_SERVICE
import com.suganth.trackmycalorie.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.suganth.trackmycalorie.other.Constants.MAP_ZOOM
import com.suganth.trackmycalorie.other.Constants.POLYLINE_COLOR
import com.suganth.trackmycalorie.other.Constants.POLYLINE_WIDH
import com.suganth.trackmycalorie.other.TrackingUtility
import com.suganth.trackmycalorie.services.PolyLine
import com.suganth.trackmycalorie.services.TrackingServices
import com.suganth.trackmycalorie.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()
    private var istracking = false
    private var pathPoints = mutableListOf<PolyLine>()
    private var map : GoogleMap? = null
    private var currenttimeMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnToggleRun.setOnClickListener{
            toggleRun()
        }

        mapView.getMapAsync{
            map = it
            addAllPolyLines()
        }

        subscribetoObeservers()
    }

    private fun subscribetoObeservers(){
        TrackingServices.istracking.observe(viewLifecycleOwner, Observer {
            updatetracking(it)
        })

        TrackingServices.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameratoUser()
        })

        TrackingServices.timeInMillis.observe(viewLifecycleOwner, Observer {
            currenttimeMillis = it
            val formattedtime = TrackingUtility.getFormattedStopWatchtime(currenttimeMillis, true)
            tvTimer.text = formattedtime
        })
    }

    private fun toggleRun(){
        if(istracking)
        {
            sendCommandtoService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandtoService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updatetracking(istracking: Boolean){
        this.istracking = istracking
        if(!istracking)
        {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        }else{
            btnToggleRun.text = "Stop"
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameratoUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty())
        {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolyLines() {
        for(polyline in pathPoints)
        {
            val polyLineOption = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDH)
                .addAll(polyline)
            map?.addPolyline(polyLineOption)
        }
    }

    //A functn which used to connect two last points of our path points list
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1)
        {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions().
                    color(POLYLINE_COLOR)
                .width(POLYLINE_WIDH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    /**
     * Function that delivers this intents to our service and
     * react to that command
     */
    private fun sendCommandtoService(action: String) = Intent(requireContext(), TrackingServices::class.java).also{
        it.action = action
        requireContext().startService(it)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}