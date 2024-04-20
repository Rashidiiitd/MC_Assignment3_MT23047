package com.example.weatherapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AccelerometerDao {
    @Insert
    suspend fun insertAccelerometerData(data: AccelerometerData)

    @Query("SELECT * FROM accelerometer_data WHERE timestamp = :timestamp")
    suspend fun getAccelerometerData(timestamp: Long): AccelerometerData?

    @Query("SELECT * FROM accelerometer_data")
    suspend fun getAllAccelerometerData(): List<AccelerometerData>
    @Query("DELETE FROM accelerometer_data")
    suspend fun clearAllData()
}
