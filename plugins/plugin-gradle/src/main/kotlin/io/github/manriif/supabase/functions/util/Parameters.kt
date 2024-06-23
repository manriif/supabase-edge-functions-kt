package io.github.manriif.supabase.functions.util

fun Map<String, String>.getBooleanOrDefault(key: String, default: Boolean): Boolean {
    if (!containsKey(key)) {
        return default
    }

    val value = get(key)?.lowercase()
        ?: return default


    if (value.isBlank()) {
        return true
    }

    return value.toBooleanStrictOrNull() ?: default
}

fun Map<String, String>.getLongOrDefault(key: String, default: Long = 0): Long {
    if (!containsKey(key)) {
        return default
    }

    val value = get(key) ?: return default

    if (value.isBlank()) {
        return default
    }

    return value.toLongOrNull() ?: default
}