package io.github.manriif.supabase.functions.kmp

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

internal fun Project.jsOutputName(kmpExtension: KotlinMultiplatformExtension): Provider<String> {
    val defaultModuleName = defaultOutputName(this)
    var moduleNameProvider = provider { "" }

    kmpExtension.targets.withType<KotlinJsIrTarget>().configureEach {
        moduleName?.let { targetModuleName ->
            moduleNameProvider = moduleNameProvider.map { currentName ->
                currentName.takeIf { it.isNotBlank() } ?: targetModuleName
            }
        }
    }

    moduleNameProvider = moduleNameProvider.map { currentName ->
        currentName.takeIf { it.isNotBlank() } ?: defaultModuleName
    }

    return moduleNameProvider
}

private fun defaultOutputName(project: Project): String {
    var directory = project.projectDir
    val directories = mutableListOf(directory)

    while (directory != project.rootDir) {
        directory = directory.parentFile
        directories.add(0, directory)
    }

    return directories.joinToString("-") { it.name }
}