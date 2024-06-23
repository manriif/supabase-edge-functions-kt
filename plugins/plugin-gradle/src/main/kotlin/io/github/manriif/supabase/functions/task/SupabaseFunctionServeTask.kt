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
import org.gradle.configurationcache.extensions.get
import org.gradle.deployment.internal.DeploymentRegistry
import org.gradle.initialization.BuildCancellationToken
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

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
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
    val inspect = ServeInspect()

    @Nested
    val autoRequest = ServeAutoRequest()

    init {
        outputs.upToDateWhen { false }
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