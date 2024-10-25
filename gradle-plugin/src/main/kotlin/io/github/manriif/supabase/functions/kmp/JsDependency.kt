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
package io.github.manriif.supabase.functions.kmp

import io.github.manriif.supabase.functions.JS_SOURCES_INPUT_DIR
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import javax.inject.Inject

internal abstract class JsDependency @Inject constructor() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectories: Property<FileCollection>

    @get:Internal
    abstract val projectDirectory: DirectoryProperty

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val jsOutputName: Property<String>
}

internal fun Project.jsDependencies(): Provider<Collection<JsDependency>> {
    val projectDependencies = mutableMapOf<String, JsDependency>()

    afterEvaluate {
        findProjectDependencies(projectDependencies)
    }

    return provider(projectDependencies::values)
}

private fun Project.configureJsDependency(collector: MutableMap<String, JsDependency>) {
    if (collector.containsKey(path)) {
        return
    }

    check(collector.values.none { it.projectName.orNull == name }) {
        "Duplicate project name `$name`. Ensure that all projects " +
                "(including included build project's) have a distinct name."
    }

    val dependency = objects.newInstance<JsDependency>().apply {
        projectDirectory.convention(project.layout.projectDirectory)
        projectName.convention(name)
    }

    collector[path] = dependency

    plugins.withType<KotlinMultiplatformPluginWrapper> {
        extensions.findByType<KotlinMultiplatformExtension>()?.run {
            val directories = project.objects.fileCollection().apply {
                disallowUnsafeRead()
            }

            dependency.sourceDirectories.convention(provider { directories })
            dependency.jsOutputName.convention(jsOutputName(this))

            sourceSets.configureEach {
                if (!name.endsWith("test", ignoreCase = true)) {
                    directories.from(project.file("src/$name/$JS_SOURCES_INPUT_DIR"))
                }
            }
        }
    }
}

private fun Project.findProjectDependencies(collector: MutableMap<String, JsDependency>) {
    configureJsDependency(collector)

    // TODO find an alternative for accessing project dependencies to stay compatible with Gradle 9.0
    configurations.forEach { configuration ->
        configuration.allDependencies.withType<ProjectDependency> {
            dependencyProject.findProjectDependencies(collector)
        }
    }
}