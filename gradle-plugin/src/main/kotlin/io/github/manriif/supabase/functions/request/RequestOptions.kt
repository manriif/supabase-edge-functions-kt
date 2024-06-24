package io.github.manriif.supabase.functions.request

import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName

// Gson set nulls reflectively no matter on default values and non-null types
internal abstract class RequestOptions {

    val http: HttpVersion? = null
    val timeout: Long? = null

    @Suppress("USELESS_ELVIS")
    val headers: Map<String, String> = emptyMap()
        get() = field ?: emptyMap()

    enum class HttpVersion {

        @SerializedName("1.1")
        HTTP_1_1,

        @SerializedName("2")
        HTTP_2
    }
}