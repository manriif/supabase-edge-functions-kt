/**
 * Copyright 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka) apply false
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.supabase.functions.get()
}

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        documentedVisibilities = setOf(DokkaConfiguration.Visibility.PUBLIC)

        // Read docs for more details: https://kotlinlang.org/docs/dokka-gradle.html#source-link-configuration
        sourceLink {
            localDirectory = rootProject.projectDir
            remoteUrl = URI("https://github.com/manriif/supabase-functions-kt/tree/dev").toURL()
            remoteLineSuffix = "#L"
        }
    }
}