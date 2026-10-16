package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.compiler.ArduinoCompilerEngine
import com.example.model.BoardCatalog
import com.example.model.SketchPresets
import com.example.sensor.SensorTelemetryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MicroCode", appName)
  }

  @Test
  fun `compiler compiles blink sketch successfully`() {
    val result = ArduinoCompilerEngine.compile(SketchPresets.BLINK.code, BoardCatalog.ARDUINO_UNO)
    if (!result.success) {
      System.err.println("BLINK ISSUES: " + result.issues)
    }
    assertTrue("Blink sketch should compile with success: ${result.issues}", result.success)
    assertTrue("Flash used should be greater than 0", result.flashUsedBytes > 0)
    assertTrue("RAM used should be greater than 0", result.ramUsedBytes > 0)
    assertNotNull(result.hexOutput)
  }

  @Test
  fun `compiler compiles esp32 ota sketch successfully`() {
    val result = ArduinoCompilerEngine.compile(SketchPresets.ESP32_OTA_SERVER.code, BoardCatalog.ESP32_WROOM)
    if (!result.success) {
      System.err.println("OTA ISSUES: " + result.issues)
    }
    assertTrue("ESP32 OTA sketch should compile with success: ${result.issues}", result.success)
    assertTrue("Binary bytes should be generated for flashing", result.binaryBytes.isNotEmpty())
  }

  @Test
  fun `sensor telemetry manager parses csv and key value streams`() {
    val manager = SensorTelemetryManager()
    manager.parseLine("temp:25.4,humidity:62.1,voltage:3.28")
    val stats = manager.stats.value
    assertNotNull(stats)
  }
}
