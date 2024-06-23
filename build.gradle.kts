/*
 * Copyright 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.detekt) apply false
}

allprojects {
    group = property("project.group").toString()
    version = rootProject.libs.versions.supabase.functions.get()
}