package io.github.manriif.supabase.functions.util

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Returns a [Provider] that will provides the file ony if it exists.
 */
internal fun Provider<RegularFile>.orNone(): Provider<RegularFile> {
    @Suppress("UnstableApiUsage")
    return filter { it.asFile.exists() }
}

/**
 * Returns a [Provider] that will provides the file ony if it exists.
 */
internal fun RegularFile.orNone(project: Project): Provider<RegularFile> {
    return project.provider { this }.orNone()
}