package com.example.model

data class Sketch(
    val id: String,
    val name: String,
    val description: String,
    val targetBoardId: String,
    val code: String,
    val isPreset: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

object SketchPresets {
    val BLINK = Sketch(
        id = "preset_blink",
        name = "Blink LED",
        description = "Classic beginner sketch that blinks the onboard LED once per second.",
        targetBoardId = "arduino_uno",
        isPreset = true,
        code = """// MicroCode: Classic LED Blink
// Works on Arduino Uno, Nano, Mega, and ESP32

#if defined(ESP32) || defined(ESP8266)
const int LED_PIN = 2; // Onboard LED for DevKit
#else
const int LED_PIN = 13; // Builtin LED for Arduino Uno/Nano
#endif

void setup() {
  pinMode(LED_PIN, OUTPUT);
  Serial.begin(115200);
  Serial.println("System initialized. Starting LED blink routine...");
}

void loop() {
  digitalWrite(LED_PIN, HIGH);
  Serial.println("LED State: ON");
  delay(1000);
  
  digitalWrite(LED_PIN, LOW);
  Serial.println("LED State: OFF");
  delay(1000);
}
""".trimIndent()
    )

    val ESP32_OTA_SERVER = Sketch(
        id = "preset_esp32_ota",
        name = "ESP32 WiFi & Web OTA",
        description = "Connects to Wi-Fi, hosts a web dashboard, and supports HTTP OTA firmware uploads.",
        targetBoardId = "esp32_wroom",
        isPreset = true,
        code = """// MicroCode: ESP32 Remote Management & Web OTA Update
#include <WiFi.h>
#include <WebServer.h>
#include <Update.h>

const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

WebServer server(80);

void handleRoot() {
  String html = "<html><body>";
  html += "<h2>ESP32 Remote Node Status</h2>";
  html += "<p>Free Heap: " + String(ESP.getFreeHeap()) + " bytes</p>";
  html += "<p>Uptime: " + String(millis() / 1000) + " s</p>";
  html += "<p>WiFi RSSI: " + String(WiFi.RSSI()) + " dBm</p>";
  html += "<h3>Firmware OTA Upload</h3>";
  html += "<form method='POST' action='/update' enctype='multipart/form-data'>";
  html += "<input type='file' name='update'><input type='submit' value='Upload Binary'>";
  html += "</form></body></html>";
  server.send(200, "text/html", html);
}

void handleStatusJson() {
  String json = "{";
  json += "\"uptime_sec\":" + String(millis() / 1000) + ",";
  json += "\"free_heap\":" + String(ESP.getFreeHeap()) + ",";
  json += "\"wifi_rssi\":" + String(WiFi.RSSI()) + ",";
  json += "\"chip_model\":\"" + String(ESP.getChipModel()) + "\"";
  json += "}";
  server.send(200, "application/json", json);
}

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\nConnecting to Wi-Fi...");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi connected!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());

  server.on("/", HTTP_GET, handleRoot);
  server.on("/status", HTTP_GET, handleStatusJson);
  
  // OTA Binary endpoint
  server.on("/update", HTTP_POST, []() {
    server.sendHeader("Connection", "close");
    server.send(200, "text/plain", (Update.hasError()) ? "OTA_FAIL" : "OTA_OK");
    ESP.restart();
  }, []() {
    HTTPUpload& upload = server.upload();
    if (upload.status == UPLOAD_FILE_START) {
      Serial.printf("Update: %s\n", upload.filename.c_str());
      if (!Update.begin(UPDATE_SIZE_UNKNOWN)) {
        Update.printError(Serial);
      }
    } else if (upload.status == UPLOAD_FILE_WRITE) {
      if (Update.write(upload.buf, upload.currentSize) != upload.currentSize) {
        Update.printError(Serial);
      }
    } else if (upload.status == UPLOAD_FILE_END) {
      if (Update.end(true)) {
        Serial.printf("Update Success: %u bytes\nRebooting...\n", upload.totalSize);
      }
    }
  });

  server.begin();
  Serial.println("HTTP Server and OTA endpoint active on port 80");
}

void loop() {
  server.handleClient();
  delay(2);
}
""".trimIndent()
    )

    val SENSOR_PLOTTER = Sketch(
        id = "preset_sensor_plotter",
        name = "Real-Time Sensor Telemetry",
        description = "Streams multi-channel analog sensor and simulated environmental telemetry.",
        targetBoardId = "esp32_wroom",
        isPreset = true,
        code = """// MicroCode: High-Speed Multi-Channel Sensor Telemetry
// Format is automatically graphed in real-time by the Sensor Graph tab!

const int SENSOR_PIN_A = 34; // ADC1 Channel 6
const int SENSOR_PIN_B = 35; // ADC1 Channel 7

float phase = 0.0;

void setup() {
  Serial.begin(115200);
  analogReadResolution(12); // ESP32 12-bit ADC (0-4095)
  Serial.println("# Telemetry Stream Initialized: Temp, Humidity, AnalogCh1, AnalogCh2");
}

void loop() {
  phase += 0.08;
  if (phase > 6.28) phase = 0.0;

  // Synthesize realistic environmental and analog telemetry
  float tempCelsius = 22.5 + (sin(phase) * 3.2);
  float humidityPercent = 55.0 + (cos(phase * 0.7) * 8.0);
  int rawAdc = analogRead(SENSOR_PIN_A);
  float voltage = (rawAdc / 4095.0) * 3.3;

  // Stream in standard CSV format: Channel1, Channel2, Channel3
  Serial.print("temp:");
  Serial.print(tempCelsius, 2);
  Serial.print(",humidity:");
  Serial.print(humidityPercent, 1);
  Serial.print(",voltage:");
  Serial.print(voltage, 3);
  Serial.println();

  delay(50); // 20 Hz sample rate
}
""".trimIndent()
    )

    val SERVO_CONTROLLER = Sketch(
        id = "preset_servo",
        name = "Servo Motor Sweep & PWM",
        description = "Sweeps an SG90/MG996R servo motor back and forth across 180 degrees.",
        targetBoardId = "arduino_uno",
        isPreset = true,
        code = """// MicroCode: Servo Motor Sweep
#include <Servo.h>

Servo myServo;
const int SERVO_PIN = 9;

void setup() {
  Serial.begin(9600);
  myServo.attach(SERVO_PIN);
  Serial.println("Servo Controller Initialized on Pin 9");
}

void loop() {
  // Sweep from 0 to 180 degrees
  for (int pos = 0; pos <= 180; pos += 5) {
    myServo.write(pos);
    Serial.print("Servo Angle: ");
    Serial.println(pos);
    delay(30);
  }

  delay(500);

  // Sweep from 180 back to 0 degrees
  for (int pos = 180; pos >= 0; pos -= 5) {
    myServo.write(pos);
    Serial.print("Servo Angle: ");
    Serial.println(pos);
    delay(30);
  }

  delay(500);
}
""".trimIndent()
    )

    val ULTRASONIC_DISTANCE = Sketch(
        id = "preset_ultrasonic",
        name = "HC-SR04 Ultrasonic Distance",
        description = "Measures distance in centimeters using ultrasound pulses and echoes.",
        targetBoardId = "arduino_uno",
        isPreset = true,
        code = """// MicroCode: HC-SR04 Ultrasonic Distance Meter
const int TRIG_PIN = 11;
const int ECHO_PIN = 12;

void setup() {
  Serial.begin(115200);
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  Serial.println("HC-SR04 Ultrasonic Sensor Ready");
}

void loop() {
  // Clear trigger
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);

  // Send 10 microsecond pulse
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  // Measure echo pulse duration
  long durationUs = pulseIn(ECHO_PIN, HIGH, 30000); // 30ms timeout

  if (durationUs == 0) {
    Serial.println("Distance: Out of range");
  } else {
    // Speed of sound = 343 m/s = 0.0343 cm/us
    float distanceCm = (durationUs * 0.0343) / 2.0;
    
    // Output for Serial Monitor & Graph
    Serial.print("distance_cm:");
    Serial.println(distanceCm, 1);
  }

  delay(100);
}
""".trimIndent()
    )

    val allPresets = listOf(
        ESP32_OTA_SERVER,
        BLINK,
        SENSOR_PLOTTER,
        SERVO_CONTROLLER,
        ULTRASONIC_DISTANCE
    )
}
