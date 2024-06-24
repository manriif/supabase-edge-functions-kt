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

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

internal abstract class ServeBaseOutputStream() : OutputStream() {

    private val buffer = ByteArrayOutputStream()
    private var closed: Boolean = false

    override fun close() {
        closed = true
        flushLine()
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (closed) {
            throw IOException("The stream has been closed.")
        }

        var i = off
        var last = off

        fun bytesToAppend() = i - last

        val end = off + len

        fun append(len: Int = bytesToAppend()) {
            buffer.write(b, last, i - last)
            last += len
        }

        while (i < end) {
            val c = b[i++]

            if (c == '\n'.code.toByte()) {
                append()
                flushLine()
            }
        }

        append()
    }

    override fun write(b: Int) {
        write(byteArrayOf(b.toByte()), 0, 1)
    }

    private fun flushLine() {
        if (buffer.size() > 0) {
            val text = buffer.toString(Charsets.UTF_8)
            process(text)
            buffer.reset()
        }
    }

    protected abstract fun process(rawLine: String)
}