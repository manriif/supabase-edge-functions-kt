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