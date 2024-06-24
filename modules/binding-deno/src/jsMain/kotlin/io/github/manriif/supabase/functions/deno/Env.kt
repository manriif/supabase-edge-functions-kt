package io.github.manriif.supabase.functions.deno

/**
 * Environment API.
 */
external interface Env {

    /**
     * Whether an environment variable is set for [key].
     */
    fun has(key: String): Boolean

    /**
     * Gets the value of an environment variable or `null` if [key] doesn't exist.
     */
    operator fun get(key: String): String?

    /**
     * Sets the [value] of the environment variable identified by [key].
     */
    operator fun set(key: String, value: String)

    /**
     * Deletes the value of environment variable identified by [key].
     */
    fun delete(key: String)

    /**
     * Returns a snapshot of the environment variables at invocation.
     */
    fun toObject(): dynamic
}

/**
 * Gets the environment variables for [key] or throws an ISE if no value is present.
 */
fun Env.require(key: String): String {
    return checkNotNull(get(key)) { "Environment value for key `$key` is not set" }
}