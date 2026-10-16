package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Board
import com.example.model.BoardCatalog
import com.example.model.PinType
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TerminalBackground
import com.example.viewmodel.MainViewModel

@Composable
fun BoardsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeBoard by viewModel.selectedBoard.collectAsState()
    val inspectingBoard by viewModel.inspectingBoard.collectAsState()
    val sketches by viewModel.sketches.collectAsState()

    var selectedPinFilter by remember { mutableStateOf<PinType?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Board Selector Strip
        item {
            Text(
                "SUPPORTED MICROCONTROLLER BOARDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BoardCatalog.allBoards) { board ->
                    val isInspecting = board.id == inspectingBoard.id
                    val isActiveTarget = board.id == activeBoard.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isInspecting) SlateCard else SlateDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActiveTarget) TealAccent else if (isInspecting) CyanAccent else SlateCardBorder
                        ),
                        modifier = Modifier
                            .width(170.dp)
                            .clickable { viewModel.inspectBoard(board) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = if (isActiveTarget) TealAccent else CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (isActiveTarget) {
                                    Box(
                                        modifier = Modifier
                                            .background(TealAccent, CircleShape)
                                            .size(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                board.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                board.mcu.take(18),
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // Selected Board Specifications Card
        item {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                inspectingBoard.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                inspectingBoard.description,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }

                        if (inspectingBoard.id != activeBoard.id) {
                            Button(
                                onClick = { viewModel.selectBoard(inspectingBoard) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("set_active_board_button")
                            ) {
                                Text("Select Target", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Surface(
                                color = TealAccent.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = TealAccent, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Active Target", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Specs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem("Clock Speed", "${inspectingBoard.clockSpeedMhz} MHz")
                        SpecItem("Flash (ROM)", "${inspectingBoard.flashBytes / 1024} KB")
                        SpecItem("SRAM (RAM)", if (inspectingBoard.ramBytes >= 1024 * 1024) "${inspectingBoard.ramBytes / (1024 * 1024)} MB" else "${inspectingBoard.ramBytes / 1024} KB")
                        SpecItem("Voltage", inspectingBoard.operatingVoltage)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecItem("Default Baud", "${inspectingBoard.defaultBaudRate} bps")
                        SpecItem("WiFi & OTA", if (inspectingBoard.supportsWifiOta) "Supported" else "No (USB Only)")
                        SpecItem("Architecture", inspectingBoard.family.name)
                    }
                }
            }
        }

        // Pinout Reference Table
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "HARDWARE PINOUT & GPIO MATRIX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pin Type Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedPinFilter == null,
                            onClick = { selectedPinFilter = null },
                            label = { Text("All (${inspectingBoard.pins.size})", fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                        PinType.values().forEach { type ->
                            val count = inspectingBoard.pins.count { it.type == type }
                            if (count > 0) {
                                FilterChip(
                                    selected = selectedPinFilter == type,
                                    onClick = { selectedPinFilter = type },
                                    label = { Text("${type.name} ($count)", fontSize = 10.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }

                    // Pin items table
                    val filteredPins = if (selectedPinFilter == null) {
                        inspectingBoard.pins
                    } else {
                        inspectingBoard.pins.filter { it.type == selectedPinFilter }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TerminalBackground, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredPins.forEach { pin ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeBg, badgeTextColor) = when (pin.type) {
                                        PinType.DIGITAL -> Pair(CyanAccent.copy(alpha = 0.2f), CyanAccent)
                                        PinType.ANALOG -> Pair(AmberAccent.copy(alpha = 0.2f), AmberAccent)
                                        PinType.COMMUNICATION -> Pair(PurpleAccent.copy(alpha = 0.2f), PurpleAccent)
                                        PinType.POWER -> Pair(Color.Red.copy(alpha = 0.2f), Color.Red)
                                        PinType.SPECIAL -> Pair(TealAccent.copy(alpha = 0.2f), TealAccent)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(badgeBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            pin.name,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            color = badgeTextColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        pin.description,
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }

                                Row {
                                    if (pin.isPwm) {
                                        Box(
                                            modifier = Modifier
                                                .background(AmberAccent.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("PWM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    if (pin.isInterrupt) {
                                        Box(
                                            modifier = Modifier
                                                .background(TealAccent.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text("INT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TealAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Example Sketches For This Board
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "EXAMPLE SKETCHES FOR THIS ARCHITECTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    sketches.forEach { sketch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sketch.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                Text(sketch.description, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { viewModel.loadSketch(sketch) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanAccent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open", fontSize = 11.sp, color = CyanAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
    }
}
