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
package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.FUNCTION_JS_TARGET_DIR
import io.github.manriif.supabase.functions.FUNCTION_KOTLIN_TARGET_DIR
import io.github.manriif.supabase.functions.IMPORT_MAP_JSON_IMPORTS
import io.github.manriif.supabase.functions.IMPORT_MAP_JSON_SCOPES
import io.github.manriif.supabase.functions.JS_SOURCES_INPUT_DIR
import io.github.manriif.supabase.functions.kmp.JsDependency
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.JsonObject
import org.jetbrains.kotlin.gradle.targets.js.npm.fromSrcPackageJson
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.relativeTo

private const val MODULE_TARGET = "module"

/**
 * Task responsible for generating import_map.json file for the function.
 *
 * If this task is disabled:
 *
 * - NPM dependencies for the project and its dependencies must be manually
 * added.
 * - JS imports must be manually added.
 */
@CacheableTask
abstract class SupabaseFunctionGenerateImportMapTask : DefaultTask() {

    @get:Internal
    internal abstract val importMapsDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val packageJsonDir: DirectoryProperty

    @get:Input
    internal abstract val functionName: Property<String>

    @get:Nested
    internal abstract val jsDependencies: ListProperty<JsDependency>

    @get:OutputFile
    internal val importMapFile: File
        get() = importMapsDir.file("${functionName.get()}.json").get().asFile

    @TaskAction
    fun generate() {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val importMap = JsonObject().apply {
            add(IMPORT_MAP_JSON_IMPORTS, createFunctionImports())
            add(IMPORT_MAP_JSON_SCOPES, createFunctionScopes())
        }

        importMapFile.writeText(gson.toJson(importMap))
    }

    private fun createFunctionImports(): JsonObject {
        val imports = JsonObject()
        val packageJsonFile = packageJsonDir.file("package.json").get().asFile
        val packageJson = fromSrcPackageJson(packageJsonFile) ?: return imports

        packageJson.dependencies.forEach { (packageName, version) ->
            imports.addProperty(packageName, "npm:$packageName@$version")
        }

        return imports
    }

    private fun functionRelativePath(filePath: String): String {
        return "./${functionName.get()}/$filePath"
    }

    private fun kotlinSource(filePath: String): String {
        return functionRelativePath("$FUNCTION_KOTLIN_TARGET_DIR/$filePath")
    }

    private fun jsSource(filePath: String): String {
        return functionRelativePath("$FUNCTION_JS_TARGET_DIR/$filePath")
    }

    private fun createFunctionScopes(): JsonObject {
        val scopes = JsonObject()
        val jsSourcesPath = Path(JS_SOURCES_INPUT_DIR)
        val jsGlobalDir = jsSource("")
        val jsGlobalScope = JsonObject()

        scopes.add(jsGlobalDir, jsGlobalScope)

        jsDependencies.get().forEach { dependency ->
            val kotlinSourceFile = kotlinSource("${dependency.jsOutputName.get()}.mjs")
            val files = dependency.sourceDirectories.get().asFileTree

            if (!files.isEmpty) {
                val projectPath = dependency.projectDirectory.get().asFile.toPath()
                val kotlinSourceScope = JsonObject()

                scopes.add(kotlinSourceFile, kotlinSourceScope)

                files.forEach { file ->
                    val pathFromProject = file.toPath().relativeTo(projectPath)
                    val jsPathIndex = pathFromProject.indexOf(jsSourcesPath) + 1
                    val modulePath = pathFromProject.subpath(jsPathIndex, pathFromProject.nameCount)

                    kotlinSourceScope.addProperty(
                        "$MODULE_TARGET/${modulePath}",
                        jsSource("${dependency.jsOutputName.get()}/${modulePath}")
                    )
                }
            }

            jsGlobalScope.addProperty(dependency.projectName.get(), kotlinSourceFile)

            val jsModuleDir = jsSource("${dependency.jsOutputName.get()}/")
            val jsModuleScope = JsonObject()

            scopes.add(jsModuleDir, jsModuleScope)
            jsModuleScope.addProperty(MODULE_TARGET, kotlinSourceFile)
        }

        return scopes
    }
}