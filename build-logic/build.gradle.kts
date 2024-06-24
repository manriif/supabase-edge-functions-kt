/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        listOf("common", "detekt", "dokka", "kmp", "publishing").forEach { scriptName ->
            named("conventions-$scriptName") {
                version = libs.versions.supabase.functions.get()
            }
        }
    }
}

dependencies {
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}