package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.example.weatherapp.ui.theme.WeatherAppTheme
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
class GraphActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val accelerometerDatabase = Room.databaseBuilder(
            applicationContext,
            AccelerometerDatabase::class.java, "accelerometer_database"
        ).build()
        val accelerometerDao = accelerometerDatabase.accelerometerDao()

        setContent {
            WeatherAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    //GraphContent()
                }
            }
        }

    }
}

@Composable
fun GraphContent() {
    var refreshDataset by remember { mutableStateOf(0) }
    val modelProducer = remember { ChartEntryModelProducer() }
    val datasetForModel = remember { mutableListOf<FloatEntry>() }
    val datasetLineSpec = remember { mutableListOf<LineChart.LineSpec>() }
    val scrollState = rememberChartScrollState()

    LaunchedEffect(key1 = refreshDataset) {
        datasetForModel.clear()
        datasetLineSpec.clear()
        var xPos = 0f
        val dataPoints = mutableListOf<FloatEntry>()
        datasetLineSpec.add(
            LineChart.LineSpec(
                lineColor = Green.toArgb(),
                lineBackgroundShader = DynamicShaders.fromBrush(
                    brush = Brush.verticalGradient(
                        listOf(
                            Green.copy(alpha = com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                            Green.copy(alpha = com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END)
                        )
                    )
                )
            )
        )
        for (i in 1..100) {
            val randomYFloat = (1..100).random().toFloat()
            dataPoints.add(FloatEntry(x = xPos, y = randomYFloat))
            xPos += 1f
        }

        datasetForModel.addAll(dataPoints)

        modelProducer.setEntries(datasetForModel)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(modifier = Modifier.fillMaxSize()) {
            if (datasetForModel.isNotEmpty()) {
                ProvideChartStyle {
                    Chart(
                        chart = lineChart(
                            lines = datasetLineSpec
                        ),
                        chartModelProducer = modelProducer,
                       // scrollState = scrollState
                        isZoomEnabled = true // Assuming isZoomEnabled is a boolean variable
                    )
                }
            }
        }
        TextButton(
            modifier = Modifier.fillMaxSize(),
            onClick = { refreshDataset++ }
        ) {
            Text(text = "Refresh", textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GraphPreview() {
    WeatherAppTheme {
        GraphContent()
    }
}
