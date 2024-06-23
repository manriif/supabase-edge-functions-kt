import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    org.jetbrains.dokka
}

tasks.withType<DokkaTaskPartial> {
    dokkaSourceSets.configureEach {
        includes = files("Module.md")
    }
}