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