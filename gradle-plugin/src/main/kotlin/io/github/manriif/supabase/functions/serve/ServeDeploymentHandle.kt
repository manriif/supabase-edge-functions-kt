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