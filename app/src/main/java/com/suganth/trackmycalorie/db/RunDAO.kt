package com.suganth.trackmycalorie.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timeStamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedBytimeInMillis(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistanceInMeters(): LiveData<List<Run>>

    /**
     * SUM() - is a function of SQLite that goes through our running table
     * and adds up all of those times in milliseconds entries and returns the result as
     * long and we simply observe on that
     */
    @Query("SELECT SUM(timeInMillis) From running_table")
    fun getTotaltimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(caloriesBurned) From running_table")
    fun getTotalCaloriesBurned(): LiveData<Long>

    @Query("SELECT SUM(distanceInMeters) From running_table")
    fun getTotaldistanceinMeters(): LiveData<Long>

    @Query("SELECT SUM(avgSpeedInKMH) From running_table")
    fun getTotalavgSpeedInKMH(): LiveData<Long>
}