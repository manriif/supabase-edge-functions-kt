plugins {
    org.jetbrains.kotlin.multiplatform
    id("conventions-common")
}

description = property("module.description").toString()

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


