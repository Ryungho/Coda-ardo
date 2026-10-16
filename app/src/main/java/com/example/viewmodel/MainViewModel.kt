package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.compiler.ArduinoCompilerEngine
import com.example.flasher.FlasherOtaManager
import com.example.model.Board
import com.example.model.BoardCatalog
import com.example.model.CompilationResult
import com.example.model.LineEnding
import com.example.model.SerialConfig
import com.example.model.SerialPortType
import com.example.model.Sketch
import com.example.model.SketchPresets
import com.example.sensor.SensorTelemetryManager
import com.example.sensor.SimulationPattern
import com.example.serial.SerialConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    EDITOR("Editor"),
    SERIAL("Serial Monitor"),
    FLASHER("Flasher & OTA"),
    TELEMETRY("Sensor Graph"),
    BOARDS("Boards & Pinouts")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val serialManager = SerialConnectionManager(application)
    val flasherManager = FlasherOtaManager()
    val telemetryManager = SensorTelemetryManager()

    private val _currentTab = MutableStateFlow(AppTab.EDITOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Editor state
    private val _sketches = MutableStateFlow<List<Sketch>>(SketchPresets.allPresets)
    val sketches: StateFlow<List<Sketch>> = _sketches.asStateFlow()

    private val _currentSketch = MutableStateFlow<Sketch>(SketchPresets.ESP32_OTA_SERVER)
    val currentSketch: StateFlow<Sketch> = _currentSketch.asStateFlow()

    private val _codeText = MutableStateFlow(SketchPresets.ESP32_OTA_SERVER.code)
    val codeText: StateFlow<String> = _codeText.asStateFlow()

    private val _selectedBoard = MutableStateFlow(BoardCatalog.ESP32_WROOM)
    val selectedBoard: StateFlow<Board> = _selectedBoard.asStateFlow()

    // Compilation State
    private val _compilationResult = MutableStateFlow<CompilationResult?>(null)
    val compilationResult: StateFlow<CompilationResult?> = _compilationResult.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    private val _showCompileConsole = MutableStateFlow(false)
    val showCompileConsole: StateFlow<Boolean> = _showCompileConsole.asStateFlow()

    // Serial state
    private val _serialConfig = MutableStateFlow(SerialConfig())
    val serialConfig: StateFlow<SerialConfig> = _serialConfig.asStateFlow()

    private val _commandInput = MutableStateFlow("")
    val commandInput: StateFlow<String> = _commandInput.asStateFlow()

    // OTA Configuration
    private val _otaIp = MutableStateFlow("192.168.1.145")
    val otaIp: StateFlow<String> = _otaIp.asStateFlow()

    private val _otaPort = MutableStateFlow("80")
    val otaPort: StateFlow<String> = _otaPort.asStateFlow()

    private val _otaAuthToken = MutableStateFlow("")
    val otaAuthToken: StateFlow<String> = _otaAuthToken.asStateFlow()

    private val _flashMode = MutableStateFlow("WIFI_OTA") // "WIFI_OTA" or "USB_OTG"
    val flashMode: StateFlow<String> = _flashMode.asStateFlow()

    // Board detail preview
    private val _inspectingBoard = MutableStateFlow<Board>(BoardCatalog.ESP32_WROOM)
    val inspectingBoard: StateFlow<Board> = _inspectingBoard.asStateFlow()

    // Snackbar / Feedback message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        // Automatically pipe serial monitor text stream into live sensor graph
        viewModelScope.launch {
            serialManager.parsedTelemetry.collect { line ->
                telemetryManager.parseLine(line)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateCode(newCode: String) {
        _codeText.value = newCode
    }

    fun selectBoard(board: Board) {
        _selectedBoard.value = board
        _inspectingBoard.value = board
        _otaPort.value = board.defaultOtaPort.toString()
        _serialConfig.value = _serialConfig.value.copy(baudRate = board.defaultBaudRate)
        showFeedback("Target board set to ${board.name}")
    }

    fun inspectBoard(board: Board) {
        _inspectingBoard.value = board
    }

    fun loadSketch(sketch: Sketch) {
        _currentSketch.value = sketch
        _codeText.value = sketch.code
        val target = BoardCatalog.getBoardById(sketch.targetBoardId)
        selectBoard(target)
        _compilationResult.value = null
        _showCompileConsole.value = false
        _currentTab.value = AppTab.EDITOR
        showFeedback("Loaded '${sketch.name}'")
    }

    fun createNewSketch(title: String) {
        val newSketch = Sketch(
            id = "sketch_${System.currentTimeMillis()}",
            name = if (title.isBlank()) "Untitled_Sketch" else title.trim(),
            description = "Custom user sketch",
            targetBoardId = _selectedBoard.value.id,
            code = """// ${title.ifBlank { "New Sketch" }}
void setup() {
  Serial.begin(${_selectedBoard.value.defaultBaudRate});
  Serial.println("${title.ifBlank { "MicroCode" }} initialized");
}

void loop() {
  // Put your main code here, to run repeatedly:
  delay(1000);
}
""".trimIndent()
        )
        val list = _sketches.value.toMutableList()
        list.add(0, newSketch)
        _sketches.value = list
        loadSketch(newSketch)
    }

    fun compileSketch() {
        _isCompiling.value = true
        _showCompileConsole.value = true
        viewModelScope.launch {
            val result = ArduinoCompilerEngine.compile(_codeText.value, _selectedBoard.value)
            _compilationResult.value = result
            _isCompiling.value = false

            if (result.success) {
                showFeedback("Compilation Verified! Flash: ${String.format("%.1f", result.flashPercentage)}%")
            } else {
                showFeedback("Compilation Failed: ${result.issues.count { !it.isWarning }} error(s)")
            }
        }
    }

    fun toggleCompileConsole() {
        _showCompileConsole.value = !_showCompileConsole.value
    }

    // Serial methods
    fun setBaudRate(baud: Int) {
        _serialConfig.value = _serialConfig.value.copy(baudRate = baud)
    }

    fun setLineEnding(ending: LineEnding) {
        _serialConfig.value = _serialConfig.value.copy(lineEnding = ending)
    }

    fun setSerialPortType(type: SerialPortType) {
        _serialConfig.value = _serialConfig.value.copy(portType = type)
    }

    fun setTcpHost(host: String) {
        _otaIp.value = host
        _serialConfig.value = _serialConfig.value.copy(tcpHost = host)
    }

    fun setTcpPort(port: Int) {
        _serialConfig.value = _serialConfig.value.copy(tcpPort = port)
    }

    fun toggleSerialConnection() {
        if (serialManager.isConnected.value) {
            serialManager.disconnect()
        } else {
            serialManager.connect(_serialConfig.value)
        }
    }

    fun updateCommandInput(text: String) {
        _commandInput.value = text
    }

    fun sendCurrentCommand() {
        val text = _commandInput.value
        if (text.isNotBlank()) {
            serialManager.sendMessage(text, _serialConfig.value.lineEnding)
            _commandInput.value = ""
        }
    }

    fun sendMacro(cmd: String) {
        serialManager.sendMessage(cmd, _serialConfig.value.lineEnding)
    }

    fun resetMicrocontroller() {
        serialManager.pulseDtrRts()
    }

    // Flasher / OTA methods
    fun setFlashMode(mode: String) {
        _flashMode.value = mode
    }

    fun setOtaIp(ip: String) {
        _otaIp.value = ip
        _serialConfig.value = _serialConfig.value.copy(tcpHost = ip)
    }

    fun setOtaPort(port: String) {
        _otaPort.value = port
    }

    fun setOtaAuthToken(token: String) {
        _otaAuthToken.value = token
    }

    fun checkRemoteBoard() {
        val port = _otaPort.value.toIntOrNull() ?: 80
        flasherManager.checkRemoteBoard(_otaIp.value, port)
    }

    fun rebootRemoteBoard() {
        val port = _otaPort.value.toIntOrNull() ?: 80
        flasherManager.rebootRemoteBoard(_otaIp.value, port)
    }

    fun startFlashUpload() {
        // Check compilation first
        val comp = _compilationResult.value
        val binaryData = if (comp != null && comp.success && comp.binaryBytes.isNotEmpty()) {
            comp.binaryBytes
        } else {
            // Auto compile first
            val res = ArduinoCompilerEngine.compile(_codeText.value, _selectedBoard.value)
            _compilationResult.value = res
            if (!res.success) {
                _showCompileConsole.value = true
                showFeedback("Cannot upload: Fix compilation errors first!")
                return
            }
            res.binaryBytes
        }

        if (_flashMode.value == "WIFI_OTA") {
            val port = _otaPort.value.toIntOrNull() ?: _selectedBoard.value.defaultOtaPort
            flasherManager.startOtaUpload(
                ip = _otaIp.value,
                port = port,
                authToken = _otaAuthToken.value,
                board = _selectedBoard.value,
                binaryData = binaryData
            )
        } else {
            flasherManager.startUsbFlashing(
                board = _selectedBoard.value,
                binaryData = binaryData
            )
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showFeedback("Copied to clipboard")
    }

    fun shareText(title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooser)
    }

    fun showFeedback(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearFeedback() {
        _snackbarMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        serialManager.disconnect()
    }
}
