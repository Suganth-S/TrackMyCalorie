package com.suganth.trackmycalorie.other

import android.Manifest
import android.content.Context
import android.os.Build
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

/**
 * In manifest, ACCESS_BACKGROUND_LOCATION is only needed for android Q, below Q we dont need to request that
 * permission, there it can just track the location in background by default , so we need to differentiate android Q
 * and not android Q, and to handle all that Location permission stuff, I inclded a library called easy permission,
 * which make very easy to handle all that location permission stuff, fo that we create utility class with a utility function
 */
object TrackingUtility {
    /**
     * check with whether the user allowed the permissions or not
     */
        fun hasLocationPermissions(context: Context) =
           if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
           { EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
           }else {
               EasyPermissions.hasPermissions(
                   context,
                   Manifest.permission.ACCESS_FINE_LOCATION,
                   Manifest.permission.ACCESS_COARSE_LOCATION,
                   Manifest.permission.ACCESS_BACKGROUND_LOCATION
               )
           }

    fun getFormattedStopWatchtime(ms:Long, includeMillis: Boolean = false): String{
        var milliseconds = ms
        var hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds = TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds = TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if(!includeMillis)
        {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:"+
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10

        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:"+
                "${if(seconds < 10) "0" else ""}$seconds"+
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }
}