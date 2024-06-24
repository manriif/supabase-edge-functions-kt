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