package io.github.manriif.supabase.functions.supabase

import io.github.manriif.supabase.functions.serve.ServeInspect
import io.github.manriif.supabase.functions.IMPORT_MAP_FILE_NAME
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.internal.os.OperatingSystem
import org.gradle.process.ExecSpec

/**
 * Gets the supabase CLI command.
 * This function exists because of an [issue](https://github.com/gradle/gradle/issues/10483)
 * between macOS and Java 21.
 */
internal fun supabaseCommand(): String {
    return when {
        OperatingSystem.current().isMacOsX -> "/usr/local/bin/supabase"
        else -> "supabase"
    }
}

/**
 * Appends import-map argument to [this] if [deployImportMap] is set to true.
 */
internal fun ExecSpec.importMap(
    supabaseDir: DirectoryProperty,
    deployImportMap: Property<Boolean>
) {
    if (deployImportMap.isPresent && deployImportMap.get()) {
        val importMapFile = supabaseAllFunctionsDirFile(supabaseDir, IMPORT_MAP_FILE_NAME)
        args("--import-map", importMapFile.canonicalPath)
    }
}

/**
 * Appends no-verify-jwt argument to [this] if [verifyJwt] is set to false.
 */
internal fun ExecSpec.noVerifyJwt(verifyJwt: Property<Boolean>) {
    if (verifyJwt.isPresent && !verifyJwt.get()) {
        args("--no-verify-jwt")
    }
}

/**
 * Appends project-ref argument to [this] if [projectRef] is not empty.
 */
internal fun ExecSpec.projectRef(projectRef: Property<String>) {
    if (projectRef.isPresent && projectRef.get().isNotBlank()) {
        args("--project-ref", projectRef.get())
    }
}

/**
 * Appends env-file argument to [this] if [envFile] points to a valid file.
 */
internal fun ExecSpec.envFile(envFile: RegularFileProperty) {
    if (!envFile.isPresent) {
        return
    }

    val file = envFile.get().asFile

    if (file.exists()) {
        args("--env-file", file.canonicalPath)
    }
}

/**
 * Appends inspection related arguments to [this] based on [inspect] properties.
 */
internal fun ExecSpec.inspect(inspect: ServeInspect) {
    if (inspect.debug) {
        args("--debug")
    }

    args("--inspect-mode", inspect.mode.value)

    if (inspect.main) {
        args("--inspect-main")
    }
}