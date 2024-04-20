package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accelerometer_data")
data class AccelerometerData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long, // Timestamp to track when the data was recorded
    val xAngle: Float,
    val yAngle: Float,
    val zAngle: Float
)


