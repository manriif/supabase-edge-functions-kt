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

import io.github.manriif.supabase.functions.idea.RunConfigurationOptions
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * Extensions for configuring supabase function plugin.
 */
abstract class SupabaseFunctionExtension {

    /**
     * Name of the package in which the kotlin's main function resides.
     */
    abstract val packageName: Property<String>

    /**
     * Supabase directory location.
     * By default, the plugin expects this directory to be under the root project directory.
     */
    abstract val supabaseDir: DirectoryProperty

    /**
     * The name of the supabase function.
     * Default to the project name.
     *
     * See [Supabase Docs](https://supabase.com/docs/guides/functions/quickstart) for function
     * naming recommendations.
     */
    abstract val functionName: Property<String>

    /**
     * Optional remote project reference for function deployment task.
     * No default value.
     */
    abstract val projectRef: Property<String>

    /**
     * Whether a valid JWT is required.
     * Default to true.
     *
     * See [Supabase Docs](https://supabase.com/docs/guides/cli/config#functions.function_name.verify_jwt)
     * for more explanation.
     */
    abstract val verifyJwt: Property<Boolean>

    /**
     * Whether to include import_map.json in function serve and deploy task.
     * Default to true.
     */
    abstract val importMap: Property<Boolean>

    /**
     * Env file for function serving.
     * By default, the plugin will use a `.env.local` that is expected to be under the supabase
     * directory. The file will only be used if it exists.
     */
    abstract val envFile: RegularFileProperty

    /**
     * Configure the run configuration to generate for IntelliJ based IDEs.
     */
    val runConfiguration = RunConfigurationOptions()

    /**
     * Allows to configure [runConfiguration] in a DSL manner.
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun runConfiguration(action: Action<RunConfigurationOptions>) {
        action.execute(runConfiguration)
    }
}

internal fun SupabaseFunctionExtension.setupConvention(project: Project) {
    supabaseDir.convention(project.rootProject.layout.projectDirectory.dir("supabase"))
    envFile.convention(supabaseDir.file(LOCAL_ENV_FILE_NAME))
    functionName.convention(project.name)
    verifyJwt.convention(true)
    importMap.convention(true)

    packageName.convention(project.provider {
        error("packageName is not set, please provide the package name of the kotlin main function.")
    })
}