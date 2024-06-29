package io.github.manriif.supabase.functions.util

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.withType

internal inline fun <reified T : Task> Project.postponeErrorOnTaskInvocation(errorMessage: String) {
    tasks.withType<T>().configureEach {
        doFirst {
            error(errorMessage)
        }
    }
}