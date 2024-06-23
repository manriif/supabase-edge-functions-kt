plugins {
    `kotlin-dsl`
    id(libs.plugins.conventions.common.get().pluginId)
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.gradle.idea.ext)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.atlassian.sourcemap)
}