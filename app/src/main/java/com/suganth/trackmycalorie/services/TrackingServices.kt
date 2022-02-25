package com.suganth.trackmycalorie.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.suganth.trackmycalorie.MainActivity
import com.suganth.trackmycalorie.R
import com.suganth.trackmycalorie.other.Constants
import com.suganth.trackmycalorie.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.suganth.trackmycalorie.other.Constants.FASTEST_LOCATION_INTERVAL
import com.suganth.trackmycalorie.other.Constants.LOCATION_UPDATE_INTERVAL
import com.suganth.trackmycalorie.other.Constants.NOTIFICATION_CHANNEL_ID
import com.suganth.trackmycalorie.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.suganth.trackmycalorie.other.Constants.NOTIFICATION_ID
import com.suganth.trackmycalorie.other.TrackingUtility
import timber.log.Timber
typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>
/**
 * The reason for inherit LifecycleService instead of IntentService and service is , we need to obseve the
 * LiveData object insvead of tis service class .
 * the way we manage communication from activity to service is by the help of intents
 */
class TrackingServices : LifecycleService() {

    private var isFirstRun = true
    /**
     * Fused location provider client => that is just something we used to request location updates so it will
     * deliver us on consistent basis with new location updates, whenever the location changes ,or we can set interval
     * for that when we want to get new location update, for that we need call back to get the actual location results
     */
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val istracking = MutableLiveData<Boolean>()
        /**
         * Whenever we get a new co-ordinate inside of our polyline list then we can observe on that change
         * we can see that there is actually a change in our tracking fragment and draw the actual line in a map view
         * MutableList<LatLng> => is a simple polyline , just a line of co-ordinates in our map
         */
        val pathPoints = MutableLiveData<PolyLines>()
    }

    private fun postInitialValues(){
        istracking.postValue(false)
        //an empty list because we don't have any co-ordinates in the beginning
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        istracking.observe(this, Observer {
            updateLocationtracking(it)
        })
    }

    /**
     * Called whenever we send a command to our service , so whenever we send a intent
     * with an action attached to its service class
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun)
                    {
                        startForegroundService()
                        isFirstRun = false
                    }else {
                        Timber.d("Started or resumed service")
                    }
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationtracking(istracking:Boolean){
        if (istracking){
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
             if(istracking.value!!){
                 result?.locations?.let {
                      locations ->
                     for (location in locations)
                     {
                         addPathPoint(location)
                         Timber.d("NEW LOCATION : ${location.latitude}, ${location.longitude}")
                     }
                 }
             }
        }
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyLine()
        istracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
        as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }

    /**
     * Pending intent is one which used to open our main activity , when we used to click on the notification
     */
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        //iT means whenver we launch a pending intent,if its already available it will launch that
        //instead of recreating
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            /**
             * notification always comes wid a sound ,would mean that each time we update our notification ,the phone would ring..
             * and ofcourse we dont want that, so make sure to check importance low here
             */
            IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }
}