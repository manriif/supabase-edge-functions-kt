/**
 * Copyright 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

rootProject.name = "supabase-functions-kt"

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

fun includeNamed(path: String, name: String) {
    include(path)
    project(path).name = "supabase-functions-$name"
}

fun includePlugin(name: String) = includeNamed(":plugins:plugin-$name", name)
fun includeModule(name: String) = includeNamed(":modules:module-$name", name)

includePlugin("gradle")
includeModule("deno-bindings")
includeModule("fetch-json")
