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

import io.github.manriif.supabase.functions.request.RequestClient
import org.gradle.api.logging.Logger
import org.gradle.process.internal.ExecHandle

internal class ServeInitOutputStream(
    private val requestSender: RequestClient?,
    private val logger: Logger,
) : ServeBaseOutputStream() {

    private var execHandle: ExecHandle? = null
    private var messageCount = 0

    /**
     * Sets the handle to abort after requests are sent.
     */
    fun setHandle(handle: ExecHandle) {
        execHandle = handle
    }

    override fun process(rawLine: String) {
        if (++messageCount > 1) {
            // Send initial request after supabase-edge-runtime is ready
            requestSender?.sendRequestsAsync {
                execHandle?.abort()
            }
        }

        logger.lifecycle(rawLine.removeSuffix("\n"))
    }
}