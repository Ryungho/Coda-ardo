package com.example.model

enum class BoardFamily {
    ARDUINO_AVR,
    ESP32,
    ESP8266,
    RP2040
}

enum class PinType {
    DIGITAL,
    ANALOG,
    POWER,
    COMMUNICATION,
    SPECIAL
}

data class PinInfo(
    val name: String,
    val type: PinType,
    val description: String,
    val isPwm: Boolean = false,
    val isInterrupt: Boolean = false
)

data class Board(
    val id: String,
    val name: String,
    val family: BoardFamily,
    val mcu: String,
    val clockSpeedMhz: Int,
    val flashBytes: Long,
    val ramBytes: Long,
    val operatingVoltage: String,
    val defaultBaudRate: Int,
    val supportsWifiOta: Boolean,
    val defaultOtaPort: Int = 80,
    val description: String,
    val pins: List<PinInfo>
)

object BoardCatalog {
    val ARDUINO_UNO = Board(
        id = "arduino_uno",
        name = "Arduino Uno R3",
        family = BoardFamily.ARDUINO_AVR,
        mcu = "ATmega328P (8-bit)",
        clockSpeedMhz = 16,
        flashBytes = 32 * 1024L,
        ramBytes = 2 * 1024L,
        operatingVoltage = "5V",
        defaultBaudRate = 9600,
        supportsWifiOta = false,
        description = "Standard 8-bit AVR board with 14 digital I/O pins and 6 analog inputs.",
        pins = listOf(
            PinInfo("D0 / RX", PinType.COMMUNICATION, "UART Receive / Digital 0"),
            PinInfo("D1 / TX", PinType.COMMUNICATION, "UART Transmit / Digital 1"),
            PinInfo("D2", PinType.DIGITAL, "Digital I/O", isInterrupt = true),
            PinInfo("D3", PinType.DIGITAL, "Digital I/O / PWM", isPwm = true, isInterrupt = true),
            PinInfo("D4", PinType.DIGITAL, "Digital I/O"),
            PinInfo("D5", PinType.DIGITAL, "Digital I/O / PWM", isPwm = true),
            PinInfo("D6", PinType.DIGITAL, "Digital I/O / PWM", isPwm = true),
            PinInfo("D7", PinType.DIGITAL, "Digital I/O"),
            PinInfo("D8", PinType.DIGITAL, "Digital I/O"),
            PinInfo("D9", PinType.DIGITAL, "Digital I/O / PWM", isPwm = true),
            PinInfo("D10 / SS", PinType.DIGITAL, "Digital I/O / SPI Slave Select / PWM", isPwm = true),
            PinInfo("D11 / MOSI", PinType.COMMUNICATION, "Digital I/O / SPI MOSI / PWM", isPwm = true),
            PinInfo("D12 / MISO", PinType.COMMUNICATION, "Digital I/O / SPI MISO"),
            PinInfo("D13 / SCK", PinType.COMMUNICATION, "Digital I/O / SPI SCK / Built-in LED"),
            PinInfo("A0", PinType.ANALOG, "Analog Input 0 (10-bit ADC)"),
            PinInfo("A1", PinType.ANALOG, "Analog Input 1 (10-bit ADC)"),
            PinInfo("A2", PinType.ANALOG, "Analog Input 2 (10-bit ADC)"),
            PinInfo("A3", PinType.ANALOG, "Analog Input 3 (10-bit ADC)"),
            PinInfo("A4 / SDA", PinType.COMMUNICATION, "Analog Input 4 / I2C SDA"),
            PinInfo("A5 / SCL", PinType.COMMUNICATION, "Analog Input 5 / I2C SCL")
        )
    )

    val ARDUINO_NANO = Board(
        id = "arduino_nano",
        name = "Arduino Nano",
        family = BoardFamily.ARDUINO_AVR,
        mcu = "ATmega328P (8-bit)",
        clockSpeedMhz = 16,
        flashBytes = 32 * 1024L,
        ramBytes = 2 * 1024L,
        operatingVoltage = "5V",
        defaultBaudRate = 115200,
        supportsWifiOta = false,
        description = "Breadboard-friendly AVR board with Mini-B/Type-C USB and 8 analog inputs.",
        pins = listOf(
            PinInfo("D0 / RX", PinType.COMMUNICATION, "UART RX"),
            PinInfo("D1 / TX", PinType.COMMUNICATION, "UART TX"),
            PinInfo("D2", PinType.DIGITAL, "Digital 2 / INT0", isInterrupt = true),
            PinInfo("D3", PinType.DIGITAL, "Digital 3 / PWM / INT1", isPwm = true, isInterrupt = true),
            PinInfo("D4", PinType.DIGITAL, "Digital 4"),
            PinInfo("D5", PinType.DIGITAL, "Digital 5 / PWM", isPwm = true),
            PinInfo("D6", PinType.DIGITAL, "Digital 6 / PWM", isPwm = true),
            PinInfo("D9", PinType.DIGITAL, "Digital 9 / PWM", isPwm = true),
            PinInfo("D10", PinType.DIGITAL, "Digital 10 / PWM", isPwm = true),
            PinInfo("D11", PinType.DIGITAL, "Digital 11 / PWM / MOSI", isPwm = true),
            PinInfo("D12", PinType.COMMUNICATION, "Digital 12 / MISO"),
            PinInfo("D13", PinType.COMMUNICATION, "Digital 13 / SCK / LED"),
            PinInfo("A0 - A7", PinType.ANALOG, "8x 10-bit ADC Channels (0-1023)")
        )
    )

    val ARDUINO_MEGA = Board(
        id = "arduino_mega",
        name = "Arduino Mega 2560",
        family = BoardFamily.ARDUINO_AVR,
        mcu = "ATmega2560 (8-bit)",
        clockSpeedMhz = 16,
        flashBytes = 256 * 1024L,
        ramBytes = 8 * 1024L,
        operatingVoltage = "5V",
        defaultBaudRate = 9600,
        supportsWifiOta = false,
        description = "High I/O capacity microcontroller with 54 digital pins, 16 analog pins, and 4 hardware UARTs.",
        pins = listOf(
            PinInfo("D0-D13", PinType.DIGITAL, "Standard Digital / 15 PWM pins", isPwm = true),
            PinInfo("D14-D19", PinType.COMMUNICATION, "3 Extra Hardware Serial UARTs"),
            PinInfo("D20-D21", PinType.COMMUNICATION, "Hardware I2C (SDA/SCL)"),
            PinInfo("D50-D53", PinType.COMMUNICATION, "Hardware SPI (MISO, MOSI, SCK, SS)"),
            PinInfo("A0-A15", PinType.ANALOG, "16x 10-bit Analog Inputs")
        )
    )

    val ESP32_WROOM = Board(
        id = "esp32_wroom",
        name = "ESP32 DevKit V1",
        family = BoardFamily.ESP32,
        mcu = "Xtensa Dual-Core 32-bit LX6",
        clockSpeedMhz = 240,
        flashBytes = 4 * 1024 * 1024L,
        ramBytes = 520 * 1024L,
        operatingVoltage = "3.3V",
        defaultBaudRate = 115200,
        supportsWifiOta = true,
        defaultOtaPort = 80,
        description = "Popular IoT powerhouse with 2.4GHz Wi-Fi, Bluetooth 4.2 BLE, ultra-low power coprocessor.",
        pins = listOf(
            PinInfo("GPIO 2", PinType.DIGITAL, "Built-in LED / Boot strapping / ADC2_CH2", isPwm = true),
            PinInfo("GPIO 4", PinType.ANALOG, "ADC2_CH0 / Touch 0 / PWM", isPwm = true),
            PinInfo("GPIO 5", PinType.COMMUNICATION, "VSPI CS0 / PWM", isPwm = true),
            PinInfo("GPIO 12", PinType.ANALOG, "ADC2_CH5 / Touch 5 / HSPI MISO"),
            PinInfo("GPIO 13", PinType.ANALOG, "ADC2_CH4 / Touch 4 / HSPI MOSI"),
            PinInfo("GPIO 14", PinType.ANALOG, "ADC2_CH6 / Touch 6 / HSPI CLK"),
            PinInfo("GPIO 15", PinType.ANALOG, "ADC2_CH3 / Touch 3 / HSPI CS0"),
            PinInfo("GPIO 18", PinType.COMMUNICATION, "VSPI SCK"),
            PinInfo("GPIO 19", PinType.COMMUNICATION, "VSPI MISO"),
            PinInfo("GPIO 21", PinType.COMMUNICATION, "I2C SDA"),
            PinInfo("GPIO 22", PinType.COMMUNICATION, "I2C SCL"),
            PinInfo("GPIO 23", PinType.COMMUNICATION, "VSPI MOSI"),
            PinInfo("GPIO 32", PinType.ANALOG, "ADC1_CH4 / Touch 9 (WiFi Safe)"),
            PinInfo("GPIO 33", PinType.ANALOG, "ADC1_CH5 / Touch 8 (WiFi Safe)"),
            PinInfo("GPIO 34", PinType.ANALOG, "ADC1_CH6 / Input Only"),
            PinInfo("GPIO 35", PinType.ANALOG, "ADC1_CH7 / Input Only"),
            PinInfo("GPIO 36 (VP)", PinType.ANALOG, "ADC1_CH0 / Input Only"),
            PinInfo("GPIO 39 (VN)", PinType.ANALOG, "ADC1_CH3 / Input Only")
        )
    )

    val ESP32_S3 = Board(
        id = "esp32_s3",
        name = "ESP32-S3 DevKit",
        family = BoardFamily.ESP32,
        mcu = "Xtensa Dual-Core 32-bit LX7 (AI Vector)",
        clockSpeedMhz = 240,
        flashBytes = 8 * 1024 * 1024L,
        ramBytes = 512 * 1024L,
        operatingVoltage = "3.3V",
        defaultBaudRate = 115200,
        supportsWifiOta = true,
        defaultOtaPort = 80,
        description = "Advanced ESP32 with vector instructions for on-device machine learning, native USB OTG.",
        pins = listOf(
            PinInfo("GPIO 19 / 20", PinType.COMMUNICATION, "Native USB D- / D+ OTG interface"),
            PinInfo("GPIO 43 / 44", PinType.COMMUNICATION, "UART0 TX / RX"),
            PinInfo("GPIO 1-18", PinType.ANALOG, "ADC1 & ADC2 channels / Touch"),
            PinInfo("GPIO 48", PinType.SPECIAL, "Addressable RGB WS2812 NeoPixel")
        )
    )

    val ESP32_C3 = Board(
        id = "esp32_c3",
        name = "ESP32-C3 RISC-V",
        family = BoardFamily.ESP32,
        mcu = "32-bit RISC-V Single-Core",
        clockSpeedMhz = 160,
        flashBytes = 4 * 1024 * 1024L,
        ramBytes = 400 * 1024L,
        operatingVoltage = "3.3V",
        defaultBaudRate = 115200,
        supportsWifiOta = true,
        defaultOtaPort = 80,
        description = "Cost-effective RISC-V IoT microcontroller with Wi-Fi 4 and Bluetooth 5 (LE).",
        pins = listOf(
            PinInfo("GPIO 8", PinType.DIGITAL, "Built-in Status LED"),
            PinInfo("GPIO 0-5", PinType.ANALOG, "ADC1 Channels 0-5"),
            PinInfo("GPIO 18 / 19", PinType.COMMUNICATION, "USB Serial / JTAG"),
            PinInfo("GPIO 20 / 21", PinType.COMMUNICATION, "UART RX / TX")
        )
    )

    val ESP8266_NODEMCU = Board(
        id = "esp8266_nodemcu",
        name = "ESP8266 NodeMCU",
        family = BoardFamily.ESP8266,
        mcu = "Tensilica L106 32-bit",
        clockSpeedMhz = 80,
        flashBytes = 4 * 1024 * 1024L,
        ramBytes = 80 * 1024L,
        operatingVoltage = "3.3V",
        defaultBaudRate = 115200,
        supportsWifiOta = true,
        defaultOtaPort = 8266,
        description = "Pioneer Wi-Fi development board, 1 analog pin (A0, 1.0V max), compact form factor.",
        pins = listOf(
            PinInfo("D0 / GPIO 16", PinType.DIGITAL, "Wake from deep sleep"),
            PinInfo("D1 / GPIO 5", PinType.COMMUNICATION, "I2C SCL"),
            PinInfo("D2 / GPIO 4", PinType.COMMUNICATION, "I2C SDA"),
            PinInfo("D4 / GPIO 2", PinType.DIGITAL, "Built-in LED / Boot fail if LOW"),
            PinInfo("A0 / ADC0", PinType.ANALOG, "Single 10-bit ADC channel (0-1.0V max)")
        )
    )

    val RPI_PICO = Board(
        id = "rpi_pico",
        name = "Raspberry Pi Pico (RP2040)",
        family = BoardFamily.RP2040,
        mcu = "Dual ARM Cortex-M0+",
        clockSpeedMhz = 133,
        flashBytes = 2 * 1024 * 1024L,
        ramBytes = 264 * 1024L,
        operatingVoltage = "3.3V",
        defaultBaudRate = 115200,
        supportsWifiOta = false,
        description = "Dual-core ARM microcontroller with revolutionary Programmable I/O (PIO) state machines.",
        pins = listOf(
            PinInfo("GP25", PinType.DIGITAL, "Built-in Green LED"),
            PinInfo("GP0 / GP1", PinType.COMMUNICATION, "UART0 TX / RX"),
            PinInfo("GP26 - GP28", PinType.ANALOG, "3x 12-bit ADC Channels")
        )
    )

    val allBoards = listOf(
        ARDUINO_UNO,
        ARDUINO_NANO,
        ARDUINO_MEGA,
        ESP32_WROOM,
        ESP32_S3,
        ESP32_C3,
        ESP8266_NODEMCU,
        RPI_PICO
    )

    fun getBoardById(id: String): Board {
        return allBoards.find { it.id == id } ?: ESP32_WROOM
    }
}
