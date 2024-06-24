package io.github.manriif.supabase.functions

import io.github.manriif.supabase.functions.task.TASK_FUNCTION_DEPLOY
import io.github.manriif.supabase.functions.task.TASK_FUNCTION_SERVE
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.IdeaExtPlugin
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

/**
 * A run configuration that can be generated.
 */
data class ServeRunConfiguration(

    /**
     * Whether a run configuration should be generated.
     */
    var enabled: Boolean = true,

    /**
     * Whether continuous build is enabled for the generated run configuration.
     */
    var continuous: Boolean = true
)

/**
 * IDEA run configurations that must be generated.
 */
data class RunConfigurationOptions(

    /**
     * Indicates if a run configuration should be generated for deploying the function.
     */
    var deploy: Boolean = true,

    /**
     * [ServeRunConfiguration] for serving the function.
     */
    var serve: ServeRunConfiguration = ServeRunConfiguration(),

    /**
     * [ServeRunConfiguration] for serving the function and inspecting (debugging) the function via
     * Chrome DevTools.
     */
    var inspect: ServeRunConfiguration = ServeRunConfiguration(),

    /**
     * [ServeRunConfiguration] for serving the function and automatically send requests.
     */
    var request: ServeRunConfiguration = ServeRunConfiguration()
)

///////////////////////////////////////////////////////////////////////////
// IDEA Configurations
///////////////////////////////////////////////////////////////////////////

internal fun Project.configureIdeaGradleConfigurations(
    extension: SupabaseFunctionExtension
) = with(extension.runConfiguration) {
    if (deploy) {
        createIdeaDeployConfiguration(extension)
    }

    if (serve.enabled) {
        createIdeaServeConfiguration(extension, serve)
    }

    if (inspect.enabled) {
        createIdeaInspectConfiguration(extension, inspect)
    }

    if (request.enabled) {
        createAutoRequestConfiguration(extension, request)
    }
}

private fun Project.registerIdeaGradleConfiguration(
    extension: SupabaseFunctionExtension,
    name: String,
    taskName: String,
    configure: (Gradle.() -> Unit)? = null
) {
    rootProject.pluginManager.apply(IdeaExtPlugin::class)

    rootProject.extensions.findByType<IdeaModel>()?.let { idea ->
        idea.project.settings.runConfigurations.register<Gradle>(
            name = "${extension.functionName.get()} $name"
        ) {
            projectPath = projectDir.absolutePath
            taskNames = listOf(taskName)
            configure?.invoke(this)
        }
    }
}

private fun Gradle.configureServe(
    configuration: ServeRunConfiguration,
    parameters: String = ""
) {
    if (configuration.continuous) {
        scriptParameters = "$parameters --continuous"
    }
}

private fun Project.createIdeaDeployConfiguration(
    extension: SupabaseFunctionExtension
) = registerIdeaGradleConfiguration(
    extension = extension,
    name = "deploy",
    taskName = TASK_FUNCTION_DEPLOY
)

private fun Project.createIdeaServeConfiguration(
    extension: SupabaseFunctionExtension,
    configuration: ServeRunConfiguration
) = registerIdeaGradleConfiguration(
    extension = extension,
    name = "serve",
    taskName = TASK_FUNCTION_SERVE,
) {
    configureServe(configuration)
}

private fun Project.createIdeaInspectConfiguration(
    extension: SupabaseFunctionExtension,
    configuration: ServeRunConfiguration,
) = registerIdeaGradleConfiguration(
    extension = extension,
    name = "inspect",
    taskName = TASK_FUNCTION_SERVE
) {
    configureServe(configuration, "-P$PARAMETER_INSPECT")
}

private fun Project.createAutoRequestConfiguration(
    extension: SupabaseFunctionExtension,
    configuration: ServeRunConfiguration,
) = registerIdeaGradleConfiguration(
    extension = extension,
    name = "request",
    taskName = TASK_FUNCTION_SERVE
) {
    configureServe(configuration, "-P$PARAMETER_AUTO_REQUEST")
}