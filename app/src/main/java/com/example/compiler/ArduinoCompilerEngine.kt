package com.example.compiler

import com.example.model.Board
import com.example.model.BoardFamily
import com.example.model.CompilationIssue
import com.example.model.CompilationResult
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object ArduinoCompilerEngine {

    fun compile(code: String, board: Board): CompilationResult {
        val issues = mutableListOf<CompilationIssue>()
        val logs = mutableListOf<String>()

        val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        logs.add("[$timeStr] MicroCode Compiler Toolchain v2.4.1")
        logs.add("Target Board: ${board.name} (${board.mcu}) @ ${board.clockSpeedMhz}MHz")
        logs.add("Detecting architecture profile: ${board.family.name}...")

        val lines = code.lines()

        // 1. Bracket & Brace matching with line coordinates
        val braceStack = mutableListOf<Pair<Char, Pair<Int, Int>>>()
        var insideBlockComment = false
        var hasSetup = false
        var hasLoop = false
        var includesWifi = false
        var includesServo = false
        var includesOta = false

        for ((lineIdx, line) in lines.withIndex()) {
            val lineNum = lineIdx + 1
            val trimmed = line.trim()

            // Handle block comments
            if (trimmed.startsWith("/*")) insideBlockComment = true
            if (insideBlockComment) {
                if (trimmed.contains("*/")) {
                    insideBlockComment = false
                }
                continue
            }
            if (trimmed.startsWith("//") || trimmed.isEmpty()) {
                continue
            }

            // Detect includes
            if (trimmed.startsWith("#include")) {
                if (trimmed.contains("WiFi", ignoreCase = true)) includesWifi = true
                if (trimmed.contains("Servo", ignoreCase = true)) includesServo = true
                if (trimmed.contains("Update", ignoreCase = true) || trimmed.contains("OTA", ignoreCase = true)) includesOta = true

                // Check include syntax
                if (!trimmed.contains("<") && !trimmed.contains("\"")) {
                    issues.add(CompilationIssue(lineNum, 1, "Malformed #include directive: missing '<...>' or '\"...\"'"))
                }
            }

            // Detect setup() and loop()
            val simplifiedLine = line.replace("\\s+".toRegex(), " ")
            if (simplifiedLine.contains("void setup(") || simplifiedLine.contains("void setup (")) {
                hasSetup = true
            }
            if (simplifiedLine.contains("void loop(") || simplifiedLine.contains("void loop (")) {
                hasLoop = true
            }

            // Check casing typos
            checkIdentifierCasing(line, lineNum, issues)

            // Missing semicolon detection heuristic
            checkMissingSemicolon(trimmed, lineNum, issues)

            // Delimiter checking (ignoring content in string literals)
            var inString = false
            var inChar = false
            for (colIdx in line.indices) {
                val c = line[colIdx]
                val prev = if (colIdx > 0) line[colIdx - 1] else ' '

                if (c == '"' && prev != '\\') {
                    inString = !inString
                    continue
                }
                if (c == '\'' && prev != '\\') {
                    inChar = !inChar
                    continue
                }
                if (inString || inChar) continue

                // Check single line comment
                if (c == '/' && colIdx + 1 < line.length && line[colIdx + 1] == '/') {
                    break
                }

                when (c) {
                    '{', '(', '[' -> braceStack.add(Pair(c, Pair(lineNum, colIdx + 1)))
                    '}' -> {
                        if (braceStack.isEmpty() || braceStack.last().first != '{') {
                            issues.add(CompilationIssue(lineNum, colIdx + 1, "Extraneous or unmatched '}'"))
                        } else {
                            braceStack.removeAt(braceStack.lastIndex)
                        }
                    }
                    ')' -> {
                        if (braceStack.isEmpty() || braceStack.last().first != '(') {
                            issues.add(CompilationIssue(lineNum, colIdx + 1, "Extraneous or unmatched ')'"))
                        } else {
                            braceStack.removeAt(braceStack.lastIndex)
                        }
                    }
                    ']' -> {
                        if (braceStack.isEmpty() || braceStack.last().first != '[') {
                            issues.add(CompilationIssue(lineNum, colIdx + 1, "Extraneous or unmatched ']'"))
                        } else {
                            braceStack.removeAt(braceStack.lastIndex)
                        }
                    }
                }
            }

            if (inString) {
                issues.add(CompilationIssue(lineNum, line.length, "Unterminated string literal"))
            }
        }

        // Remaining unclosed braces
        for (unmatched in braceStack) {
            val typeName = when (unmatched.first) {
                '{' -> "curly brace '{'"
                '(' -> "parenthesis '('"
                '[' -> "bracket '['"
                else -> "'${unmatched.first}'"
            }
            issues.add(
                CompilationIssue(
                    unmatched.second.first,
                    unmatched.second.second,
                    "Unclosed $typeName opened here"
                )
            )
        }

        // Verify setup() and loop() entrypoints
        if (!hasSetup) {
            issues.add(CompilationIssue(1, 1, "Undefined reference: 'void setup()' function is required in Arduino/ESP32 sketches."))
        }
        if (!hasLoop) {
            issues.add(CompilationIssue(1, 1, "Undefined reference: 'void loop()' function is required in Arduino/ESP32 sketches."))
        }

        // Check if Wi-Fi / OTA features are used on non-WiFi boards
        if ((includesWifi || includesOta) && !board.supportsWifiOta) {
            issues.add(
                CompilationIssue(
                    1, 1,
                    "Target board '${board.name}' does not have integrated Wi-Fi hardware. Change target board to ESP32 or ESP8266.",
                    isWarning = false
                )
            )
        }

        val hasFatalErrors = issues.any { !it.isWarning }

        // Compute Memory Allocation Estimates
        val (flashUsed, ramUsed) = calculateMemoryFootprint(code, board, includesWifi, includesServo)

        if (flashUsed > board.flashBytes) {
            issues.add(
                CompilationIssue(
                    1, 1,
                    "Sketch too big: Flash used (${flashUsed / 1024} KB) exceeds maximum capacity (${board.flashBytes / 1024} KB) of ${board.name}!"
                )
            )
        }

        val success = issues.none { !it.isWarning }

        // Generate logs
        if (success) {
            logs.add("Compiling sketch core wiring and runtime libraries...")
            logs.add("Preprocessing macros, inline functions, and type definitions...")
            logs.add("Linking object files with GNU ld...")
            logs.add("Optimization level: -Os (Size optimization enabled)")
            logs.add("Archiving built-in library headers...")
            logs.add("--------------------------------------------------")
            logs.add("Sketch uses $flashUsed bytes (${String.format(Locale.US, "%.1f", (flashUsed.toFloat() / board.flashBytes) * 100)}%) of program storage space. Maximum is ${board.flashBytes} bytes.")
            logs.add("Global variables use $ramUsed bytes (${String.format(Locale.US, "%.1f", (ramUsed.toFloat() / board.ramBytes) * 100)}%) of dynamic memory, leaving ${board.ramBytes - ramUsed} bytes for local variables. Maximum is ${board.ramBytes} bytes.")
            logs.add("--------------------------------------------------")
            logs.add("SUCCESS: Binary firmware image generated successfully.")
        } else {
            logs.add("Compilation halted with ${issues.count { !it.isWarning }} error(s) and ${issues.count { it.isWarning }} warning(s).")
            for (issue in issues) {
                val prefix = if (issue.isWarning) "WARNING" else "ERROR"
                logs.add("[$prefix] Line ${issue.line}, Col ${issue.column}: ${issue.message}")
            }
        }

        val (hex, binary) = if (success) {
            generateFirmwarePayload(flashUsed.toInt(), board)
        } else {
            Pair("", ByteArray(0))
        }

        return CompilationResult(
            success = success,
            targetBoard = board,
            issues = issues,
            flashUsedBytes = flashUsed,
            flashTotalBytes = board.flashBytes,
            ramUsedBytes = ramUsed,
            ramTotalBytes = board.ramBytes,
            hexOutput = hex,
            binaryBytes = binary,
            buildLogs = logs
        )
    }

    private fun checkIdentifierCasing(line: String, lineNum: Int, issues: MutableList<CompilationIssue>) {
        val typos = mapOf(
            "\\bserial\\.print" to "Did you mean 'Serial.print'? Identifier is case-sensitive.",
            "\\bserial\\.begin" to "Did you mean 'Serial.begin'? Identifier is case-sensitive.",
            "\\bdigitalwrite\\b" to "Did you mean 'digitalWrite'? CamelCase required.",
            "\\bdigitalread\\b" to "Did you mean 'digitalRead'? CamelCase required.",
            "\\bpinmode\\b" to "Did you mean 'pinMode'? CamelCase required.",
            "\\banalogread\\b" to "Did you mean 'analogRead'? CamelCase required.",
            "\\banalogwrite\\b" to "Did you mean 'analogWrite'? CamelCase required.",
            "\\bdelaymicroseconds\\b" to "Did you mean 'delayMicroseconds'?"
        )

        for ((pattern, message) in typos) {
            // Flag only when the incorrect lowercase pattern literally appears in the code
            if (line.contains(pattern.toRegex())) {
                issues.add(CompilationIssue(lineNum, 1, message, isWarning = false))
            }
        }
    }

    private fun checkMissingSemicolon(trimmed: String, lineNum: Int, issues: MutableList<CompilationIssue>) {
        val codePart = if (trimmed.contains("//")) trimmed.substringBefore("//").trim() else trimmed

        if (codePart.isEmpty() ||
            codePart.startsWith("#") ||
            codePart.endsWith("{") ||
            codePart.endsWith("}") ||
            codePart.endsWith(":") ||
            codePart.endsWith(";") ||
            codePart.endsWith(",") ||
            codePart.startsWith("if ") ||
            codePart.startsWith("if(") ||
            codePart.startsWith("for ") ||
            codePart.startsWith("for(") ||
            codePart.startsWith("while ") ||
            codePart.startsWith("while(") ||
            codePart.startsWith("else") ||
            codePart.startsWith("void ") ||
            (codePart.startsWith("int ") && codePart.endsWith(")"))
        ) {
            return
        }

        // Likely statements requiring semicolon
        if (codePart.contains("=") ||
            codePart.startsWith("Serial.") ||
            codePart.startsWith("digitalWrite") ||
            codePart.startsWith("analogWrite") ||
            codePart.startsWith("pinMode") ||
            codePart.startsWith("delay") ||
            codePart.startsWith("return")
        ) {
            issues.add(CompilationIssue(lineNum, codePart.length + 1, "Missing ';' at end of statement"))
        }
    }

    private fun calculateMemoryFootprint(code: String, board: Board, hasWifi: Boolean, hasServo: Boolean): Pair<Long, Long> {
        val codeLength = code.length.toLong()
        val stringLiteralBytes = "\"([^\"]*)\"".toRegex().findAll(code).sumOf { it.value.length.toLong() }

        var baseFlash = when (board.family) {
            BoardFamily.ARDUINO_AVR -> 924L + (codeLength * 2)
            BoardFamily.ESP32 -> if (hasWifi) 245_760L + (codeLength * 4) else 131_072L + (codeLength * 3)
            BoardFamily.ESP8266 -> if (hasWifi) 220_160L + (codeLength * 3) else 98_304L + (codeLength * 2)
            BoardFamily.RP2040 -> 65_536L + (codeLength * 3)
        }

        if (hasServo) baseFlash += 1840L
        baseFlash += stringLiteralBytes * 2

        var baseRam = when (board.family) {
            BoardFamily.ARDUINO_AVR -> 180L + (codeLength / 8) + stringLiteralBytes
            BoardFamily.ESP32 -> if (hasWifi) 28_672L + (codeLength / 2) else 14_336L + (codeLength / 4)
            BoardFamily.ESP8266 -> if (hasWifi) 24_576L + (codeLength / 3) else 8_192L + (codeLength / 4)
            BoardFamily.RP2040 -> 12_288L + (codeLength / 4)
        }

        // Keep realistic minimums
        baseFlash = baseFlash.coerceAtLeast(444L).coerceAtMost(board.flashBytes)
        baseRam = baseRam.coerceAtLeast(40L).coerceAtMost(board.ramBytes)

        return Pair(baseFlash, baseRam)
    }

    private fun generateFirmwarePayload(flashBytes: Int, board: Board): Pair<String, ByteArray> {
        val hexBuilder = StringBuilder()
        val binaryStream = ByteArrayOutputStream()

        // Generate synthetic Intel HEX records
        // Intel HEX Format: :[count:1][addr:2][type:1][data...][chk:1]
        val rng = Random(42)

        // Magic header for binary
        if (board.family == BoardFamily.ESP32) {
            // ESP32 binary image magic header 0xE9, 4 segments
            binaryStream.write(0xE9)
            binaryStream.write(0x04)
            binaryStream.write(0x02)
            binaryStream.write(0x20)
        }

        val totalRecords = (flashBytes / 16).coerceIn(12, 128)
        var address = 0

        for (i in 0 until totalRecords) {
            val recordBytes = ByteArray(16) { (rng.nextInt(256)).toByte() }
            binaryStream.write(recordBytes)

            val countHex = "%02X".format(16)
            val addrHex = "%04X".format(address)
            val typeHex = "00" // Data record

            val dataHex = recordBytes.joinToString("") { "%02X".format(it) }
            val checksum = calculateChecksum(16, address, 0, recordBytes)

            hexBuilder.append(":$countHex$addrHex$typeHex$dataHex$checksum\n")
            address += 16
        }

        // End of File record
        hexBuilder.append(":00000001FF\n")

        return Pair(hexBuilder.toString(), binaryStream.toByteArray())
    }

    private fun calculateChecksum(count: Int, address: Int, type: Int, data: ByteArray): String {
        var sum = count + (address shr 8) + (address and 0xFF) + type
        for (b in data) {
            sum += (b.toInt() and 0xFF)
        }
        val chk = ((sum.inv() + 1) and 0xFF)
        return "%02X".format(chk)
    }
}
