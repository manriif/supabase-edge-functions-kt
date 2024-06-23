plugins {
    `kotlin-dsl`
}

fun NamedDomainObjectContainer<PluginDeclaration>.configure(provider: Provider<PluginDependency>) {
    named(provider.get().pluginId) {
        version = libs.versions.supabase.functions.get()
    }
}

gradlePlugin {
    plugins {
        configure(libs.plugins.conventions.common)
        configure(libs.plugins.conventions.kmp)
        configure(libs.plugins.conventions.publishing)
    }
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}