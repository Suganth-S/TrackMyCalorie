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
import com.suganth.trackmycalorie.other.Constants.ACTION_PAUSE_SERVICE
import com.suganth.trackmycalorie.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.suganth.trackmycalorie.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.suganth.trackmycalorie.other.Constants.FASTEST_LOCATION_INTERVAL
import com.suganth.trackmycalorie.other.Constants.LOCATION_UPDATE_INTERVAL
import com.suganth.trackmycalorie.other.Constants.NOTIFICATION_CHANNEL_ID
import com.suganth.trackmycalorie.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.suganth.trackmycalorie.other.Constants.NOTIFICATION_ID
import com.suganth.trackmycalorie.other.Constants.TIMER_UPDATE_INTERVAL
import com.suganth.trackmycalorie.other.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias PolyLine = MutableList<LatLng>
typealias PolyLines = MutableList<PolyLine>

/**
 * The reason for inherit LifecycleService instead of IntentService and service is , we need to obseve the
 * LiveData object insvead of tis service class .
 * the way we manage communication from activity to service is by the help of intents
 */
@AndroidEntryPoint
class TrackingServices : LifecycleService() {

    private var isFirstRun = true

    /**
     * Fused location provider client => that is just something we used to request location updates so it will
     * deliver us on consistent basis with new location updates, whenever the location changes ,or we can set interval
     * for that when we want to get new location update, for that we need call back to get the actual location results
     */
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunInSeconds = MutableLiveData<Long>()

    /**
     * For update a notification, we just need to post a new notification with a same id and thats why we create a
     * baseNotificationBuilder which will hold the configuration of every of our notifications
     */
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    /**
     * which will have a different configuration, than our baseNotification has ..
     * Here we are going to change text and also adding two new action buttons
     */
    lateinit var currentNotification: NotificationCompat.Builder

    companion object {
        val timeInMillis = MutableLiveData<Long>()
        val istracking = MutableLiveData<Boolean>()

        /**
         * Whenever we get a new co-ordinate inside of our polyline list then we can observe on that change
         * we can see that there is actually a change in our tracking fragment and draw the actual line in a map view
         * MutableList<LatLng> => is a simple polyline , just a line of co-ordinates in our map
         */
        val pathPoints = MutableLiveData<PolyLines>()
    }

    private fun postInitialValues() {
        istracking.postValue(false)
        //an empty list because we don't have any co-ordinates in the beginning
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        currentNotification = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        istracking.observe(this, Observer {
            updateLocationtracking(it)
            updateNotificationtrackingState(it)
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
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Started or resumed service")
                        starttimer()
                    }
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var istimerEnabled = false
    private var laptime = 0L
    private var timeRun = 0L
    private var timeStrated = 0L
    private var lastSecondtimeStamp = 0L

    private fun starttimer() {
        addEmptyPolyLine()
        istracking.postValue(true)
        timeStrated = System.currentTimeMillis()
        istimerEnabled = true
        /**
         * instead of calling LiveData, we use Coroutine to observe the changes
         */
        CoroutineScope(Dispatchers.Main).launch {
            while (istracking.value!!) {
                //time differenc between now and timeStarted
                laptime = System.currentTimeMillis() - timeStrated
                //post the new Laptime
                timeInMillis.postValue(timeRun + laptime)
                if (timeInMillis.value!! >= lastSecondtimeStamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondtimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += laptime
        }
    }

    private fun pauseService() {
        istracking.postValue(false)
        istimerEnabled = false
    }

    private fun updateNotificationtrackingState(istracking: Boolean) {
        val notificationActiontext = if (istracking) "Pause" else "Resume"
        val pendingIntent = if (istracking) {
            val pauseIntent = Intent(this, TrackingServices::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingServices::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotification, ArrayList<NotificationCompat.Action>())
        }

        currentNotification = baseNotificationBuilder.addAction(
            R.drawable.ic_pause_black_24dp,
            notificationActiontext,
            pendingIntent
        )
        notificationManager.notify(NOTIFICATION_ID, currentNotification.build())
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationtracking(istracking: Boolean) {
        if (istracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
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
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (istracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION : ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
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
        starttimer()
        istracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            val notification = currentNotification
                .setContentText(TrackingUtility.getFormattedStopWatchtime(it*1000L))
            notificationManager.notify(NOTIFICATION_ID,notification.build())
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            /**
             * notification always comes wid a sound ,would mean that each time we update our notification ,the phone would ring..
             * and ofcourse we dont want that, so make sure to check importance low here
             */
            IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }
}