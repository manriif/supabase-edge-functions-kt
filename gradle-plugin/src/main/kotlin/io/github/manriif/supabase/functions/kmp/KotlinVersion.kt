package io.github.manriif.supabase.functions.kmp

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

internal fun Project.kotlinVersion(): KotlinVersion {
    val kotlinPluginVersion = getKotlinPluginVersion()

    val (major, minor) = kotlinPluginVersion
        .split('.')
        .take(2)
        .map { it.toInt() }

    val patch = kotlinPluginVersion
        .substringAfterLast('.')
        .substringBefore('-')
        .toInt()

    return KotlinVersion(major, minor, patch)
}