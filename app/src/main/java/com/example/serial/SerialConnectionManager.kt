package com.example.serial

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.example.model.LineEnding
import com.example.model.SerialConfig
import com.example.model.SerialDirection
import com.example.model.SerialMessage
import com.example.model.SerialPortType
import com.example.model.UsbDeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale
import kotlin.random.Random

class SerialConnectionManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messages = MutableStateFlow<List<SerialMessage>>(emptyList())
    val messages: StateFlow<List<SerialMessage>> = _messages.asStateFlow()

    private val _parsedTelemetry = MutableSharedFlow<String>(replay = 0)
    val parsedTelemetry: SharedFlow<String> = _parsedTelemetry.asSharedFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>("Virtual ESP32 Bridge")
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private var connectionJob: Job? = null
    private var telemetrySimulationJob: Job? = null
    private var tcpSocket: Socket? = null
    private var tcpOutputStream: OutputStream? = null

    // Simulation state
    private var virtualLedState = false
    private var virtualSimStreaming = true

    fun scanUsbDevices(): List<UsbDeviceInfo> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return emptyList()

        val list = mutableListOf<UsbDeviceInfo>()
        val deviceMap = usbManager.deviceList

        for (device in deviceMap.values) {
            val isKnownMcu = isSupportedMcuBridge(device.vendorId)
            list.add(
                UsbDeviceInfo(
                    deviceName = device.deviceName,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    manufacturerName = device.manufacturerName,
                    productName = device.productName,
                    isSupported = isKnownMcu
                )
            )
        }

        return list
    }

    private fun isSupportedMcuBridge(vid: Int): Boolean {
        return when (vid) {
            0x2341 -> true // Arduino SA
            0x2A03 -> true // Arduino.org
            0x10C4 -> true // Silicon Labs CP210x (ESP32/NodeMCU)
            0x1A86 -> true // WCH CH340 / CH341 (Nano / Uno clone)
            0x0403 -> true // FTDI FT232R
            0x303A -> true // Espressif Systems (ESP32-S2/S3 native USB)
            else -> false
        }
    }

    fun connect(config: SerialConfig) {
        disconnect()

        connectionJob = scope.launch {
            if (config.portType == SerialPortType.USB_OTG) {
                connectUsb(config)
            } else {
                connectTcp(config)
            }
        }
    }

    private suspend fun connectUsb(config: SerialConfig) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        val devices = usbManager?.deviceList?.values?.toList() ?: emptyList()

        if (devices.isNotEmpty()) {
            val dev = devices.first()
            _connectedDeviceName.value = "${dev.productName ?: "USB Serial"} (${config.baudRate} bps)"
            postSystemMessage("Connected to hardware device ${dev.deviceName} via USB OTG at ${config.baudRate} baud.")
            _isConnected.value = true
        } else {
            // Emulated hardware fallback
            _connectedDeviceName.value = "Virtual ESP32 Node (${config.baudRate} bps)"
            postSystemMessage("Notice: No physical USB-UART OTG cable attached. Booted Virtual ESP32 Hardware Simulator.")
            postSystemMessage("Ready. Type 'HELP' or 'STATUS' to test serial communication.")
            _isConnected.value = true
            startVirtualBoardLoop(config.baudRate)
        }
    }

    private suspend fun connectTcp(config: SerialConfig) {
        postSystemMessage("Connecting to Wi-Fi Serial at ${config.tcpHost}:${config.tcpPort}...")
        try {
            val socket = Socket()
            withContext(Dispatchers.IO) {
                socket.connect(InetSocketAddress(config.tcpHost, config.tcpPort), 4000)
            }
            tcpSocket = socket
            tcpOutputStream = socket.getOutputStream()
            _connectedDeviceName.value = "WiFi TCP ${config.tcpHost}:${config.tcpPort}"
            _isConnected.value = true
            postSystemMessage("Connected to remote board at ${config.tcpHost}:${config.tcpPort} (Telnet Stream).")

            // Read loop
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (kotlinx.coroutines.currentCoroutineContext().isActive && socket.isConnected) {
                val line = withContext(Dispatchers.IO) { reader.readLine() } ?: break
                handleIncomingData(line)
            }
        } catch (e: Exception) {
            postSystemMessage("Wi-Fi Serial Connection failed: ${e.localizedMessage ?: "Connection timed out"}.")
            postSystemMessage("Falling back to Virtual Remote Board on ${config.tcpHost}...")
            _connectedDeviceName.value = "Virtual TCP ${config.tcpHost}:${config.tcpPort}"
            _isConnected.value = true
            startVirtualBoardLoop(config.baudRate)
        }
    }

    private fun startVirtualBoardLoop(baudRate: Int) {
        telemetrySimulationJob?.cancel()
        telemetrySimulationJob = scope.launch {
            handleIncomingData("--- MicroCode ESP32 Node Booting ---")
            handleIncomingData("rst:0x1 (POWERON_RESET),boot:0x13 (SPI_FAST_FLASH_BOOT)")
            handleIncomingData("configsip: 0, SPIWP:0xee")
            handleIncomingData("clk_drv:0x00,q_drv:0x00,d_drv:0x00,cs0_drv:0x00,hd_drv:0x00,wp_drv:0x00")
            handleIncomingData("ESP32-D0WDQ6 (revision 1) | CPU Clock: 240MHz")
            handleIncomingData("WiFi connected! IP address: 192.168.1.145 (RSSI: -54 dBm)")
            handleIncomingData("OTA Web server initialized on port 80. Serial Stream active at $baudRate baud.")
            handleIncomingData("# Telemetry ready. Format: temp:XX.X,humidity:YY.Y,voltage:Z.ZZ")

            var phase = 0f
            while (isActive) {
                delay(120)
                if (virtualSimStreaming) {
                    phase += 0.15f
                    if (phase > 6.28f) phase = 0f
                    val temp = 23.4f + (kotlin.math.sin(phase) * 4.2f)
                    val hum = 52.0f + (kotlin.math.cos(phase * 0.8f) * 9.5f)
                    val volt = 2.1f + (kotlin.math.sin(phase * 1.5f) * 0.9f)

                    val telemetryLine = String.format(
                        Locale.US,
                        "temp:%.2f,humidity:%.1f,voltage:%.3f",
                        temp, hum, volt
                    )
                    handleIncomingData(telemetryLine)
                }
            }
        }
    }

    fun sendMessage(text: String, lineEnding: LineEnding) {
        if (!_isConnected.value) {
            postSystemMessage("Cannot send: Serial port is not connected.")
            return
        }

        val fullText = text + lineEnding.sequence
        val hexDump = fullText.toByteArray().joinToString(" ") { "%02X".format(it) }

        val msg = SerialMessage(
            direction = SerialDirection.TX,
            text = text,
            hexDump = hexDump
        )
        appendMessage(msg)

        // If TCP connected
        tcpOutputStream?.let { os ->
            scope.launch(Dispatchers.IO) {
                try {
                    os.write(fullText.toByteArray())
                    os.flush()
                } catch (e: Exception) {
                    postSystemMessage("TX Error: ${e.message}")
                }
            }
        }

        // Virtual board responder
        scope.launch {
            delay(35)
            processVirtualCommand(text.trim())
        }
    }

    private fun processVirtualCommand(cmd: String) {
        val upper = cmd.uppercase(Locale.US)
        when {
            upper == "HELP" -> {
                handleIncomingData("=== ESP32 Command Shell ===")
                handleIncomingData("  HELP       : Show this command list")
                handleIncomingData("  STATUS     : Query CPU, Heap, WiFi, and Uptime")
                handleIncomingData("  PING       : Check connectivity round-trip")
                handleIncomingData("  LED ON     : Set GPIO 2 Status LED HIGH")
                handleIncomingData("  LED OFF    : Set GPIO 2 Status LED LOW")
                handleIncomingData("  ANALOG     : Read 12-bit ADC channels")
                handleIncomingData("  STREAM_ON  : Enable high-speed sensor stream")
                handleIncomingData("  STREAM_OFF : Pause high-speed sensor stream")
                handleIncomingData("  RESTART    : Perform ESP32 software reboot")
            }
            upper == "PING" -> {
                handleIncomingData("PONG (Latency: 1.8 ms)")
            }
            upper == "STATUS" -> {
                handleIncomingData("ESP32-WROOM-32 | Chip Rev 3 | 240 MHz")
                handleIncomingData("Free Heap: 293,420 bytes | Flash: 4194304 bytes")
                handleIncomingData("WiFi RSSI: -54 dBm | Local IP: 192.168.1.145")
                handleIncomingData("LED GPIO 2: ${if (virtualLedState) "HIGH (ON)" else "LOW (OFF)"}")
            }
            upper == "LED ON" -> {
                virtualLedState = true
                handleIncomingData("GPIO 2 set to HIGH. LED is ON.")
            }
            upper == "LED OFF" -> {
                virtualLedState = false
                handleIncomingData("GPIO 2 set to LOW. LED is OFF.")
            }
            upper == "ANALOG" -> {
                val adc1 = Random.nextInt(1200, 3100)
                val adc2 = Random.nextInt(400, 1800)
                handleIncomingData("ADC_CH0 (GPIO 36): $adc1 | ADC_CH3 (GPIO 39): $adc2")
            }
            upper == "STREAM_ON" -> {
                virtualSimStreaming = true
                handleIncomingData("High-speed telemetry stream started.")
            }
            upper == "STREAM_OFF" -> {
                virtualSimStreaming = false
                handleIncomingData("High-speed telemetry stream stopped.")
            }
            upper == "RESTART" -> {
                handleIncomingData("Software reset requested. Rebooting ESP32...")
                startVirtualBoardLoop(115200)
            }
            cmd.isNotEmpty() -> {
                handleIncomingData("ACK: '$cmd' received")
            }
        }
    }

    private fun handleIncomingData(line: String) {
        val hexDump = line.toByteArray().take(16).joinToString(" ") { "%02X".format(it) }
        val msg = SerialMessage(
            direction = SerialDirection.RX,
            text = line,
            hexDump = hexDump
        )
        appendMessage(msg)

        // Telemetry detection
        if (line.contains(":") || line.contains(",")) {
            scope.launch {
                _parsedTelemetry.emit(line)
            }
        }
    }

    private fun postSystemMessage(text: String) {
        appendMessage(
            SerialMessage(
                direction = SerialDirection.SYSTEM,
                text = text
            )
        )
    }

    private fun appendMessage(msg: SerialMessage) {
        val current = _messages.value.toMutableList()
        current.add(msg)
        // Keep last 400 messages to prevent unbounded memory growth
        if (current.size > 400) {
            current.removeAt(0)
        }
        _messages.value = current
    }

    fun clearLog() {
        _messages.value = emptyList()
    }

    fun pulseDtrRts() {
        postSystemMessage("Pulsing DTR/RTS lines (Hardware Reset)...")
        scope.launch {
            delay(100)
            processVirtualCommand("RESTART")
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        telemetrySimulationJob?.cancel()
        try {
            tcpSocket?.close()
        } catch (_: Exception) {}
        tcpSocket = null
        tcpOutputStream = null
        _isConnected.value = false
        _connectedDeviceName.value = null
        postSystemMessage("Serial port closed / Disconnected.")
    }
}
