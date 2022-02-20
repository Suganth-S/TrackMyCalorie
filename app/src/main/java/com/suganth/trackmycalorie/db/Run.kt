package com.suganth.trackmycalorie.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    /**
     * Databases in general are not made to save on complex objects and bitmap is such a complex objects
     * so somehow we need to find a way fr room to save this bitmap and to get a bitmap again from how room
     * saved it and for that we need to add a type converter to our db.. for that we neeed to add two functions
     * 1.For room to convert a bitmap into a format that room understands and on the other hand
     * 2.we need a function to convert the format that room understands to the format we want to have so
     *
     */
    var img: Bitmap? = null,
    //timestamp equal to the amount of milliseconds ,when this run on,
    //the date of this run is converted into milliseconds,reason why we dont
    //save run with date parameter is becoz we want to sort our runs by date
    //so if we just save date ,then its really difficult to sort that runs
    //so we save it in form of long, so that we can easily sort long values, comparing to date
    var timeStamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    /**
     * timeStamp - describe when our run was
     * timeInMillis - describes how long our run was
     * always have a time in milliseconds, so that it could help to convert and regulate easily
     */
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null
}