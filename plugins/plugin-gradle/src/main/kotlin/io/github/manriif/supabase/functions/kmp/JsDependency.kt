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
    findProjectDependencies(projectDependencies)
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

    configurations.configureEach {
        allDependencies.withType<ProjectDependency> {
            dependencyProject.findProjectDependencies(collector)
        }
    }
}