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

import io.github.manriif.supabase.functions.DENO_KOTLIN_BRIDGE_FUNCTION_NAME
import io.github.manriif.supabase.functions.INDEX_FILE_NAME
import io.github.manriif.supabase.functions.supabase.supabaseFunctionDirFile
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Task responsible for generating necessary files for calling kotlin main function from deno serve
 * function.
 */
@CacheableTask
abstract class SupabaseFunctionGenerateKotlinBridgeTask : DefaultTask() {

    @get:Internal
    internal abstract val supabaseDir: DirectoryProperty

    @get:OutputDirectory
    internal abstract val generatedSourceOutputDir: DirectoryProperty

    @get:Input
    internal abstract val packageName: Property<String>

    @get:Input
    internal abstract val jsOutputName: Property<String>

    @get:Input
    internal abstract val functionName: Property<String>

    @get:Input
    abstract val mainFunctionName: Property<String>

    @get:OutputFile
    internal val indexFile: File
        get() = supabaseFunctionDirFile(supabaseDir, functionName, INDEX_FILE_NAME)

    @TaskAction
    fun generate() {
        createDenoIndexFile()
        createKotlinBridgeFunction()
    }

    /**
     * Creates the Deno function index.ts file.
     */
    private fun createDenoIndexFile() {
        val content = """
            |import { $DENO_KOTLIN_BRIDGE_FUNCTION_NAME } from './kotlin/${jsOutputName.get()}.mjs';
            |
            |Deno.serve(async (req) => {
            |   return await $DENO_KOTLIN_BRIDGE_FUNCTION_NAME(req)
            |})
        """.trimMargin()

        indexFile.writeText(content)
    }

    /**
     * Creates the Kotlin function that will be called by the index.ts serve function
     */
    private fun createKotlinBridgeFunction() {
        val delicateCoroutineApi = ClassName("kotlinx.coroutines", "DelicateCoroutinesApi")
        val experimentalJsExport = ClassName("kotlin.js", "ExperimentalJsExport")
        val optIn = ClassName("kotlin", "OptIn")
        val request = ClassName("org.w3c.fetch", "Request")
        val response = ClassName("org.w3c.fetch", "Response")
        val promise = ClassName("kotlin.js", "Promise")
        val jsExport = ClassName("kotlin.js", "JsExport")

        val optInAnnotation = AnnotationSpec.builder(optIn)
            .addMember("%L::class, %L::class", delicateCoroutineApi, experimentalJsExport)
            .build()

        val serveFunction = FunSpec.builder(DENO_KOTLIN_BRIDGE_FUNCTION_NAME)
            .addAnnotation(optInAnnotation)
            .addAnnotation(jsExport)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("request", request)
            .returns(promise.parameterizedBy(response))
            .addCode(
                """
                return GlobalScope
                    .async(
                        context = Dispatchers.Unconfined,
                        start = CoroutineStart.UNDISPATCHED,
                        block = {
                            ${mainFunctionName.get()}(request)
                        }
                    )
                    .asPromise()
            """.trimIndent()
            )
            .build()

        FileSpec.builder(packageName.get(), "SupabaseServe")
            .addImport(
                "kotlinx.coroutines",
                "CoroutineStart",
                "Dispatchers",
                "GlobalScope",
                "asPromise",
                "async"
            )
            .addFunction(serveFunction)
            .build()
            .writeTo(generatedSourceOutputDir.get().asFile)
    }
}