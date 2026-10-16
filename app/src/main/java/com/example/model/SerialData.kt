package com.example.model

enum class SerialDirection {
    RX, // Received from microcontroller
    TX, // Transmitted to microcontroller
    SYSTEM // Local connection or system log
}

data class SerialMessage(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val direction: SerialDirection,
    val text: String,
    val hexDump: String = ""
)

enum class SerialPortType {
    USB_OTG,
    WIFI_TCP
}

data class SerialConfig(
    val portType: SerialPortType = SerialPortType.USB_OTG,
    val baudRate: Int = 115200,
    val lineEnding: LineEnding = LineEnding.CR_LF,
    val tcpHost: String = "192.168.4.1",
    val tcpPort: Int = 23,
    val autoScroll: Boolean = true,
    val showTimestamps: Boolean = true
)

enum class LineEnding(val label: String, val sequence: String) {
    NONE("No Line Ending", ""),
    NEWLINE("New Line (\\n)", "\n"),
    CARRIAGE_RETURN("Carriage Return (\\r)", "\r"),
    CR_LF("Both NL & CR (\\r\\n)", "\r\n")
}
