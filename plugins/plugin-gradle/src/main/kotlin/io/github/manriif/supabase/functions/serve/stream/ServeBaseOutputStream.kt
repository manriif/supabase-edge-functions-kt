package io.github.manriif.supabase.functions.serve.stream

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

internal abstract class ServeBaseOutputStream() : OutputStream() {

    private val buffer = ByteArrayOutputStream()

    protected var closed: Boolean = false
        private set

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