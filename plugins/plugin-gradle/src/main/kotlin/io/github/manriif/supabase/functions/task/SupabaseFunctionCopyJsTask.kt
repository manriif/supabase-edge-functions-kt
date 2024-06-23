package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.FUNCTION_JS_TARGET_DIR
import io.github.manriif.supabase.functions.kmp.JsDependency
import io.github.manriif.supabase.functions.supabase.supabaseFunctionDirFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

/**
 * Task responsible for copying generated js code into supabase function directory.
 */
@CacheableTask
abstract class SupabaseFunctionCopyJsTask : DefaultTask() {

    @get:Inject
    internal abstract val fileSystemOperations: FileSystemOperations

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:Nested
    internal abstract val jsDependencies: ListProperty<JsDependency>

    @get:Input
    internal abstract val functionName: Property<String>

    @get:OutputDirectory
    internal val jsTargetDir: File
        get() = supabaseFunctionDirFile(supabaseDir, functionName, FUNCTION_JS_TARGET_DIR)

    @TaskAction
    fun generate() {
        if (jsTargetDir.exists()) {
            jsTargetDir.deleteRecursively()
        }

        jsDependencies.get().forEach { dependency ->
            copyJsSources(dependency)
        }

        if (jsTargetDir.list()?.isEmpty() == true) {
            jsTargetDir.delete()
        }
    }

    private fun copyJsSources(dependency: JsDependency) {
        val outputDirectory = File(jsTargetDir, dependency.jsOutputName.get())

        val directories = dependency.sourceDirectories.get()
            .filter { it.isDirectory }

        fileSystemOperations.copy {
            duplicatesStrategy = DuplicatesStrategy.FAIL
            from(directories)
            into(outputDirectory)
        }
    }
}