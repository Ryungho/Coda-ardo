package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChannelStats
import com.example.model.SensorChannel
import com.example.model.TelemetrySample
import com.example.sensor.SimulationPattern
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TerminalBackground
import com.example.viewmodel.MainViewModel

@Composable
fun SensorGraphScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val samples by viewModel.telemetryManager.samples.collectAsState()
    val channels by viewModel.telemetryManager.channels.collectAsState()
    val stats by viewModel.telemetryManager.stats.collectAsState()
    val isPaused by viewModel.telemetryManager.isPaused.collectAsState()
    val isSimulating by viewModel.telemetryManager.isSimulating.collectAsState()
    val simPattern by viewModel.telemetryManager.simulationPattern.collectAsState()

    var showSimMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedBufferSize by remember { mutableStateOf(120) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Controls Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulation Pattern Dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { showSimMenu = true }
                                .background(SlateCard, RoundedCornerShape(8.dp))
                                .border(1.dp, SlateCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                simPattern.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showSimMenu,
                            onDismissRequest = { showSimMenu = false }
                        ) {
                            SimulationPattern.values().forEach { pattern ->
                                DropdownMenuItem(
                                    text = { Text(pattern.title) },
                                    onClick = {
                                        viewModel.telemetryManager.setSimulationPattern(pattern)
                                        showSimMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Simulation Generator Toggle Chip
                    FilterChip(
                        selected = isSimulating,
                        onClick = { viewModel.telemetryManager.toggleSimulation() },
                        label = { Text(if (isSimulating) "Sim ON" else "Live Only", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealAccent.copy(alpha = 0.2f),
                            selectedLabelColor = TealAccent
                        ),
                        modifier = Modifier.height(32.dp)
                    )

                    // Pause / Freeze Button
                    IconButton(
                        onClick = { viewModel.telemetryManager.togglePause() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = if (isPaused) AmberAccent else Color.White
                        )
                    }

                    // Export / Share CSV Button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", tint = CyanAccent)
                    }

                    // Clear Data Button
                    IconButton(
                        onClick = { viewModel.telemetryManager.clearData() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.LightGray)
                    }
                }

                // Buffer Size chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Window: ${selectedBufferSize} pts",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(30, 60, 120, 240).forEach { size ->
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        selectedBufferSize = size
                                        viewModel.telemetryManager.setMaxBufferSize(size)
                                    }
                                    .background(
                                        if (selectedBufferSize == size) CyanAccent.copy(alpha = 0.2f) else SlateCard,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        0.5.dp,
                                        if (selectedBufferSize == size) CyanAccent else SlateCardBorder,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "$size",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (selectedBufferSize == size) CyanAccent else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Oscilloscope Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(TerminalBackground)
                .padding(8.dp)
        ) {
            OscilloscopeCanvas(
                samples = samples,
                channels = channels,
                stats = stats,
                modifier = Modifier.fillMaxSize()
            )

            if (isPaused) {
                Surface(
                    color = AmberAccent.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        "FREEZE FRAME",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Live Channel Statistics Strip
        Surface(
            color = SlateDark,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(channels) { ch ->
                    val stat = stats[ch.id]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(160.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(ch.color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        ch.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (stat != null) String.format("%.2f", stat.current) else "--",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = ch.color
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Min: ${if (stat != null) String.format("%.1f", stat.min) else "--"}",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "Max: ${if (stat != null) String.format("%.1f", stat.max) else "--"}",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    "Avg: ${if (stat != null) String.format("%.1f", stat.average) else "--"}",
                                    fontSize = 9.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        val csvData = viewModel.telemetryManager.exportToCsv()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Sensor Telemetry") },
            text = {
                Column {
                    Text("Recorded data formatted as CSV ready for Excel or analysis:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(TerminalBackground, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = csvData,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.copyToClipboard("Sensor Telemetry CSV", csvData)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Copy CSV", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.shareText("Sensor Telemetry CSV", csvData)
                        showExportDialog = false
                    }
                ) {
                    Text("Share")
                }
            }
        )
    }
}

@Composable
fun OscilloscopeCanvas(
    samples: List<TelemetrySample>,
    channels: List<SensorChannel>,
    stats: Map<String, ChannelStats>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Background subtle grid lines
        val gridColor = Color(0xFF1E293B)
        val gridEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        // 4 Horizontal divisions
        for (i in 1..4) {
            val y = height * (i / 5f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = gridEffect
            )
        }

        // 6 Vertical divisions
        for (i in 1..6) {
            val x = width * (i / 7f)
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f,
                pathEffect = gridEffect
            )
        }

        if (samples.size < 2 || channels.isEmpty()) return@Canvas

        // Find global or per-channel min/max to normalize
        var globalMin = Float.MAX_VALUE
        var globalMax = Float.MIN_VALUE

        for (ch in channels) {
            val s = stats[ch.id] ?: continue
            if (s.min < globalMin) globalMin = s.min
            if (s.max > globalMax) globalMax = s.max
        }

        if (globalMin >= globalMax) {
            globalMin -= 1f
            globalMax += 1f
        }

        val range = (globalMax - globalMin).coerceAtLeast(0.001f)
        val stepX = width / (samples.size - 1).coerceAtLeast(1)

        // Draw each channel's line waveform
        for (ch in channels) {
            val path = Path()
            val fillPath = Path()
            var firstPoint = true

            for ((idx, sample) in samples.withIndex()) {
                val value = sample.values[ch.id] ?: continue
                val normalizedY = (value - globalMin) / range
                val x = idx * stepX
                val y = height - (normalizedY * (height * 0.82f)) - (height * 0.08f)

                if (firstPoint) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                    firstPoint = false
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            if (!firstPoint) {
                fillPath.lineTo(width, height)
                fillPath.close()

                // Subtle gradient glow under waveform
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(ch.color.copy(alpha = 0.18f), Color.Transparent)
                    )
                )

                // Main stroke
                drawPath(
                    path = path,
                    color = ch.color,
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}
