package io.github.manriif.supabase.functions.request

import io.github.manriif.supabase.functions.util.link
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.JsonElement

internal sealed interface ValidationResult {

    fun isValid(validation: RequestValidation): Boolean

    fun expected(validation: RequestValidation, gson: Gson): String?

    fun toString(gson: Gson): String

    object None : ValidationResult {

        override fun isValid(validation: RequestValidation): Boolean = true

        override fun expected(validation: RequestValidation, gson: Gson): String? = null

        override fun toString(gson: Gson): String = "null"
    }

    @JvmInline
    value class Plain(private val content: String) : ValidationResult {

        override fun isValid(validation: RequestValidation): Boolean {
            return validation.body?.asString == content
        }

        override fun expected(validation: RequestValidation, gson: Gson): String? =
            validation.body?.asString

        override fun toString(gson: Gson): String = content
    }

    @JvmInline
    value class Json(private val json: JsonElement) : ValidationResult {

        override fun isValid(validation: RequestValidation): Boolean {
            return json == validation.body
        }

        override fun expected(validation: RequestValidation, gson: Gson): String? =
            gson.toJson(validation.body)

        override fun toString(gson: Gson): String = gson.toJson(json)
    }

    @JvmInline
    value class File(private val file: java.io.File) : ValidationResult {

        override fun isValid(validation: RequestValidation): Boolean {
            return file.readBytes().contentEquals(validation.resolvedFile?.readBytes())
        }

        override fun expected(validation: RequestValidation, gson: Gson): String? {
            return validation.resolvedFile?.canonicalPath
        }

        override fun toString(gson: Gson): String {
            return file.takeIf { it.exists() }?.link("output") ?: "<error>"
        }
    }
}