package io.github.manriif.supabase.functions.supabase

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Gets a [File] named [fileName] under the supabase/functions dir where supabase directory is resolved
 * from [supabaseDir].
 */
internal fun supabaseAllFunctionsDirFile(
    supabaseDir: DirectoryProperty,
    fileName: String
): File {
    return supabaseDir.file("functions/$fileName").get().asFile
}

/**
 * Gets a [File] named [fileName] under the supabase/functions/[functionName] dir where supabase
 * directory is resolved from [supabaseDir].
 */
internal fun supabaseFunctionDirFile(
    supabaseDir: DirectoryProperty,
    functionName: Provider<String>,
    fileName: String
): File {
    return supabaseDir.file("functions/${functionName.get()}/$fileName").get().asFile
}