package io.github.manriif.supabase.functions.serve.stacktrace

import io.github.manriif.supabase.functions.ExperimentalSupabaseFunctionApi

/**
 * Strategy for resolving source file on uncaught exception.
 */
enum class StackTraceSourceMapStrategy(
    internal val kotlin: Boolean,
    internal val js: Boolean
) {

    /**
     * Do not apply source mapping.
     */
    None(kotlin = false, js = false),

    /**
     * Resolve project js files only.
     */
    JsOnly(kotlin = false, js = true),

    /**
     * Resolve kotlin source files only.
     * However, if the source file is not mapped, the js file will be resolved instead.
     */
    @ExperimentalSupabaseFunctionApi
    KotlinOnly(kotlin = true, js = false),

    /**
     * Resolve kotlin source file whenever possible. Otherwise, fallback to the js file.
     */
    @ExperimentalSupabaseFunctionApi
    KotlinPreferred(kotlin = true, js = true),
}