/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra
import kotlin.properties.ReadOnlyProperty

private const val ROOT_PROJECT_PROPERTY_PREFIX = "project"
private const val LOCAL_PROJECT_PROPERTY_PREFIX = "local"

private const val IS_MODULE_EXTRA_NAME = "isModule"

private fun Project.getProperty(prefix: String, name: String): String {
    val propertyName = "$prefix.$name"

    if (!hasProperty(propertyName)) {
        error("property $propertyName not found in project `${path}`")
    }

    return property(propertyName).toString()
}

///////////////////////////////////////////////////////////////////////////
// Root project
///////////////////////////////////////////////////////////////////////////

private fun rootProjectProperty(name: String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { thisRef, _ ->
        thisRef.rootProject.getProperty(ROOT_PROJECT_PROPERTY_PREFIX, name)
    }
}

val Project.projectGroup by rootProjectProperty("group")
val Project.projectWebsite by rootProjectProperty("website")
val Project.projectLicenseName by rootProjectProperty("license.name")
val Project.projectLicenseUrl by rootProjectProperty("license.url")
val Project.projectGitBase by rootProjectProperty("git.base")
val Project.projectGitUrl by rootProjectProperty("git.url")

val Project.projectDevId by rootProjectProperty("dev.id")
val Project.projectDevName by rootProjectProperty("dev.name")
val Project.projectDevUrl by rootProjectProperty("dev.url")

///////////////////////////////////////////////////////////////////////////
// Local project
///////////////////////////////////////////////////////////////////////////

val Project.isModule: Boolean
    get() = extra[IS_MODULE_EXTRA_NAME] == true

private fun localProjectProperty(name: String): ReadOnlyProperty<Project, String> {
    return ReadOnlyProperty { thisRef, _ ->
        thisRef.getProperty(LOCAL_PROJECT_PROPERTY_PREFIX, name)
    }
}

val Project.localName: String by localProjectProperty("name")
val Project.localDescription: String by localProjectProperty("description")