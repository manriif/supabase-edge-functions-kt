package io.github.manriif.supabase.functions.kmp

import io.github.manriif.supabase.functions.COROUTINES_VERSION
import io.github.manriif.supabase.functions.COROUTINES_VERSION_GRADLE_PROPERTY
import io.github.manriif.supabase.functions.SUPABASE_FUNCTION_PLUGIN_NAME
import io.github.manriif.supabase.functions.task.TASK_GENERATE_BRIDGE
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapNamesPolicy
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

internal fun Project.setupKotlinMultiplatform(kmpExtension: KotlinMultiplatformExtension) {
    kmpExtension.targets.withType<KotlinJsIrTarget>().configureEach {
        ensureMeetRequirements()
        configureCompilation()
    }

    kmpExtension.sourceSets.named { it == "jsMain" }.configureEach {
        val coroutinesVersion = findProperty(COROUTINES_VERSION_GRADLE_PROPERTY)?.toString()
            ?: COROUTINES_VERSION

        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
        }
    }
}

private fun KotlinJsIrTarget.ensureMeetRequirements() {
    val granularity = project.findProperty("kotlin.js.ir.output.granularity")?.toString()

    if (!(granularity.isNullOrBlank() || granularity == "per-module")) {
        error(
            "Only `per-module` JS IR output granularity is supported " +
                    "by `$SUPABASE_FUNCTION_PLUGIN_NAME` plugin. " +
                    "Current granularity is `$granularity`."
        )
    }

    if (isBrowserConfigured) {
        error(
            "Browser execution environment is not supported by " +
                    "`$SUPABASE_FUNCTION_PLUGIN_NAME` plugin."
        )
    }

    if (!isNodejsConfigured) {
        error(
            "Node.js execution environment is a requirement " +
                    "for `$SUPABASE_FUNCTION_PLUGIN_NAME` plugin."
        )
    }
}

private fun KotlinJsIrTarget.configureCompilation() {
    compilations.named(KotlinCompilation.MAIN_COMPILATION_NAME) {
        // Module kind is not set when using new compiler option DSL, fallback to deprecated one
        @Suppress("DEPRECATION")
        compilerOptions.configure {
            val kind = moduleKind.orNull

            if (kind != JsModuleKind.MODULE_ES) {
                error(
                    "Plugin `supabase-function` only supports ES module kind. " +
                            "Current module kind is $kind."
                )
            }

            // [KT-47968](https://youtrack.jetbrains.com/issue/KT-47968/KJS-IR-Debug-in-external-tool-cant-step-into-library-function-with-available-sources)
            // [KT-49757](https://youtrack.jetbrains.com/issue/KT-49757/Kotlin-JS-support-sourceMapEmbedSources-setting-by-IR-backend)
            sourceMap.set(true)
            sourceMapNamesPolicy.set(JsSourceMapNamesPolicy.SOURCE_MAP_NAMES_POLICY_FQ_NAMES)
            sourceMapEmbedSources.set(JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS)
        }

        compileTaskProvider.configure {
            dependsOn(TASK_GENERATE_BRIDGE)
        }
    }
}