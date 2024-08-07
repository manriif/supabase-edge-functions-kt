/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.gradle.plugin.publish) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.supabase.functions.get()
    extra["isModule"] = path.startsWith(":modules")

    val dokkaBase = """{
        "footerMessage": "© 2024 <a href=\"https://github.com/manriif\">Maanrifa Bacar Ali</a>."
    }"""

    tasks.withType<AbstractDokkaTask>().configureEach {
        pluginsMapConfiguration = mapOf("org.jetbrains.dokka.base.DokkaBase" to dokkaBase)
    }
}

tasks.withType<DokkaMultiModuleTask> {
    val dokkaDir = rootProject.layout.projectDirectory.dir("dokka")

    includes = dokkaDir.files("README.md")
    moduleName = rootProject.property("project.name").toString()
    outputDirectory = dokkaDir.dir("documentation")
}