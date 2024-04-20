package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.room.Room
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.BrushShader
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import io.ktor.http.ContentDisposition.Companion.File
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.Random

class MainActivity2 : ComponentActivity() {
    private lateinit var accelerometerDao: AccelerometerDao // Add this line
    private var showList by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDatabase()
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content()
                }
            }
        }
    }

    private fun setupDatabase() {
        val accelerometerDatabase = Room.databaseBuilder(
            applicationContext,
            AccelerometerDatabase::class.java, "accelerometer_database"
        ).build()
        accelerometerDao = accelerometerDatabase.accelerometerDao()
    }

        @Composable
        fun Content() {
        var accelerometerDataList by remember { mutableStateOf(emptyList<AccelerometerData>()) }
            var showXAxisGraph by remember { mutableStateOf(false) }
    var showYAxisGraph by remember { mutableStateOf(false) }
    var showZAxisGraph by remember { mutableStateOf(false) }
        // Collect accelerometer data from the database using LaunchedEffect
        LaunchedEffect(Unit) {
            val data = accelerometerDao.getAllAccelerometerData()
            accelerometerDataList = data
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)) {
           Row(  modifier = Modifier.fillMaxWidth(),
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.SpaceBetween
           ) {

           Button(
                onClick = { showList = !showList },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (showList) "Hide List" else "Show List")
            }

           Button(
                onClick = { },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Get Data")
            }
           }

            if (showList) {
                LazyColumn {
                    items(accelerometerDataList) { data ->
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize(),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 30.dp
                            )
                        ) {
                            Text(
                                text = "Timestamp: ${data.timestamp}, X: ${data.xAngle}, Y: ${data.yAngle}, Z: ${data.zAngle}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { showXAxisGraph = true; showYAxisGraph = false; showZAxisGraph = false },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Show X-Axis Graph")
            }
            Button(
                onClick = { showXAxisGraph = false; showYAxisGraph = true; showZAxisGraph = false },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Show Y-Axis Graph")
            }
            Button(
                onClick = { showXAxisGraph = false; showYAxisGraph = false; showZAxisGraph = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(text = "Show Z-Axis Graph")
            }
        }
            Button(
                onClick = {  },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Refresh Graph")
            }

        if (showXAxisGraph) {
           CreateLineChart("X-Axis vs Timestamp", accelerometerDataList,AccelerometerData::xAngle, AccelerometerData::timestamp )
            // CreateLineChart("X-Axis vs Timestamp", generateRandomData(100), AccelerometerData::timestamp, AccelerometerData::xAngle)
        }
        if (showYAxisGraph) {
            CreateLineChart("Y-Axis vs Timestamp", accelerometerDataList, AccelerometerData::yAngle, AccelerometerData::timestamp)
        }
        if (showZAxisGraph) {
            CreateLineChart("Z-Axis vs Timestamp", accelerometerDataList ,AccelerometerData::zAngle, AccelerometerData::timestamp)
        }

               ///Create three line charts for x-axis, y-axis, and z-axis
//            CreateLineChart("X-Axis vs Timestamp", generateRandomData(100), AccelerometerData::timestamp, AccelerometerData::xAngle)
//            CreateLineChart("Y-Axis vs Timestamp", generateRandomData(100), AccelerometerData::timestamp, AccelerometerData::yAngle)
//            CreateLineChart("Z-Axis vs Timestamp", generateRandomData(100), AccelerometerData::timestamp, AccelerometerData::zAngle)
        }
    }






     @Composable
    private fun CreateLineChart(
        title: String,
        data: List<AccelerometerData>,
        xSelector: (AccelerometerData) -> Float,
        ySelector: (AccelerometerData) -> Long
    ) {
         val modelProducer = remember { ChartEntryModelProducer() }
         val datasetForModel = remember { mutableListOf<FloatEntry>() }
         val datasetLineSpec = remember { mutableListOf<LineChart.LineSpec>() }

         LaunchedEffect(Unit) {
             datasetForModel.clear()
             datasetLineSpec.clear()
             var xPos = 1f
             val dataPoints = mutableListOf<FloatEntry>()
             data.forEach { entry ->
                 dataPoints.add(FloatEntry(x = xPos, y = xSelector(entry)))
                 xPos += 1f // increment x position
             }

             datasetForModel.addAll(dataPoints)

             datasetLineSpec.add(
                 LineChart.LineSpec(
                     lineColor = Color.Red.toArgb(), // Red color for the line
                     lineBackgroundShader = BrushShader(
                         brush = Brush.verticalGradient(
                             colors = listOf(
                                 Green.copy(alpha = com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                                 Green.copy(alpha = com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END)
                             )
                         )
                     )

                 )
             )

             modelProducer.setEntries(datasetForModel)
         }

         Column(modifier = Modifier.fillMaxSize()) {
             Text(
                 text = title,
                 textAlign = TextAlign.Center,
                 color = Color.White,
                 modifier = Modifier.padding(vertical = 16.dp)
             )
             ProvideChartStyle {
                 Chart(
                     chart = lineChart(
                         lines = datasetLineSpec
                     ),
                     chartModelProducer = modelProducer,
                     startAxis = rememberStartAxis(
                         title = "X-Axis",
                         tickLength = 0.dp,
                         valueFormatter = { value, _ -> value.toInt().toString() }
                     ),
                     bottomAxis = rememberBottomAxis(
                         title = "Y-Axis",
                         tickLength = 0.dp,
                         valueFormatter = { value, _ -> value.toString() }
                     )
                 )
             }
         }
     }
}
