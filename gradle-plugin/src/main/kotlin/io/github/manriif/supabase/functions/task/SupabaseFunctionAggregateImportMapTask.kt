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

import io.github.manriif.supabase.functions.IMPORT_MAP_FILE_NAME
import io.github.manriif.supabase.functions.IMPORT_MAP_JSON_IMPORTS
import io.github.manriif.supabase.functions.IMPORT_MAP_JSON_SCOPES
import io.github.manriif.supabase.functions.IMPORT_MAP_TEMPLATE_FILE_NAME
import io.github.manriif.supabase.functions.error.SupabaseFunctionImportMapTemplateException
import io.github.manriif.supabase.functions.supabase.supabaseAllFunctionsDirFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.JsonObject
import org.jetbrains.kotlin.com.google.gson.JsonParser
import java.io.File

/**
 * Task responsible for aggregating import maps from all functions into one import_map.json.
 */
@CacheableTask
abstract class SupabaseFunctionAggregateImportMapTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val importMapDirs: ConfigurableFileCollection

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val importMapTemplateFile: RegularFileProperty

    @get:OutputFile
    internal val aggregatedImportMapFile: File
        get() = supabaseAllFunctionsDirFile(supabaseDir, IMPORT_MAP_FILE_NAME)

    @TaskAction
    fun aggregate() {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val template = importMapTemplateFile.orNull?.asFile

        val importMap = if (template?.exists() == true && template.isFile) {
            try {
                JsonParser.parseReader(template.reader()).asJsonObject
            } catch (throwable: Throwable) {
                throw SupabaseFunctionImportMapTemplateException(
                    message = "Failed to load $IMPORT_MAP_TEMPLATE_FILE_NAME",
                    cause = throwable
                )
            }
        } else {
            JsonObject()
        }

        if (!importMap.has(IMPORT_MAP_JSON_IMPORTS)) {
            importMap.add(IMPORT_MAP_JSON_IMPORTS, JsonObject())
        }

        if (!importMap.has(IMPORT_MAP_JSON_SCOPES)) {
            importMap.add(IMPORT_MAP_JSON_SCOPES, JsonObject())
        }

        val imports = importMap.getAsJsonObject(IMPORT_MAP_JSON_IMPORTS)
        val scopes = importMap.getAsJsonObject(IMPORT_MAP_JSON_SCOPES)

        importMapDirs.asFileTree
            .matching {
                include { file ->
                    !file.isDirectory && file.name.endsWith(".json")
                }
            }
            .mapNotNull { file ->
                kotlin.runCatching {
                    JsonParser.parseReader(file.reader()).asJsonObject
                }.getOrNull()
            }.forEach { json ->
                json.get(IMPORT_MAP_JSON_IMPORTS).takeIf { it.isJsonObject }?.asJsonObject?.run {
                    keySet().forEach { key ->
                        if (!imports.has(key) && get(key).isJsonPrimitive) {
                            imports.addProperty(key, get(key).asString)
                        }
                    }
                }

                json.get(IMPORT_MAP_JSON_SCOPES).takeIf { it.isJsonObject }?.asJsonObject?.run {
                    keySet().forEach { key ->
                        if (!scopes.has(key) && get(key).isJsonObject) {
                            scopes.add(key, get(key))
                        }
                    }
                }
            }

        aggregatedImportMapFile.writeText(gson.toJson(importMap))
    }
}