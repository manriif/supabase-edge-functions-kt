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