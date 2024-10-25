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
package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.error.SupabaseFunctionServeException
import io.github.manriif.supabase.functions.serve.ServeAutoRequest
import io.github.manriif.supabase.functions.serve.ServeDeploymentHandle
import io.github.manriif.supabase.functions.serve.ServeInspect
import io.github.manriif.supabase.functions.serve.ServeRunner
import io.github.manriif.supabase.functions.serve.getServeCommandFailedReason
import io.github.manriif.supabase.functions.serve.stacktrace.StackTraceSourceMapStrategy
import org.gradle.StartParameter
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.initialization.BuildCancellationToken
import org.gradle.kotlin.dsl.support.get
import org.gradle.process.internal.ExecHandleFactory
import javax.inject.Inject

private const val HANDLE_NAME = "supabaseServe"

/**
 * Task responsible for serving supabase functions.
 */
@CacheableTask
abstract class SupabaseFunctionServeTask : DefaultTask() {

    @get:Inject
    internal abstract val execHandleFactory: ExecHandleFactory

    @get:Internal
    internal abstract val compiledSourceDir: DirectoryProperty

    @get:Internal
    internal abstract val rootProjectDir: DirectoryProperty

    @get:Internal
    internal abstract val projectDir: DirectoryProperty

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:Internal
    internal abstract val requestOutputDir: DirectoryProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val requestConfigFile: RegularFileProperty

    @get:Internal
    internal abstract val envFile: RegularFileProperty

    @get:Input
    internal abstract val functionName: Property<String>

    @get:Input
    abstract val verifyJwt: Property<Boolean>

    @get:Input
    abstract val importMap: Property<Boolean>

    @get:Input
    abstract val stackTraceSourceMapStrategy: Property<StackTraceSourceMapStrategy>

    @Nested
    val autoRequest = ServeAutoRequest()

    @Nested
    val inspect = ServeInspect()

    init {
        outputs.upToDateWhen { false }
    }

    /**
     * Configure [autoRequest] in a DSL manner.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun autoRequest(action: ServeAutoRequest.() -> Unit) {
        autoRequest.action()
    }

    /**
     * Configure [inspect] in a DSL manner.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun inspect(action: ServeInspect.() -> Unit) {
        inspect.action()
    }

    @TaskAction
    fun serve() {
        val startParameter = services.get<StartParameter>()
        val properties = startParameter.projectProperties

        val runner = ServeRunner(
            execHandleFactory = execHandleFactory,
            execActionFactory = services.get(),
            properties = properties,
            logger = logger,
            compiledSourceDir = compiledSourceDir,
            rootProjectDir = rootProjectDir,
            projectDir = projectDir,
            supabaseDir = supabaseDir,
            functionName = functionName,
            verifyJwt = verifyJwt,
            deployImportMap = importMap,
            envFile = envFile,
            inspect = inspect,
            sourceMapStrategy = stackTraceSourceMapStrategy,
            autoRequest = autoRequest,
            requestOutputDir = requestOutputDir,
            requestConfigFile = requestConfigFile
        )

        if (startParameter.isContinuous) {
            val registry = services.get(DeploymentRegistry::class.java)
            val handle = registry.get(HANDLE_NAME, ServeDeploymentHandle::class.java)

            if (handle == null) {
                registry.start(
                    HANDLE_NAME,
                    DeploymentRegistry.ChangeBehavior.NONE,
                    ServeDeploymentHandle::class.java,
                    runner,
                    logger,
                    services.get<BuildCancellationToken>()
                )
            } else {
                handle.notifyBuildReloaded()
            }
        } else {
            val context = runner.execute(services)

            getServeCommandFailedReason(
                result = context.target.exitValue,
                message = context.lastMessage
            )?.let { reason ->
                throw SupabaseFunctionServeException(reason)
            }
        }
    }
}