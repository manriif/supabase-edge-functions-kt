package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.IMPORT_MAP_FILE_NAME
import io.github.manriif.supabase.functions.IMPORT_MAP_JSON_IMPORTS
import io.github.manriif.supabase.functions.IMPORT_MAP_JSON_SCOPES
import io.github.manriif.supabase.functions.IMPORT_MAP_TEMPLATE_FILE_NAME
import io.github.manriif.supabase.functions.error.SupabaseFunctionImportMapTemplateException
import io.github.manriif.supabase.functions.supabase.supabaseAllFunctionsDirFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
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

    @get:InputDirectory
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val importMapsDir: DirectoryProperty

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal val importMapTemplateFile: File
        get() = supabaseAllFunctionsDirFile(supabaseDir, IMPORT_MAP_TEMPLATE_FILE_NAME)

    @get:OutputFile
    internal val aggregatedImportMapFile: File
        get() = supabaseAllFunctionsDirFile(supabaseDir, IMPORT_MAP_FILE_NAME)

    @TaskAction
    fun aggregate() {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val importMap = if (importMapTemplateFile.exists() && importMapTemplateFile.isFile) {
            try {
                JsonParser.parseReader(importMapTemplateFile.reader()).asJsonObject
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

        importMapsDir.get().asFileTree
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