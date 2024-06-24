plugins {
    `kotlin-dsl`
    id(libs.plugins.conventions.common.get().pluginId)
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.gradle.idea.ext)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.atlassian.sourcemap)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}