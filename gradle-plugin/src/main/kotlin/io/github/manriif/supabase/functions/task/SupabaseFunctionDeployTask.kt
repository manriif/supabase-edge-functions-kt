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

import io.github.manriif.supabase.functions.supabase.importMap
import io.github.manriif.supabase.functions.supabase.noVerifyJwt
import io.github.manriif.supabase.functions.supabase.projectRef
import io.github.manriif.supabase.functions.supabase.supabaseCommand
import io.github.manriif.supabase.functions.error.SupabaseFunctionDeployException
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Task responsible for deploying function to remote project.
 */
@CacheableTask
abstract class SupabaseFunctionDeployTask : DefaultTask() {

    @get:Inject
    internal abstract val execOperations: ExecOperations

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:Input
    internal abstract val functionName: Property<String>

    @get:Input
    @get:Optional
    abstract val projectRef: Property<String>

    @get:Input
    abstract val verifyJwt: Property<Boolean>

    @get:Input
    abstract val importMap: Property<Boolean>

    init {
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun deploy() {
        val output = ByteArrayOutputStream()

        val result = execOperations.exec {
            isIgnoreExitValue = true
            executable = supabaseCommand()
            workingDir = supabaseDir.get().asFile
            errorOutput = output

            args("functions", "deploy", functionName.get())
            projectRef(projectRef)
            importMap(supabaseDir, importMap)
            noVerifyJwt(verifyJwt)
        }

        if (result.exitValue != 0) {
            throw SupabaseFunctionDeployException("Failed to deploy function: $output")
        }
    }
}