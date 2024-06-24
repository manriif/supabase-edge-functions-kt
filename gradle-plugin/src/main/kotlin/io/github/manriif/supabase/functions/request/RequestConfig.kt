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

    requireNotNull(status, message("required field `status` is missing"))

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