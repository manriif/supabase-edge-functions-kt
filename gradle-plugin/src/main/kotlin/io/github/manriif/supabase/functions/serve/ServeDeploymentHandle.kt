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
package io.github.manriif.supabase.functions.serve

import org.gradle.api.logging.Logger
import org.gradle.deployment.internal.Deployment
import org.gradle.deployment.internal.DeploymentHandle
import org.gradle.initialization.BuildCancellationToken
import org.gradle.process.ExecResult
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleListener
import javax.inject.Inject

internal open class ServeDeploymentHandle @Inject constructor(
    private val runner: ServeRunner,
    private val logger: Logger,
    private val buildCancellationToken: BuildCancellationToken
) : DeploymentHandle {

    private var runContext: ServeRunner.RunContext<ExecHandle>? = null

    override fun isRunning() = runContext != null

    override fun start(deployment: Deployment) {
        runContext = runner.start().also { context ->
            context.target.addListener(Listener())
        }
    }

    override fun stop() {
        runContext?.target?.abort()?.also {
            runContext = null
        }
    }

    fun notifyBuildReloaded() {
        runContext?.onBuildRefreshed()
    }

    private inner class Listener : ExecHandleListener {

        override fun beforeExecutionStarted(execHandle: ExecHandle?) = Unit

        override fun executionStarted(execHandle: ExecHandle?) = Unit

        override fun executionFinished(execHandle: ExecHandle, execResult: ExecResult) {
            val reason = getServeCommandFailedReason(
                result = execResult.exitValue,
                message = runContext?.lastMessage
            )

            reason?.let {
                logger.error("\n${reason.removeSuffix("\n")}")
            }

            buildCancellationToken.cancel()
        }
    }

}