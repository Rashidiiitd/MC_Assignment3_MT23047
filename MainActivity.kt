package com.example.weatherapp

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import android.util.Log
import android.widget.Toast
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var accelerometerDao: AccelerometerDao
    private val accelerometerDataList = mutableStateOf<List<AccelerometerData>>(emptyList())

    private var xaxis by mutableStateOf("f")
    private var yaxis by mutableStateOf("f")
    private var zaxis by mutableStateOf("f")

    private var isReadingData = true // Flag to control data reading
    private var isDisplayingList by mutableStateOf(false) // Flag to control the display of the list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDatabase()
        setupSensor()
        setContent {
            MainScreen()
        }
        startStopDataReading()
    }

    private fun setupDatabase() {
        val accelerometerDatabase = Room.databaseBuilder(
            applicationContext,
            AccelerometerDatabase::class.java, "accelerometer_database"
        ).build()
        accelerometerDao = accelerometerDatabase.accelerometerDao()
    }

    private fun setupSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: error("Accelerometer sensor not available")
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun fetchAccelerometerData() {
        lifecycleScope.launch {
            accelerometerDataList.value = withContext(Dispatchers.IO) {
                accelerometerDao.getAllAccelerometerData()
            }
        }
    }

    @Composable
    private fun MainScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)
        ) {
            Image(
                painter = painterResource(id = R.drawable.pexels_eberhard_grossgasteiger_1624496),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Accelerometer Data", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(40.dp))
                DisplayAccelerometerData()
                Spacer(modifier = Modifier.height(16.dp))
                AccelerometerControls(
                    onShowGraphClick = { showGraph() },
                    onClearDataClick = { clearData() },
                    onReadDataAgainClick = { readDataAgain()
                    },
                    onGenerateCSVClick = { generateCSV() }
                )
                Spacer(modifier = Modifier.height(16.dp))
                fetchAccelerometerData()
                if (isDisplayingList) {
                    DisplayAccelerometerList(
                        dataList = accelerometerDataList.value,
                        onCloseClick = { isDisplayingList = false }
                    )
                }
            }
        }
    }

    @Composable
    private fun DisplayAccelerometerData() {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                  .clickable {
                        isDisplayingList = !isDisplayingList
                    },

                shape = RoundedCornerShape(8.dp)
            ) {
                Text("X Axis: $xaxis", fontSize = 20.sp)
            }
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Y Axis: $yaxis", fontSize = 20.sp)
            }
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Z Axis: $zaxis", fontSize = 20.sp)
            }
        }
    }

    private fun startStopDataReading() {
        lifecycleScope.launch {
            delay(500L)
            isReadingData = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

  override fun onSensorChanged(event: SensorEvent?) {
    event?.let {
        if (isReadingData) {
            val xRaw = Math.toDegrees(Math.atan2(event.values[1].toDouble(), event.values[2].toDouble())).toFloat()
            val yRaw = Math.toDegrees(Math.atan2((-event.values[0]).toDouble(), event.values[2].toDouble())).toFloat()
            val zRaw = Math.toDegrees(Math.atan2(event.values[1].toDouble(), event.values[0].toDouble())).toFloat()

            // Check if all values are non-zero before inserting into the database
            if (xRaw != 0f || yRaw != 0f || zRaw != 0f) {
                xaxis = xRaw.toString()
                yaxis = yRaw.toString()
                zaxis = zRaw.toString()

                insertDataToDatabase()
            }
        }
    }
}

    private fun insertDataToDatabase() {
        val accelerometerData = AccelerometerData(
            timestamp = System.currentTimeMillis(),
            xAngle = xaxis.toFloat(),
            yAngle = yaxis.toFloat(),
            zAngle = zaxis.toFloat()
        )
        lifecycleScope.launch {
            accelerometerDao.insertAccelerometerData(accelerometerData)
        }
    }

    private fun clearData() {
        xaxis = "0"
        yaxis = "0"
        zaxis = "0"
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                accelerometerDao.clearAllData()
            }
        }
    }

    private fun readDataAgain() {
        fetchAccelerometerData()
        isReadingData = true
        lifecycleScope.launch {
            delay(500L)
            isReadingData = false
        }
    }

    private fun showGraph() {
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                accelerometerDao.getAllAccelerometerData()
            }
            val intent = Intent(this@MainActivity, MainActivity2::class.java)
            startActivity(intent)
        }
    }


    private fun generateCSV() {
    val csvFileName = "accelerometer_data.csv"
    val csvFile = File(getExternalFilesDir(null), csvFileName)
    try {
        csvFile.createNewFile()
        val csvWriter = csvWriter().open(csvFile) {
            writeRow(listOf("Timestamp", "X-Angle", "Y-Angle", "Z-Angle"))
            accelerometerDataList.value.forEach { data ->
                writeRow(listOf(data.timestamp.toString(), data.xAngle.toString(), data.yAngle.toString(), data.zAngle.toString()))
            }
        }
//        csvWriter.close()]

        // Get the absolute path of the CSV file
        val filePath = csvFile.absolutePath

        // Show a toast message indicating successful generation with the file location
        Toast.makeText(this, "CSV file generated successfully! File saved at: $filePath", Toast.LENGTH_SHORT).show()

        // Log the file location
        Log.d("CSV", "CSV file generated successfully! File saved at: $filePath")
    } catch (e: Exception) {
        Log.e("CSV", "Error generating CSV", e)
        Toast.makeText(this, "Error generating CSV file!", Toast.LENGTH_SHORT).show()
    }
}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}

@Composable
private fun DisplayAccelerometerList(
    dataList: List<AccelerometerData>,
    onCloseClick: () -> Unit
) {
    LazyColumn {
        items(dataList) { data ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Timestamp: ${data.timestamp}, X: ${data.xAngle}, Y: ${data.yAngle}, Z: ${data.zAngle}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    Button(
        onClick = onCloseClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Close List", fontSize = 18.sp)
    }
}

@Composable
fun AccelerometerControls(
    onShowGraphClick: () -> Unit,
    onClearDataClick: () -> Unit,
    onReadDataAgainClick: () -> Unit,
    onGenerateCSVClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onShowGraphClick, modifier = Modifier.fillMaxWidth()) {
            Text("Show Graph", fontSize = 18.sp)
        }
        Button(onClick = onClearDataClick, modifier = Modifier.fillMaxWidth()) {
            Text("Clear DataBase", fontSize = 18.sp)
        }
        Button(onClick = onReadDataAgainClick, modifier = Modifier.fillMaxWidth()) {
            Text("Read Data Again", fontSize = 18.sp)
        }
    Button(onClick = onGenerateCSVClick, modifier = Modifier.fillMaxWidth()) {
            Text("Generate CSV", fontSize = 18.sp)


    }
}
}
