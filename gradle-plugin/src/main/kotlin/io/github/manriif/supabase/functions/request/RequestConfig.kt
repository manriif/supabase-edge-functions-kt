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
package io.github.manriif.supabase.functions.request

import org.gradle.api.file.DirectoryProperty
import org.jetbrains.kotlin.com.google.gson.JsonElement
import java.io.File

// Gson set nulls reflectively no matter on default values and non-null types
internal class RequestConfig : RequestOptions() {

    @Suppress("USELESS_ELVIS")
    val requests: List<Request> = emptyList()
        get() = field ?: emptyList()

    override fun toString(): String {
        return "RequestConfig(" +
                "http=$http, " +
                "timeout=$timeout, " +
                "headers=$headers, " +
                "requests=$requests" +
                ")"
    }
}

internal fun RequestConfig.checkValidity(projectDir: DirectoryProperty) {
    requests.forEachIndexed { index, request ->
        request.checkValidity(projectDir, index)
    }
}

private fun Request.checkValidity(projectDir: DirectoryProperty, index: Int) {
    fun message(message: String): () -> String = {
        "requests[$index]: $message"
    }

    requireNotNull(name, message("required field `name` is missing"))
    requireNotNull(method, message("required field `method` is missing"))

    if (type != null) {
        requireNotNull(body, message("required field `body`is missing"))

        when (type) {
            Request.Type.Plain -> {
                require(body.isJsonPrimitive, message("plain type `body` must be a valid string"))
            }
            Request.Type.Json -> {
                require(body.isJsonObject, message("json type `body` must be a valid json object"))
            }
            Request.Type.File -> {
                resolvedFile = projectDir.checkBodyFile(body, ::message)
            }
        }
    }

    validation?.checkValidity(projectDir, index)
}

private fun RequestValidation.checkValidity(projectDir: DirectoryProperty, index: Int) {
    fun message(message: String): () -> String = {
        "requests[$index].validation: $message"
    }

    if (type != null) {
        requireNotNull(body, message("required field `body`is missing"))

        when (type) {
            RequestValidation.Type.Plain -> {
                require(body.isJsonPrimitive, message("plain type `body` must be a valid string"))
            }
            RequestValidation.Type.Json -> {
                require(body.isJsonObject, message("json type `body` must be a valid json object"))
            }
            RequestValidation.Type.File -> {
                resolvedFile = projectDir.checkBodyFile(body, ::message)
            }
        }
    }
}

private fun DirectoryProperty.checkBodyFile(
    element: JsonElement?,
    message: (String) -> () -> String
): File {
    require(
        element?.isJsonPrimitive == true,
        message("file type `body` must be a valid file path")
    )

    val resolvedFile = asFile.get().resolve(File(element!!.asString))

    require(
        resolvedFile.isFile,
        message("`body` must be a valid path relative to the project directory")
    )

    return resolvedFile
}