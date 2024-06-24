/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

const val IS_MODULE_PROPERTY_NAME = "isModule"

val Project.isModule: Boolean
    get() = extra[IS_MODULE_PROPERTY_NAME] == true