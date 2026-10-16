package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardCatalog
import com.example.model.CompilationIssue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val codeText by viewModel.codeText.collectAsState()
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val currentSketch by viewModel.currentSketch.collectAsState()
    val sketches by viewModel.sketches.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()
    val compilationResult by viewModel.compilationResult.collectAsState()
    val showConsole by viewModel.showCompileConsole.collectAsState()

    var showBoardDialog by remember { mutableStateOf(false) }
    var showNewSketchDialog by remember { mutableStateOf(false) }
    var newSketchName by remember { mutableStateOf("") }
    var showSketchMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Toolbar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sketch selector button
                Box {
                    Row(
                        modifier = Modifier
                            .clickable { showSketchMenu = true }
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Sketch",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentSketch.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSketchMenu,
                        onDismissRequest = { showSketchMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("+ New Sketch...", fontWeight = FontWeight.Bold, color = CyanAccent) },
                            onClick = {
                                showSketchMenu = false
                                showNewSketchDialog = true
                            }
                        )
                        sketches.forEach { sketch ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(sketch.name, fontWeight = if (sketch.id == currentSketch.id) FontWeight.Bold else FontWeight.Normal)
                                        Text(sketch.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                                    }
                                },
                                onClick = {
                                    viewModel.loadSketch(sketch)
                                    showSketchMenu = false
                                }
                            )
                        }
                    }
                }

                // Board Chip
                Row(
                    modifier = Modifier
                        .clickable { showBoardDialog = true }
                        .border(1.dp, SlateCardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(TealAccent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedBoard.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Verify / Compile Button
                Button(
                    onClick = { viewModel.compileSketch() },
                    enabled = !isCompiling,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("compile_button")
                ) {
                    if (isCompiling) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifying...", color = Color.Black, style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Verify",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compile", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Quick Snippets / Symbols Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateDark)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickTokens = listOf(
                "{" to "}",
                "(" to ")",
                ";" to "",
                "\"" to "\"",
                "setup/loop" to "\nvoid setup() {\n  \n}\n\nvoid loop() {\n  \n}\n",
                "pinMode" to "pinMode(LED_BUILTIN, OUTPUT);",
                "digitalWrite" to "digitalWrite(LED_BUILTIN, HIGH);",
                "analogRead" to "int val = analogRead(A0);",
                "Serial.print" to "Serial.println(\"Hello\");",
                "delay" to "delay(1000);",
                "WiFi.begin" to "WiFi.begin(ssid, password);"
            )

            quickTokens.forEach { (label, insert) ->
                Box(
                    modifier = Modifier
                        .clickable {
                            val toInsert = if (insert.isNotEmpty() && insert != label) insert else label
                            viewModel.updateCode(codeText + toInsert)
                        }
                        .background(SlateCard, RoundedCornerShape(6.dp))
                        .border(0.5.dp, SlateCardBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = CyanAccent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Main Editor Surface
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(TerminalBackground)
        ) {
            val lines = codeText.split("\n")
            val lineCount = lines.size.coerceAtLeast(1)

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .background(SlateDark)
                        .padding(vertical = 12.dp, horizontal = 6.dp)
                        .width(36.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            color = Color(0xFF4A5568),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Code Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp, horizontal = 10.dp)
                ) {
                    BasicTextField(
                        value = codeText,
                        onValueChange = { viewModel.updateCode(it) },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_editor_field")
                    )
                }
            }

            // Floating Toggle for Console if hidden
            if (!showConsole && compilationResult != null) {
                Surface(
                    color = SlateCard,
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clickable { viewModel.toggleCompileConsole() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isOk = compilationResult?.success == true
                        Icon(
                            imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (isOk) TealAccent else RedAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOk) "Build Verified" else "Build Failed",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Expandable Compilation Console Bottom Panel
        AnimatedVisibility(visible = showConsole) {
            compilationResult?.let { result ->
                CompilationConsolePanel(
                    result = result,
                    onClose = { viewModel.toggleCompileConsole() },
                    onCopyLog = { viewModel.copyToClipboard("Compile Log", result.buildLogs.joinToString("\n")) }
                )
            }
        }
    }

    // New Sketch Dialog
    if (showNewSketchDialog) {
        AlertDialog(
            onDismissRequest = { showNewSketchDialog = false },
            title = { Text("Create New Sketch") },
            text = {
                OutlinedTextField(
                    value = newSketchName,
                    onValueChange = { newSketchName = it },
                    label = { Text("Sketch Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        focusedLabelColor = CyanAccent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNewSketch(newSketchName)
                        newSketchName = ""
                        showNewSketchDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Create", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSketchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Board Switcher Dialog
    if (showBoardDialog) {
        AlertDialog(
            onDismissRequest = { showBoardDialog = false },
            title = { Text("Select Target Microcontroller") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(BoardCatalog.allBoards) { board ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (board.id == selectedBoard.id) MaterialTheme.colorScheme.primaryContainer else SlateCard
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.selectBoard(board)
                                    showBoardDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = board.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${board.mcu} @ ${board.clockSpeedMhz}MHz",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                if (board.supportsWifiOta) {
                                    Box(
                                        modifier = Modifier
                                            .background(TealAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("OTA / WiFi", color = TealAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBoardDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CompilationConsolePanel(
    result: com.example.model.CompilationResult,
    onClose: () -> Unit,
    onCopyLog: () -> Unit
) {
    Surface(
        color = SlateDark,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (result.success) TealAccent else RedAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (result.success) "Compilation Succeeded" else "Compilation Failed (${result.issues.count { !it.isWarning }} errors)",
                        fontWeight = FontWeight.Bold,
                        color = if (result.success) TealAccent else RedAccent,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Row {
                    IconButton(onClick = onCopyLog, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Log", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Memory usage bars if success
            if (result.success) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flash
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Flash (ROM)", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format("%.1f", result.flashPercentage)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            LinearProgressIndicator(
                                progress = { (result.flashPercentage / 100f).coerceIn(0f, 1f) },
                                color = CyanAccent,
                                trackColor = SlateCardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                            Text(
                                "${result.flashUsedBytes / 1024} KB / ${result.flashTotalBytes / 1024} KB",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }

                    // SRAM
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RAM (SRAM)", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format("%.1f", result.ramPercentage)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                            }
                            LinearProgressIndicator(
                                progress = { (result.ramPercentage / 100f).coerceIn(0f, 1f) },
                                color = AmberAccent,
                                trackColor = SlateCardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                            Text(
                                "${result.ramUsedBytes} B / ${result.ramTotalBytes / 1024} KB",
                                fontSize = 10.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            // Issues or Logs List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(TerminalBackground, RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                if (result.issues.isNotEmpty()) {
                    item {
                        Text(
                            "DIAGNOSTIC ISSUES:",
                            color = AmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    items(result.issues) { issue ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (issue.isWarning) Icons.Default.Warning else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (issue.isWarning) AmberAccent else RedAccent,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Line ${issue.line}, Col ${issue.column}: ${issue.message}",
                                color = if (issue.isWarning) AmberAccent else RedAccent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "BUILD OUTPUT LOG:",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                items(result.buildLogs) { log ->
                    Text(
                        text = log,
                        color = Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
