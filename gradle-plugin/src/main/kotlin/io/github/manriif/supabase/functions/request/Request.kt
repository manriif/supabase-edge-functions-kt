package io.github.manriif.supabase.functions.request

import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName
import java.io.File

// Gson set nulls reflectively no matter on default values and non-null types
internal class Request : RequestOptions() {

    val name: String? = null
    val method: String? = null
    val body: JsonElement? = null
    val type: Type? = null
    val validation: RequestValidation? = null

    @Suppress("USELESS_ELVIS")
    val parameters: Map<String, String> = emptyMap()
        get() = field ?: emptyMap()

    @Transient
    var resolvedFile: File? = null

    override fun toString(): String {
        return "Request(" +
                "name=$name, " +
                "method=$method, " +
                "http=$http, " +
                "timeout=$timeout, " +
                "type=$type, " +
                "headers=$headers, " +
                "parameters=$parameters, " +
                "body=$body, " +
                "validation=$validation" +
                ")"
    }

    enum class Type {

        @SerializedName("plain")
        Plain,

        @SerializedName("json")
        Json,

        @SerializedName("file")
        File
    }
}