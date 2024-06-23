package io.github.manriif.supabase.functions.serve

private const val PROCESS_KILLED = 143
private const val DOCKER_KILLED = 137

internal fun getServeCommandFailedReason(result: Int, message: String?): String? {
    if (result == 0 || result == PROCESS_KILLED || result == DOCKER_KILLED) {
        return null
    }

    val reason = message.takeIf { !it.isNullOrBlank() }
        ?: "supabase functions serve` finished with exit code $result"

    return "Failed to serve function: $reason"
}