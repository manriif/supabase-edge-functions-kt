package io.github.manriif.supabase.functions

import io.github.manriif.supabase.functions.kmp.kotlinVersion
import io.github.manriif.supabase.functions.kmp.setupKotlinMultiplatform
import io.github.manriif.supabase.functions.task.configurePluginTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

class SupabaseFunctionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.checkKotlinVersionCompatibility()

        val extension = target.extensions.create<SupabaseFunctionExtension>("supabaseFunction")
        extension.setupConvention(target)

        target.plugins.withType<KotlinMultiplatformPluginWrapper> {
            target.configurePlugin(
                extension = extension,
                kmpExtension = target.extensions.getByType<KotlinMultiplatformExtension>()
            )
        }

        target.afterEvaluate {
            configureIdeaGradleConfigurations(extension)
        }
    }

    private fun Project.checkKotlinVersionCompatibility() {
        val kotlinVersion = kotlinVersion()

        if (!kotlinVersion.isAtLeast(2, 0, 0)) {
            error(
                "You are applying `$SUPABASE_FUNCTION_PLUGIN_NAME` plugin on a project " +
                        "targeting Kotlin version $kotlinVersion. However, the plugin " +
                        "requires Kotlin version 2.0.0 or newer."
            )
        }
    }

    private fun Project.configurePlugin(
        extension: SupabaseFunctionExtension,
        kmpExtension: KotlinMultiplatformExtension
    ) {
        setupKotlinMultiplatform(kmpExtension)
        configurePluginTasks(extension, kmpExtension)
    }
}