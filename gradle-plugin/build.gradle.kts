/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

plugins {
    `kotlin-dsl`
    alias(libs.plugins.conventions.common)
    alias(libs.plugins.gradle.plugin.publish)
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.gradle.idea.ext)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.atlassian.sourcemap)
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = projectWebsite
    vcsUrl = projectGitUrl

    plugins {
        create("supabase-function") {
            id = projectGroup
            implementationClass = "io.github.manriif.supabase.functions.SupabaseFunctionPlugin"
            tags = setOf("kotlin", "supabase", "js", "edge-functions")
            displayName = localName
            description = localDescription
        }
    }
}