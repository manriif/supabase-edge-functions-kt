/**
 * Copyright (c) 2024 Maanrifa Bacar Ali
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.manriif.supabase.functions.serve.stream

import io.github.manriif.supabase.functions.serve.stacktrace.StackTraceProcessor
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger

internal class ServeMainOutputStream(
    private val stackTraceProcessor: StackTraceProcessor,
    private val logger: Logger,
) : ServeBaseOutputStream() {

    private var logLevel = LogLevel.LIFECYCLE

    var lastMessage: String? = null
        private set

    override fun process(rawLine: String) {
        val line = detectLogLevel(rawLine)

        if (line.trim().startsWith("at ")) {
            logger.log(logLevel, stackTraceProcessor.resolveStackTrace(line))
        } else {
            val fixedLine = preventGradleErrorInterpretation(line)
            logger.log(logLevel, fixedLine)
        }

        lastMessage = rawLine
    }

    private fun detectLogLevel(text: String): String {
        if (!text.startsWith('[')) {
            return text.substringBefore('\n')
        }

        val levelEnd = text.indexOf(']')

        if (levelEnd <= 1) {
            return text.substringBefore('\n')
        }

        logLevel = when (text.substring(1, levelEnd)) {
            "Info" -> LogLevel.LIFECYCLE
            "Error" -> LogLevel.ERROR
            else -> LogLevel.LIFECYCLE
        }

        return try {
            text.substring(startIndex = levelEnd + 2, endIndex = text.indexOf('\n'))
        } catch (throwable: Throwable) {
            text.substringBefore('\n')
        }
    }

    private fun preventGradleErrorInterpretation(line: String): String {
        if (line.startsWith("Error")) {
            logLevel = LogLevel.ERROR
            return line.replaceRange(0, 5, "\nServeRequestError")
        }

        return line
    }
}