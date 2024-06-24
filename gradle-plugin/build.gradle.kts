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
    val url = "https://github.com/manriif/supabase-edge-functions-kt"

    website.set(url)
    vcsUrl.set(url)

    plugins {
        create("supabase-function") {
            id = "io.github.manriif.supabase.functions"
            implementationClass = "io.github.manriif.supabase.functions.SupabaseFunctionPlugin"
            tags = setOf("kotlin", "supabase", "js", "edge-functions")
            displayName = "Supabase Edge Functions for Kotlin - Gradle Plugin"

            description = """
                The plugin helps in building Supabase Edge Functions using Kotlin as primary programming language.
                It offers support for multi-module project and Javascript sources.

                Additionnaly, it provides gradle tasks for serving, inspecting and testing your functions locally and later deploying them to a remote project.
            """.trimIndent()
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}