package com.suganth.trackmycalorie.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.suganth.trackmycalorie.MainActivity
import com.suganth.trackmycalorie.R
import com.suganth.trackmycalorie.other.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    /**
     * @ServiceScoped = For the lifetime of our service there is going to be only one instance of the fusedLocationProviderClient
     *                  and becoz we want that , of course we want only a single instance not multiple instances, we annotate
     *                  all those functions in this class with add service scope and the same counts for activity
     */
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app:Context
    ) = FusedLocationProviderClient(app)

    /**
     * Pending intent is one which used to open our main activity , when we used to click on the notification
     */
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent (
        @ApplicationContext app:Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        //iT means whenver we launch a pending intent,if its already available it will launch that
        //instead of recreating
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_run)
        .setContentTitle("Running App")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}










