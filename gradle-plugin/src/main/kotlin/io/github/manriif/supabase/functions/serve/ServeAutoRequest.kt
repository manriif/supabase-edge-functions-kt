package io.github.manriif.supabase.functions.serve

import org.gradle.api.tasks.Input

/**
 * Configuration for auto request feature.
 */
data class ServeAutoRequest(

    /**
     * Whether to send a request after the code has changed.
     */
    @Input
    var sendRequestOnCodeChange: Boolean = true,

    /**
     * Duration in milliseconds to wait before sending the request after the code has changed.
     */
    @Input
    var sendRequestOnCodeChangeDelay: Long = 0,

    /**
     * Whether to print the supabase output obtained from `supabase status` command.
     */
    @Input
    var logStatus: Boolean = false,

    /**
     * Whether to print the request response.
     */
    @Input
    var logResponse: Boolean = false,

    /**
     * Whether to validate the requests.
     */
    @Input
    var validate: Boolean = false
)