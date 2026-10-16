package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.BoardsScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FlasherOtaScreen
import com.example.ui.screens.SensorGraphScreen
import com.example.ui.screens.SerialMonitorScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TerminalBackground
import com.example.viewmodel.AppTab
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val selectedBoard by viewModel.selectedBoard.collectAsState()
                val isSerialConnected by viewModel.serialManager.isConnected.collectAsState()
                val snackbarMessage by viewModel.snackbarMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Handle back button when not on Editor tab
                BackHandler(enabled = currentTab != AppTab.EDITOR) {
                    viewModel.selectTab(AppTab.EDITOR)
                }

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearFeedback()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = CyanAccent.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Memory,
                                                    contentDescription = null,
                                                    tint = CyanAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "MicroCode",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = selectedBoard.name,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = CyanAccent
                                            )
                                        }
                                    }

                                    // Serial connection status pill
                                    Row(
                                        modifier = Modifier
                                            .clickable { viewModel.selectTab(AppTab.SERIAL) }
                                            .background(SlateCard, RoundedCornerShape(16.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (isSerialConnected) TealAccent else Color.Gray,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isSerialConnected) "ONLINE" else "DISCONNECTED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSerialConnected) TealAccent else Color.Gray
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.EDITOR,
                                onClick = { viewModel.selectTab(AppTab.EDITOR) },
                                icon = { Icon(Icons.Default.Code, contentDescription = "Editor") },
                                label = { Text("Editor", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanAccent
                                ),
                                modifier = Modifier.testTag("tab_editor")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.SERIAL,
                                onClick = { viewModel.selectTab(AppTab.SERIAL) },
                                icon = { Icon(Icons.Default.Terminal, contentDescription = "Serial") },
                                label = { Text("Serial", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanAccent
                                ),
                                modifier = Modifier.testTag("tab_serial")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.FLASHER,
                                onClick = { viewModel.selectTab(AppTab.FLASHER) },
                                icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Flasher & OTA") },
                                label = { Text("Flasher", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanAccent
                                ),
                                modifier = Modifier.testTag("tab_flasher")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.TELEMETRY,
                                onClick = { viewModel.selectTab(AppTab.TELEMETRY) },
                                icon = { Icon(Icons.Default.AutoGraph, contentDescription = "Sensor Graph") },
                                label = { Text("Graph", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanAccent
                                ),
                                modifier = Modifier.testTag("tab_sensors")
                            )

                            NavigationBarItem(
                                selected = currentTab == AppTab.BOARDS,
                                onClick = { viewModel.selectTab(AppTab.BOARDS) },
                                icon = { Icon(Icons.Default.Memory, contentDescription = "Boards") },
                                label = { Text("Boards", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = CyanAccent
                                ),
                                modifier = Modifier.testTag("tab_boards")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            AppTab.EDITOR -> EditorScreen(viewModel = viewModel)
                            AppTab.SERIAL -> SerialMonitorScreen(viewModel = viewModel)
                            AppTab.FLASHER -> FlasherOtaScreen(viewModel = viewModel)
                            AppTab.TELEMETRY -> SensorGraphScreen(viewModel = viewModel)
                            AppTab.BOARDS -> BoardsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
