import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType

plugins {
    io.gitlab.arturbosch.detekt
}

val detektDir = rootProject.layout.projectDirectory.dir("detekt")
val detektReportsDir = detektDir.dir("reports")

detekt {
    buildUponDefaultConfig = true
    ignoreFailures = true
    baseline = detektDir.file("baseline.xml").asFile

    config.setFrom(detektDir.file("config.yml"))
}

tasks {
    withType<Detekt>().configureEach {
        jvmTarget = libs.versions.jvm.target.get()
        basePath = rootDir.absolutePath

        reports {
            listOf(html, xml, sarif, md).forEach { report ->
                report.required = true

                report.outputLocation = detektReportsDir
                    .file("${report.type.reportId}/${project.name}.${report.type.extension}")
            }
        }
    }

    withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = libs.versions.jvm.target.get()
    }
}

// https://detekt.dev/docs/gettingstarted/gradle#disabling-detekt-from-the-check-task
afterEvaluate {
    tasks.named("check") {
        setDependsOn(dependsOn.filterNot {
            it is TaskProvider<*> && it.name.contains("detekt")
        })
    }
}