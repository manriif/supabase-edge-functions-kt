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
package io.github.manriif.supabase.functions.serve

import org.gradle.api.tasks.Input

/**
 * Behaviour of inspection during function serving.
 * More explanations on [Supabase Docs](https://supabase.com/docs/reference/cli/supabase-functions-serve).
 */
data class ServeInspect(

    /**
     * Activates the inspector capability.
     */
    @Input
    var mode: Mode = Mode.Brk,

    /**
     * Allows the creation of an inspector session which is not allowed by default.
     */
    @Input
    var main: Boolean = false,

    /**
     * Adds the debug flag to the serve command.
     */
    @Input
    var debug: Boolean = false
) {

    /**
     * Available inspection modes.
     */
    enum class Mode(internal val value: String) {
        /**
         * Simply allows a connection without additional behavior. It is not ideal for short
         * scripts, but it can be useful for long-running scripts where you might occasionally want
         * to set breakpoints.
         */
        Run("run"),

        /**
         * Same as [Run] mode, but additionally sets a breakpoint at the first line to pause script
         * execution before any code runs.
         */
        Brk("brk"),

        /**
         * Similar to [Brk] mode, but instead of setting a breakpoint at the first line, it pauses
         * script execution until an inspector session is connected.
         */
        Wait("wait")
    }
}