/**
 * Copyright 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

rootProject.name = "supabase-edge-functions-kt"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
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