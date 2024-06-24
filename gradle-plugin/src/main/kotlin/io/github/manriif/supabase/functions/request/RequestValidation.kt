package io.github.manriif.supabase.functions.request

import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName
import java.io.File

// Gson set nulls reflectively no matter on default values and non-null types
internal class RequestValidation {

    val status: Int? = null
    val type: Type? = null
    val body: JsonElement? = null

    @Transient
    var resolvedFile: File? = null

    override fun toString(): String {
        return "RequestValidation(" +
                "status=$status, " +
                "type=$type, " +
                "body=$body" +
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