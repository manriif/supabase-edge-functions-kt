import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

plugins {
    org.jetbrains.dokka
}

tasks.withType<DokkaTaskPartial>().configureEach {
    suppressInheritedMembers = true

    dokkaSourceSets.configureEach {
        val path = if (!project.isModule) project.name else {
            "modules/${project.name.removePrefix("module-")}"
        }

        val url = "https://github.com/manriif/supabase-functions-kt/tree/main/$path/src"

        includes = project.layout.projectDirectory.files("MODULE.md")
        documentedVisibilities = setOf(DokkaConfiguration.Visibility.PUBLIC)
        noStdlibLink = true

        sourceLink {
            localDirectory = projectDir.resolve("src")
            remoteUrl = URI(url).toURL()
            remoteLineSuffix = "#L"
        }
    }
}