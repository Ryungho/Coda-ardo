package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TealAccent

data class SensorChannel(
    val id: String,
    val name: String,
    val color: Color,
    val unit: String = ""
)

data class TelemetrySample(
    val timestampMs: Long,
    val values: Map<String, Float> // channelId -> value
)

data class ChannelStats(
    val channelId: String,
    val channelName: String,
    val color: Color,
    val current: Float,
    val min: Float,
    val max: Float,
    val average: Float,
    val sampleCount: Int
)

object ChannelColors {
    val palette = listOf(
        CyanAccent,
        AmberAccent,
        TealAccent,
        PurpleAccent,
        BlueAccent
    )

    fun getColorForIndex(index: Int): Color {
        return palette[index % palette.size]
    }
}
