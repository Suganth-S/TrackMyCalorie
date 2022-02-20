package com.suganth.trackmycalorie.repositories

import com.suganth.trackmycalorie.db.Run
import com.suganth.trackmycalorie.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {
    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getAllRunsSortedByDistanceInMeters() = runDAO.getAllRunsSortedByDistanceInMeters()

    fun getAllRunsSortedBytimeInMillis() = runDAO.getAllRunsSortedBytimeInMillis()

    fun gettotalAvgSpeed() = runDAO.getTotalavgSpeedInKMH()

    fun getTotaldistanceinMeters() = runDAO.getTotaldistanceinMeters()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotaltimeInMillis() = runDAO.getTotaltimeInMillis()
}