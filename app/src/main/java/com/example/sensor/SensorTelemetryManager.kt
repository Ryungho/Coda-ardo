package com.example.sensor

import androidx.compose.ui.graphics.Color
import com.example.model.ChannelColors
import com.example.model.ChannelStats
import com.example.model.SensorChannel
import com.example.model.TelemetrySample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class SimulationPattern(val title: String) {
    DHT22_ENVIRONMENTAL("DHT22 Temp & Humidity"),
    THREE_AXIS_ACCELEROMETER("MPU6050 3-Axis (X, Y, Z)"),
    ANALOG_SINE_ADC("Analog 12-bit ADC Sine Waves"),
    ECG_HEARTBEAT("Pulse & Heartbeat Waveform")
}

class SensorTelemetryManager {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _samples = MutableStateFlow<List<TelemetrySample>>(emptyList())
    val samples: StateFlow<List<TelemetrySample>> = _samples.asStateFlow()

    private val _channels = MutableStateFlow<List<SensorChannel>>(emptyList())
    val channels: StateFlow<List<SensorChannel>> = _channels.asStateFlow()

    private val _stats = MutableStateFlow<Map<String, ChannelStats>>(emptyMap())
    val stats: StateFlow<Map<String, ChannelStats>> = _stats.asStateFlow()

    private val _isSimulating = MutableStateFlow(true)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _simulationPattern = MutableStateFlow(SimulationPattern.DHT22_ENVIRONMENTAL)
    val simulationPattern: StateFlow<SimulationPattern> = _simulationPattern.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var simulationJob: Job? = null
    private var maxBufferSize = 200

    init {
        startSimulation()
    }

    fun setMaxBufferSize(size: Int) {
        maxBufferSize = size.coerceIn(30, 500)
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun clearData() {
        _samples.value = emptyList()
        _stats.value = emptyMap()
    }

    fun setSimulationPattern(pattern: SimulationPattern) {
        _simulationPattern.value = pattern
        clearData()
        _channels.value = emptyList()
    }

    fun toggleSimulation() {
        if (_isSimulating.value) {
            simulationJob?.cancel()
            _isSimulating.value = false
        } else {
            _isSimulating.value = true
            startSimulation()
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            var step = 0f
            while (isActive) {
                delay(60) // ~16.6 Hz sampling
                if (_isPaused.value) continue

                step += 0.12f
                if (step > 62.8f) step = 0f

                val generatedValues = when (_simulationPattern.value) {
                    SimulationPattern.DHT22_ENVIRONMENTAL -> {
                        val temp = 22.0f + (sin(step * 0.4f) * 4.5f) + (Random.nextFloat() * 0.4f)
                        val humidity = 58.0f + (cos(step * 0.3f) * 12.0f) + (Random.nextFloat() * 0.6f)
                        val pressure = 1013.2f + (sin(step * 0.1f) * 2.0f)
                        mapOf(
                            "temperature" to temp,
                            "humidity" to humidity,
                            "pressure" to pressure
                        )
                    }
                    SimulationPattern.THREE_AXIS_ACCELEROMETER -> {
                        val accelX = sin(step) * 1.8f + (Random.nextFloat() * 0.1f)
                        val accelY = cos(step * 1.2f) * 1.5f + (Random.nextFloat() * 0.1f)
                        val accelZ = 9.8f + sin(step * 0.5f) * 0.6f
                        mapOf(
                            "accel_x" to accelX,
                            "accel_y" to accelY,
                            "accel_z" to accelZ
                        )
                    }
                    SimulationPattern.ANALOG_SINE_ADC -> {
                        val wave1 = 2048f + (sin(step) * 1800f)
                        val wave2 = 2048f + (cos(step * 1.7f) * 1400f)
                        val noise = 512f + (Random.nextFloat() * 800f)
                        mapOf(
                            "adc_ch0" to wave1,
                            "adc_ch1" to wave2,
                            "adc_noise" to noise
                        )
                    }
                    SimulationPattern.ECG_HEARTBEAT -> {
                        // Synthetic QRS pulse
                        val t = (step % 3.14f)
                        val pulse = when {
                            t in 0.8f..1.0f -> sin((t - 0.8f) * 15f) * 8.0f
                            t in 1.0f..1.15f -> -sin((t - 1.0f) * 20f) * 12.0f
                            t in 1.15f..1.4f -> sin((t - 1.15f) * 12f) * 35.0f // R peak
                            t in 1.4f..1.55f -> -sin((t - 1.4f) * 20f) * 9.0f // S wave
                            t in 1.8f..2.2f -> sin((t - 1.8f) * 7.8f) * 4.0f // T wave
                            else -> Random.nextFloat() * 0.5f
                        }
                        mapOf(
                            "ecg_lead_ii" to (pulse + 10f),
                            "bpm" to 72f + sin(step * 0.1f) * 3f
                        )
                    }
                }

                feedValues(generatedValues)
            }
        }
    }

    fun parseLine(line: String) {
        if (_isPaused.value) return
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) return

        val values = mutableMapOf<String, Float>()

        try {
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                // Simple JSON parser
                val inner = trimmed.substring(1, trimmed.length - 1)
                val pairs = inner.split(",")
                for (pair in pairs) {
                    val kv = pair.split(":")
                    if (kv.size == 2) {
                        val key = kv[0].replace("\"", "").trim()
                        val v = kv[1].replace("\"", "").trim().toFloatOrNull()
                        if (v != null) values[key] = v
                    }
                }
            } else if (trimmed.contains(":")) {
                // Format: "temp:24.5,humidity:60.2" or "temp: 24.5"
                val segments = trimmed.split(",")
                for (seg in segments) {
                    val kv = seg.split(":")
                    if (kv.size == 2) {
                        val key = kv[0].trim()
                        val v = kv[1].trim().toFloatOrNull()
                        if (v != null) values[key] = v
                    }
                }
            } else if (trimmed.contains(",")) {
                // Format: "24.5, 60.2, 512"
                val parts = trimmed.split(",")
                for ((idx, part) in parts.withIndex()) {
                    val v = part.trim().toFloatOrNull()
                    if (v != null) values["ch${idx + 1}"] = v
                }
            } else {
                // Single number
                val v = trimmed.toFloatOrNull()
                if (v != null) values["value"] = v
            }

            if (values.isNotEmpty()) {
                feedValues(values)
            }
        } catch (_: Exception) {}
    }

    private fun feedValues(newValues: Map<String, Float>) {
        val currentChannels = _channels.value.toMutableList()
        var channelsChanged = false

        for (key in newValues.keys) {
            if (currentChannels.none { it.id == key }) {
                val color = ChannelColors.getColorForIndex(currentChannels.size)
                currentChannels.add(
                    SensorChannel(
                        id = key,
                        name = formatChannelTitle(key),
                        color = color
                    )
                )
                channelsChanged = true
            }
        }

        if (channelsChanged) {
            _channels.value = currentChannels
        }

        val sample = TelemetrySample(
            timestampMs = System.currentTimeMillis(),
            values = newValues
        )

        val updatedSamples = _samples.value.toMutableList()
        updatedSamples.add(sample)
        while (updatedSamples.size > maxBufferSize) {
            updatedSamples.removeAt(0)
        }
        _samples.value = updatedSamples

        // Update statistics
        computeStatistics(updatedSamples, currentChannels)
    }

    private fun computeStatistics(samplesList: List<TelemetrySample>, channelList: List<SensorChannel>) {
        if (samplesList.isEmpty()) return
        val statsMap = mutableMapOf<String, ChannelStats>()

        for (ch in channelList) {
            val validValues = samplesList.mapNotNull { it.values[ch.id] }
            if (validValues.isNotEmpty()) {
                val current = validValues.last()
                val min = validValues.minOrNull() ?: current
                val max = validValues.maxOrNull() ?: current
                val avg = validValues.average().toFloat()

                statsMap[ch.id] = ChannelStats(
                    channelId = ch.id,
                    channelName = ch.name,
                    color = ch.color,
                    current = current,
                    min = min,
                    max = max,
                    average = avg,
                    sampleCount = validValues.size
                )
            }
        }

        _stats.value = statsMap
    }

    private fun formatChannelTitle(key: String): String {
        return key.replace("_", " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            }
    }

    fun exportToCsv(): String {
        val channelList = _channels.value
        val sampleList = _samples.value
        if (channelList.isEmpty() || sampleList.isEmpty()) {
            return "No data recorded"
        }

        val sb = StringBuilder()
        sb.append("Timestamp,Time_ISO,")
        sb.append(channelList.joinToString(",") { it.name })
        sb.append("\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        for (sample in sampleList) {
            sb.append(sample.timestampMs).append(",")
            sb.append(sdf.format(Date(sample.timestampMs))).append(",")
            sb.append(channelList.joinToString(",") { ch ->
                sample.values[ch.id]?.toString() ?: ""
            })
            sb.append("\n")
        }

        return sb.toString()
    }
}
