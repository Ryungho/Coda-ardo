package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OtaUploadState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TerminalBackground
import com.example.viewmodel.MainViewModel

@Composable
fun FlasherOtaScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val flashMode by viewModel.flashMode.collectAsState()
    val otaIp by viewModel.otaIp.collectAsState()
    val otaPort by viewModel.otaPort.collectAsState()
    val otaAuthToken by viewModel.otaAuthToken.collectAsState()
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val currentSketch by viewModel.currentSketch.collectAsState()
    val compilationResult by viewModel.compilationResult.collectAsState()

    val uploadState by viewModel.flasherManager.uploadState.collectAsState()
    val remoteStatus by viewModel.flasherManager.remoteStatus.collectAsState()
    val logs by viewModel.flasherManager.flasherLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher Card (WiFi OTA vs USB OTG)
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "FIRMWARE UPLOAD PROTOCOL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateDark, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setFlashMode("WIFI_OTA") }
                            .background(
                                if (flashMode == "WIFI_OTA") CyanAccent else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (flashMode == "WIFI_OTA") Color.Black else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "WiFi OTA Updates",
                                fontWeight = FontWeight.Bold,
                                color = if (flashMode == "WIFI_OTA") Color.Black else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setFlashMode("USB_OTG") }
                            .background(
                                if (flashMode == "USB_OTG") CyanAccent else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Usb,
                                contentDescription = null,
                                tint = if (flashMode == "USB_OTG") Color.Black else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "USB OTG Flasher",
                                fontWeight = FontWeight.Bold,
                                color = if (flashMode == "USB_OTG") Color.Black else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // WiFi OTA Configuration & Remote Board Management
        if (flashMode == "WIFI_OTA") {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "REMOTE BOARD (ESP32/ESP8266)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(TealAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Port $otaPort", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = otaIp,
                            onValueChange = { viewModel.setOtaIp(it) },
                            label = { Text("Board IP / Hostname") },
                            singleLine = true,
                            modifier = Modifier.weight(2f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        )
                        OutlinedTextField(
                            value = otaPort,
                            onValueChange = { viewModel.setOtaPort(it) },
                            label = { Text("OTA Port") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = otaAuthToken,
                        onValueChange = { viewModel.setOtaAuthToken(it) },
                        label = { Text("OTA Password / Auth Token (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remote Control Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.checkRemoteBoard() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping & Status", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.rebootRemoteBoard() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reboot Board", fontSize = 12.sp, color = AmberAccent)
                        }
                    }

                    // Remote Board Live Telemetry Dashboard Card
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SlateDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (remoteStatus.isOnline) TealAccent else RedAccent, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (remoteStatus.isOnline) "ESP32 ONLINE" else "OFFLINE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (remoteStatus.isOnline) TealAccent else RedAccent
                                    )
                                }
                                Text(
                                    "Ping: ${remoteStatus.pingMs} ms",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.LightGray
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Free Heap", fontSize = 10.sp, color = Color.Gray)
                                    Text("${remoteStatus.freeHeapBytes / 1024} KB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                                }
                                Column {
                                    Text("WiFi Signal", fontSize = 10.sp, color = Color.Gray)
                                    Text("${remoteStatus.wifiRssiDbm} dBm", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                                }
                                Column {
                                    Text("Uptime", fontSize = 10.sp, color = Color.Gray)
                                    Text("${remoteStatus.uptimeSeconds / 60} min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("Chip Model", fontSize = 10.sp, color = Color.Gray)
                                    Text(remoteStatus.chipModel.take(12), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // USB OTG Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "USB OTG FLASHING (BOOTLOADER)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Uploads firmware directly using the hardware bootloader via USB-C or Micro-USB OTG adapter. Supports STK500v1 (Arduino Uno/Nano) and esptool ROM loader (ESP32/ESP8266).",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Auto-detects USB-UART bridges (CP2102, CH340, FT232, CDC-ACM)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Active Firmware & Target Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "FIRMWARE PAYLOAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            currentSketch.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            "Target: ${selectedBoard.name} (${selectedBoard.mcu})",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }

                    val compiledOk = compilationResult?.success == true
                    Box(
                        modifier = Modifier
                            .background(
                                if (compiledOk) TealAccent.copy(alpha = 0.2f) else AmberAccent.copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (compiledOk) "READY" else "AUTO-COMPILE",
                            color = if (compiledOk) TealAccent else AmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Big Flash Button
        val isUploading = uploadState is OtaUploadState.Uploading || uploadState is OtaUploadState.Connecting
        Button(
            onClick = { viewModel.startFlashUpload() },
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = if (flashMode == "WIFI_OTA") CyanAccent else AmberAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_upload_button")
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploading Firmware...", color = Color.Black, fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (flashMode == "WIFI_OTA") "Upload via WiFi OTA" else "Flash Code via USB OTG",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Live Upload Progress Indicator
        AnimatedVisibility(visible = uploadState !is OtaUploadState.Idle) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    when (val state = uploadState) {
                        is OtaUploadState.Connecting -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyanAccent, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Handshaking with bootloader...", color = CyanAccent, fontSize = 12.sp)
                            }
                        }
                        is OtaUploadState.Uploading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(state.stage, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${(state.progress * 100).toInt()}%", fontSize = 12.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { state.progress },
                                color = CyanAccent,
                                trackColor = SlateCardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${state.bytesSent / 1024} KB / ${state.totalBytes / 1024} KB", fontSize = 10.sp, color = Color.Gray)
                                Text("${String.format("%.1f", state.speedKbps)} KB/s", fontSize = 10.sp, color = AmberAccent, fontFamily = FontFamily.Monospace)
                            }
                        }
                        is OtaUploadState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TealAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.message, color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        is OtaUploadState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.message, color = RedAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Flasher & Bootloader Terminal Logs
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "FLASHER EXECUTION LOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Row {
                        IconButton(
                            onClick = { viewModel.copyToClipboard("Flasher Log", logs.joinToString("\n")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.flasherManager.clearLogs() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(TerminalBackground, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    if (logs.isEmpty()) {
                        Text(
                            "Ready. Click 'Upload' to begin flashing.",
                            color = Color.DarkGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("SUCCESS", ignoreCase = true) || log.contains("Complete", ignoreCase = true)) TealAccent else Color(0xFFCBD5E1),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
