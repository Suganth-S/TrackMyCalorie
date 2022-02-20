package com.suganth.trackmycalorie.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlin.jvm.Throws

@Database(
    entities = [Run::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RunningDatabase: RoomDatabase() {
    abstract fun getRunDao() : RunDAO
}