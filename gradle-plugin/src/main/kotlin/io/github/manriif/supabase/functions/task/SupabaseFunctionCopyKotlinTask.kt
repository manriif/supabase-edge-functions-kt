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
import org.gradle.api.tasks.Optional
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
    @get:Optional
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