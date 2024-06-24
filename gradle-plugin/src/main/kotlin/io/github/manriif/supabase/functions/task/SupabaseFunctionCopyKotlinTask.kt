package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.FUNCTION_KOTLIN_TARGET_DIR
import io.github.manriif.supabase.functions.supabase.supabaseFunctionDirFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * Task responsible for copying generated kotlin code into supabase function directory.
 */
@CacheableTask
abstract class SupabaseFunctionCopyKotlinTask : DefaultTask() {

    @get:Inject
    internal abstract val fileSystemOperations: FileSystemOperations

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val compiledSourceDir: DirectoryProperty

    @get:Input
    internal abstract val functionName: Property<String>

    @get:OutputDirectory
    internal val kotlinTargetDir: File
        get() = supabaseFunctionDirFile(supabaseDir, functionName, FUNCTION_KOTLIN_TARGET_DIR)

    @TaskAction
    fun generate() {
        if (kotlinTargetDir.exists()) {
            kotlinTargetDir.deleteRecursively()
        }

        fileSystemOperations.copy {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(compiledSourceDir)
            into(kotlinTargetDir)
            include { it.name.endsWith(".mjs") }
        }
    }
}