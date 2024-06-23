package io.github.manriif.supabase.functions.request

internal data class RequestClientOptions(
    val logStatus: Boolean,
    val logResponse: Boolean,
    val requestDelay: Long = 0
)
