plugins {
    id("conventions-common")
    org.jetbrains.kotlin.multiplatform
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


