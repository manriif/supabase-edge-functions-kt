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

        val url = "https://github.com/manriif/supabase-functions-kt/tree/dev/$path/src"

        logger.error("isModule=${project.isModule}, path=$path, url=$url")

        documentedVisibilities = setOf(DokkaConfiguration.Visibility.PUBLIC)
        noStdlibLink = true

        sourceLink {
            localDirectory = projectDir.resolve("src")
            remoteUrl = URI(url).toURL()
            remoteLineSuffix = "#L"
        }
    }
}