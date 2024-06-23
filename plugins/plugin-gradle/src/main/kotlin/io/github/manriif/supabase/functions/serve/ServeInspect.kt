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