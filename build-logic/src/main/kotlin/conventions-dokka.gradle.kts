import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

plugins {
    org.jetbrains.dokka
}

tasks.withType<DokkaTaskPartial>().configureEach {
    suppressInheritedMembers = true

    dokkaSourceSets.configureEach {
        documentedVisibilities = setOf(DokkaConfiguration.Visibility.PUBLIC)
        noStdlibLink = true

        val (_, kind) = project.path.split(":")
        val module = project.projectDir.name
        val url = "https://github.com/manriif/supabase-functions-kt/tree/dev/$kind/$module/src"

        sourceLink {
            localDirectory = projectDir.resolve("src")
            remoteUrl = URI(url).toURL()
            remoteLineSuffix = "#L"
        }
    }
}