/**
 * Copyright (c) 2024 Maanrifa Bacar Ali.
 * Use of this source code is governed by the MIT license.
 */

plugins {
    org.jetbrains.kotlin.multiplatform
    id("conventions-common")
    id("conventions-publish")
}

kotlin {
    applyDefaultHierarchyTemplate()

    js(IR) {
        useEsModules()

        nodejs {
            testTask {
                enabled = false
            }
        }
    }
}


