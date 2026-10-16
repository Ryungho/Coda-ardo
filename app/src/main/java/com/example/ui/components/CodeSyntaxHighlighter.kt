package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.CodeComment
import com.example.ui.theme.CodeFunction
import com.example.ui.theme.CodeKeyword
import com.example.ui.theme.CodeNumber
import com.example.ui.theme.CodeString
import com.example.ui.theme.CodeType

object CodeSyntaxHighlighter {

    private val KEYWORDS = setOf(
        "void", "int", "float", "double", "char", "bool", "boolean", "long", "short",
        "unsigned", "signed", "const", "static", "volatile", "if", "else", "for", "while",
        "do", "switch", "case", "break", "continue", "return", "true", "false", "HIGH",
        "LOW", "INPUT", "OUTPUT", "INPUT_PULLUP", "class", "struct", "public", "private"
    )

    private val ARDUINO_BUILTINS = setOf(
        "setup", "loop", "pinMode", "digitalWrite", "digitalRead", "analogRead", "analogWrite",
        "delay", "delayMicroseconds", "millis", "micros", "pulseIn", "Serial", "println",
        "print", "begin", "available", "read", "write", "WiFi", "server", "Update", "ESP",
        "analogReadResolution", "attach", "getFreeHeap", "localIP", "status", "restart"
    )

    fun highlightCode(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Highlighting comments: //...
            val singleLineComments = "//.*".toRegex()
            for (match in singleLineComments.findAll(code)) {
                addStyle(SpanStyle(color = CodeComment), match.range.first, match.range.last + 1)
            }

            // Highlighting block comments: /* ... */
            val blockComments = "/\\*[\\s\\S]*?\\*/".toRegex()
            for (match in blockComments.findAll(code)) {
                addStyle(SpanStyle(color = CodeComment), match.range.first, match.range.last + 1)
            }

            // Highlighting preprocessor directives: #include, #define, #if, etc.
            val preprocessors = "#[a-zA-Z_]+".toRegex()
            for (match in preprocessors.findAll(code)) {
                addStyle(
                    SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold),
                    match.range.first, match.range.last + 1
                )
            }

            // Highlighting string literals: "..."
            val strings = "\"(\\\\.|[^\"])*\"".toRegex()
            for (match in strings.findAll(code)) {
                addStyle(SpanStyle(color = CodeString), match.range.first, match.range.last + 1)
            }

            // Highlighting numbers: \b\d+\b
            val numbers = "\\b\\d+(\\.\\d+)?f?\\b".toRegex()
            for (match in numbers.findAll(code)) {
                addStyle(SpanStyle(color = CodeNumber), match.range.first, match.range.last + 1)
            }

            // Highlighting words
            val words = "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b".toRegex()
            for (match in words.findAll(code)) {
                val word = match.value
                when {
                    KEYWORDS.contains(word) -> {
                        addStyle(
                            SpanStyle(color = CodeKeyword, fontWeight = FontWeight.SemiBold),
                            match.range.first, match.range.last + 1
                        )
                    }
                    ARDUINO_BUILTINS.contains(word) -> {
                        addStyle(
                            SpanStyle(color = CodeFunction, fontWeight = FontWeight.Medium),
                            match.range.first, match.range.last + 1
                        )
                    }
                }
            }
        }
    }
}
