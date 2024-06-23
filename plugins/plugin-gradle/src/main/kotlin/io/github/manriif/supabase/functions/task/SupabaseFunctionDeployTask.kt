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