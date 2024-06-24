import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

const val IS_MODULE_PROPERTY_NAME = "isModule"

val Project.isModule: Boolean
    get() = extra[IS_MODULE_PROPERTY_NAME] == true