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

import io.github.manriif.supabase.functions.PARAMETER_AUTO_REQUEST
import io.github.manriif.supabase.functions.PARAMETER_INSPECT
import io.github.manriif.supabase.functions.PARAMETER_LOG_RESPONSE
import io.github.manriif.supabase.functions.PARAMETER_LOG_STATUS
import io.github.manriif.supabase.functions.PARAMETER_REQUEST_DELAY
import io.github.manriif.supabase.functions.PARAMETER_SERVE_DEBUG
import io.github.manriif.supabase.functions.request.RequestClient
import io.github.manriif.supabase.functions.request.RequestClientOptions
import io.github.manriif.supabase.functions.serve.stacktrace.StackTraceProcessor
import io.github.manriif.supabase.functions.serve.stacktrace.StackTraceSourceMapStrategy
import io.github.manriif.supabase.functions.serve.stream.ServeInitOutputStream
import io.github.manriif.supabase.functions.serve.stream.ServeMainOutputStream
import io.github.manriif.supabase.functions.supabase.envFile
import io.github.manriif.supabase.functions.supabase.importMap
import io.github.manriif.supabase.functions.supabase.inspect
import io.github.manriif.supabase.functions.supabase.noVerifyJwt
import io.github.manriif.supabase.functions.supabase.supabaseCommand
import io.github.manriif.supabase.functions.util.getBooleanOrDefault
import io.github.manriif.supabase.functions.util.getLongOrDefault
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Property
import org.gradle.internal.service.ServiceRegistry
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.internal.ExecActionFactory
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.internal.operation

internal class ServeRunner(
    private val execHandleFactory: ExecHandleFactory,
    private val execActionFactory: ExecActionFactory,
    private val properties: Map<String, String>,
    private val logger: Logger,
    private val compiledSourceDir: DirectoryProperty,
    private val rootProjectDir: DirectoryProperty,
    private val projectDir: DirectoryProperty,
    private val supabaseDir: DirectoryProperty,
    private val functionName: Property<String>,
    private val verifyJwt: Property<Boolean>,
    private val deployImportMap: Property<Boolean>,
    private val envFile: RegularFileProperty,
    private val sourceMapStrategy: Property<StackTraceSourceMapStrategy>,
    private val inspect: ServeInspect,
    private val autoRequest: ServeAutoRequest,
    private val requestOutputDir: DirectoryProperty,
    private val requestConfigFile: RegularFileProperty
) {

    ///////////////////////////////////////////////////////////////////////////
    // Command
    ///////////////////////////////////////////////////////////////////////////

    fun execute(serviceRegistry: ServiceRegistry): RunContext<ExecResult> {
        val description = "supabase functions serve"
        val exec = execHandleFactory.newExec()

        return serviceRegistry.operation(description) {
            progress(description)

            createRunContext { initOutputStream, mainOutputStream ->
                exec.configure(initOutputStream, mainOutputStream)
                val handle = exec.build()
                initOutputStream.setHandle(handle)
                handle.start().waitForFinish()
            }
        }
    }

    fun start(): RunContext<ExecHandle> {
        return createRunContext { initOutputStream, mainOutputStream ->
            val builder = execHandleFactory.newExec()
            builder.configure(initOutputStream, mainOutputStream)
            builder.build().start()
        }
    }

    private fun ExecSpec.configure(
        initOutputStream: ServeInitOutputStream,
        mainOutputStream: ServeMainOutputStream
    ) {
        workingDir = rootProjectDir.get().asFile
        standardOutput = initOutputStream
        errorOutput = mainOutputStream
        isIgnoreExitValue = true
        executable = supabaseCommand()

        args("functions", "serve")

        if (properties.getBooleanOrDefault(PARAMETER_INSPECT, false)) {
            inspect(inspect)
        } else if (properties.getBooleanOrDefault(PARAMETER_SERVE_DEBUG, false)) {
            args("--debug")
        }

        envFile(envFile)
        noVerifyJwt(verifyJwt)
        importMap(supabaseDir, deployImportMap)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Context
    ///////////////////////////////////////////////////////////////////////////

    private fun createRequestClient(): RequestClient {
        val options = RequestClientOptions(
            logStatus = properties.getBooleanOrDefault(
                key = PARAMETER_LOG_STATUS,
                default = autoRequest.logStatus
            ),
            logResponse = properties.getBooleanOrDefault(
                key = PARAMETER_LOG_RESPONSE,
                default = autoRequest.logResponse
            ),
            requestDelay = properties.getLongOrDefault(
                key = PARAMETER_REQUEST_DELAY,
                default = autoRequest.sendRequestOnCodeChangeDelay
            )
        )

        val requestClient = RequestClient(
            rootProjectDir = rootProjectDir,
            projectDir = projectDir,
            requestOutputDir = requestOutputDir,
            requestConfigFile = requestConfigFile,
            functionName = functionName,
            logger = logger,
            options = options
        )

        requestClient.initialize(execActionFactory)
        return requestClient
    }

    private fun <T> createRunContext(
        target: (ServeInitOutputStream, ServeMainOutputStream) -> T
    ): RunContext<T> {
        val requestEnabled = properties.getBooleanOrDefault(PARAMETER_AUTO_REQUEST, false)
        val requestClient = if (requestEnabled) createRequestClient() else null

        val stackTraceProcessor = StackTraceProcessor(
            supabaseDirPath = supabaseDir.get().asFile.canonicalPath,
            compiledSourceDir = compiledSourceDir,
            sourceMapStrategy = sourceMapStrategy.get(),
            logger = logger
        )

        val initOutputStream = ServeInitOutputStream(
            requestSender = requestClient,
            logger = logger
        )

        val mainOutputStream = ServeMainOutputStream(
            stackTraceProcessor = stackTraceProcessor,
            logger = logger
        )

        return RunContext(
            target = target(initOutputStream, mainOutputStream),
            stackTraceProcessor = stackTraceProcessor,
            requestSender = requestClient,
            mainOutputStream = mainOutputStream
        )
    }

    class RunContext<T>(
        val target: T,
        private val stackTraceProcessor: StackTraceProcessor,
        private val mainOutputStream: ServeMainOutputStream,
        private val requestSender: RequestClient?
    ) {

        val lastMessage: String?
            get() = mainOutputStream.lastMessage

        fun onBuildRefreshed() {
            stackTraceProcessor.reloadSourceMaps()


            requestSender?.run {
                reloadConfigFile()
                sendRequestsAsync()
            }
        }
    }
}