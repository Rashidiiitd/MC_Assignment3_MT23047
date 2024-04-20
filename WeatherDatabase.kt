package com.example.weatherapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AccelerometerData::class], version = 1, exportSchema = false)
abstract class AccelerometerDatabase : RoomDatabase() {
    abstract fun accelerometerDao(): AccelerometerDao
}
