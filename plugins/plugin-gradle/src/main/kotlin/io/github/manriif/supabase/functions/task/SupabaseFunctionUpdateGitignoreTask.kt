package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.FUNCTION_JS_TARGET_DIR
import io.github.manriif.supabase.functions.FUNCTION_KOTLIN_TARGET_DIR
import io.github.manriif.supabase.functions.GITIGNORE_FILE_NAME
import io.github.manriif.supabase.functions.IMPORT_MAP_FILE_NAME
import io.github.manriif.supabase.functions.INDEX_FILE_NAME
import io.github.manriif.supabase.functions.supabase.supabaseAllFunctionsDirFile
import io.github.manriif.supabase.functions.supabase.supabaseFunctionDirFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File

/**
 * Task responsible for updating .gitignore files by filling them with plugin generated files.
 */
@CacheableTask
abstract class SupabaseFunctionUpdateGitignoreTask : DefaultTask() {

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:Input
    internal abstract val functionName: Property<String>

    @get:Input
    abstract val importMapEntry: Property<Boolean>

    @get:Input
    abstract val indexEntry: Property<Boolean>

    @get:OutputFile
    internal val allFunctionsDirGitignore: File
        get() = supabaseAllFunctionsDirFile(supabaseDir, GITIGNORE_FILE_NAME)

    @get:OutputFile
    internal val functionDirGitignore: File
        get() = supabaseFunctionDirFile(supabaseDir, functionName, GITIGNORE_FILE_NAME)

    @TaskAction
    fun update() {
        if (importMapEntry.get()) {
            updateGitignoreFile(allFunctionsDirGitignore, IMPORT_MAP_FILE_NAME)
        }

        if (indexEntry.get()) {
            updateGitignoreFile(
                functionDirGitignore,
                INDEX_FILE_NAME,
                FUNCTION_JS_TARGET_DIR,
                FUNCTION_KOTLIN_TARGET_DIR
            )
        } else {
            updateGitignoreFile(
                functionDirGitignore,
                FUNCTION_JS_TARGET_DIR,
                FUNCTION_KOTLIN_TARGET_DIR
            )
        }
    }

    /**
     * Updates [gitignoreFile] appending each [filePaths] entry to it's content if not recorded.
     */
    private fun updateGitignoreFile(gitignoreFile: File, vararg filePaths: String) {
        if (!gitignoreFile.exists()) {
            gitignoreFile.ensureParentDirsCreated()
            return gitignoreFile.writeText(filePaths.joinToString("\n"))
        }

        val gitignoreEntries = gitignoreFile.useLines { it.toList() }

        filePaths.forEach { path ->
            if (!gitignoreEntries.contains(path)) {
                gitignoreFile.appendText("\n$path")
            }
        }
    }
}