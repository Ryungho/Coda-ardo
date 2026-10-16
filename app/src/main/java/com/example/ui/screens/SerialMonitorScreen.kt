package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LineEnding
import com.example.model.SerialDirection
import com.example.model.SerialPortType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.RedAccent
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalGreen
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SerialMonitorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isConnected by viewModel.serialManager.isConnected.collectAsState()
    val messages by viewModel.serialManager.messages.collectAsState()
    val connectedDeviceName by viewModel.serialManager.connectedDeviceName.collectAsState()
    val serialConfig by viewModel.serialConfig.collectAsState()
    val commandInput by viewModel.commandInput.collectAsState()

    var showBaudDropdown by remember { mutableStateOf(false) }
    var showLineEndingDropdown by remember { mutableStateOf(false) }
    var showHexMode by remember { mutableStateOf(false) }
    var autoScroll by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    val baudRates = listOf(300, 1200, 2400, 4800, 9600, 19200, 38400, 57600, 74880, 115200, 230400, 460800, 921600)

    LaunchedEffect(messages.size) {
        if (autoScroll && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Connection & Config Controls
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Top row: Connection status + Connect Button + Port Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Port Type Toggle (USB vs WiFi)
                    Row(
                        modifier = Modifier
                            .background(SlateCard, RoundedCornerShape(20.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable { viewModel.setSerialPortType(SerialPortType.USB_OTG) }
                                .background(
                                    if (serialConfig.portType == SerialPortType.USB_OTG) CyanAccent else Color.Transparent,
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Usb,
                                    contentDescription = null,
                                    tint = if (serialConfig.portType == SerialPortType.USB_OTG) Color.Black else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "USB OTG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (serialConfig.portType == SerialPortType.USB_OTG) Color.Black else Color.Gray
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clickable { viewModel.setSerialPortType(SerialPortType.WIFI_TCP) }
                                .background(
                                    if (serialConfig.portType == SerialPortType.WIFI_TCP) CyanAccent else Color.Transparent,
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = if (serialConfig.portType == SerialPortType.WIFI_TCP) Color.Black else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "WiFi TCP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (serialConfig.portType == SerialPortType.WIFI_TCP) Color.Black else Color.Gray
                                )
                            }
                        }
                    }

                    // Reset DTR Button
                    OutlinedButton(
                        onClick = { viewModel.resetMicrocontroller() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Board", modifier = Modifier.size(14.dp), tint = AmberAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset MCU", fontSize = 11.sp, color = AmberAccent)
                    }

                    // Connect / Disconnect Action Button
                    Button(
                        onClick = { viewModel.toggleSerialConnection() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) RedAccent else TealAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("serial_connect_button")
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.PowerOff else Icons.Default.Power,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isConnected) "Disconnect" else "Connect",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Wi-Fi Host/Port fields if WiFi TCP selected
                if (serialConfig.portType == SerialPortType.WIFI_TCP) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = serialConfig.tcpHost,
                            onValueChange = { viewModel.setTcpHost(it) },
                            label = { Text("IP Address", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(2f),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )
                        OutlinedTextField(
                            value = serialConfig.tcpPort.toString(),
                            onValueChange = { it.toIntOrNull()?.let { p -> viewModel.setTcpPort(p) } },
                            label = { Text("Port", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        )
                    }
                }

                // Second row: Baud rate selector + Line Ending + Clear + Hex toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Baud dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { showBaudDropdown = true }
                                .border(1.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${serialConfig.baudRate} baud",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        DropdownMenu(
                            expanded = showBaudDropdown,
                            onDismissRequest = { showBaudDropdown = false }
                        ) {
                            baudRates.forEach { baud ->
                                DropdownMenuItem(
                                    text = { Text("$baud baud", fontFamily = FontFamily.Monospace) },
                                    onClick = {
                                        viewModel.setBaudRate(baud)
                                        showBaudDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Line Ending dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { showLineEndingDropdown = true }
                                .border(1.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                serialConfig.lineEnding.label,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        DropdownMenu(
                            expanded = showLineEndingDropdown,
                            onDismissRequest = { showLineEndingDropdown = false }
                        ) {
                            LineEnding.values().forEach { ending ->
                                DropdownMenuItem(
                                    text = { Text(ending.label) },
                                    onClick = {
                                        viewModel.setLineEnding(ending)
                                        showLineEndingDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Hex View Toggle
                    FilterChip(
                        selected = showHexMode,
                        onClick = { showHexMode = !showHexMode },
                        label = { Text("HEX", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                            selectedLabelColor = CyanAccent
                        ),
                        modifier = Modifier.height(30.dp)
                    )

                    // Clear button
                    IconButton(
                        onClick = { viewModel.serialManager.clearLog() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Status Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateDark)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isConnected) TealAccent else RedAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = connectedDeviceName ?: "Disconnected",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isConnected) TealAccent else Color.Gray
                )
            }

            Text(
                text = "${messages.size} entries",
                fontSize = 10.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }

        // Terminal Log Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(TerminalBackground)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Usb,
                        contentDescription = null,
                        tint = Color.DarkGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Serial Monitor Ready",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Click 'Connect' to open USB OTG serial port or connect via Wi-Fi Telnet stream.",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { msg ->
                        val timeStr = timeFormat.format(Date(msg.timestamp))
                        val (prefixColor, prefixText) = when (msg.direction) {
                            SerialDirection.RX -> Pair(TerminalGreen, "<-- RX")
                            SerialDirection.TX -> Pair(AmberAccent, "--> TX")
                            SerialDirection.SYSTEM -> Pair(CyanAccent, "SYS")
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "[$timeStr] ",
                                    color = Color(0xFF4A5568),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "$prefixText: ",
                                    color = prefixColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = msg.text,
                                    color = if (msg.direction == SerialDirection.SYSTEM) Color.LightGray else Color(0xFFE2E8F0),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                            if (showHexMode && msg.hexDump.isNotEmpty()) {
                                Text(
                                    text = "     HEX: ${msg.hexDump}",
                                    color = Color(0xFF718096),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Command Macro Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateDark)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val macros = listOf("HELP", "STATUS", "PING", "LED ON", "LED OFF", "ANALOG", "STREAM_ON", "STREAM_OFF", "RESTART")
            macros.forEach { macro ->
                Box(
                    modifier = Modifier
                        .clickable { viewModel.sendMacro(macro) }
                        .background(SlateCard, RoundedCornerShape(12.dp))
                        .border(0.5.dp, SlateCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = macro,
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Bottom Input Command Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commandInput,
                    onValueChange = { viewModel.updateCommandInput(it) },
                    placeholder = { Text("Send serial command...", fontSize = 12.sp) },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.sendCurrentCommand() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SlateCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("serial_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { viewModel.sendCurrentCommand() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("serial_send_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
