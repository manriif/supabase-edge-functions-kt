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