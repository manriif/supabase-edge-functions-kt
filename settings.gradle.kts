/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

rootProject.name = "supabase-edge-functions-kt"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://packages.atlassian.com/mvn/maven-atlassian-external/")
    }
}

fun includeModule(name: String) {
    val path = ":modules:$name"
    include(path)
    project(path).name = "module-$name"
}

include("gradle-plugin")
includeModule("binding-deno")
includeModule("fetch-json")