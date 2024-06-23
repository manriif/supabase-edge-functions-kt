plugins {
    id("conventions-common")
    org.jetbrains.kotlin.multiplatform
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


