package io.github.manriif.supabase.functions.task

import io.github.manriif.supabase.functions.KOTLIN_MAIN_FUNCTION_NAME
import io.github.manriif.supabase.functions.REQUEST_CONFIG_FILE_NAME
import io.github.manriif.supabase.functions.SUPABASE_FUNCTION_OUTPUT_DIR
import io.github.manriif.supabase.functions.SUPABASE_FUNCTION_TASK_GROUP
import io.github.manriif.supabase.functions.SupabaseFunctionExtension
import io.github.manriif.supabase.functions.kmp.JsDependency
import io.github.manriif.supabase.functions.kmp.jsDependencies
import io.github.manriif.supabase.functions.kmp.jsOutputName
import io.github.manriif.supabase.functions.serve.stacktrace.StackTraceSourceMapStrategy
import io.github.manriif.supabase.functions.util.orNone
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal const val TASK_PREFIX = "supabaseFunction"
internal const val TASK_GENERATE_ENVIRONMENT_TEMPLATE = "${TASK_PREFIX}CopyKotlin%s"
internal const val TASK_GENERATE_DEVELOPMENT_ENVIRONMENT = "${TASK_PREFIX}CopyKotlinDevelopment"
internal const val TASK_GENERATE_PRODUCTION_ENVIRONMENT = "${TASK_PREFIX}CopyKotlinProduction"

internal const val TASK_COPY_JS = "${TASK_PREFIX}CopyJs"

internal const val TASK_GENERATE_BRIDGE = "${TASK_PREFIX}GenerateKotlinBridge"
internal const val TASK_GENERATE_IMPORT_MAP = "${TASK_PREFIX}GenerateImportMap"

internal const val TASK_AGGREGATE_IMPORT_MAP = "${TASK_PREFIX}AggregateImportMap"

internal const val TASK_UPDATE_GITIGNORE = "${TASK_PREFIX}UpdateGitignore"

internal const val TASK_FUNCTION_DEPLOY = "${TASK_PREFIX}Deploy"
internal const val TASK_FUNCTION_SERVE = "${TASK_PREFIX}Serve"

internal fun Project.configurePluginTasks(
    extension: SupabaseFunctionExtension,
    kmpExtension: KotlinMultiplatformExtension
) {
    val jsDependenciesProvider = jsDependencies()

    registerGenerateBridgeTask(extension, kmpExtension)
    registerCopyJsTask(extension, jsDependenciesProvider)
    registerCopyKotlinTask(extension, "development")
    registerCopyKotlinTask(extension, "production")
    registerServeTask(extension)
    registerDeployTask(extension)
    registerUpdateGitignoreTask(extension)

    val importMapsDir = rootProject.layout.buildDirectory.dir(
        "${SUPABASE_FUNCTION_OUTPUT_DIR}/importMaps"
    )

    registerAggregateImportMapTask(extension, importMapsDir)
    registerGenerateImportMapTask(extension, jsDependenciesProvider, importMapsDir)
}

private fun Project.registerGenerateBridgeTask(
    extension: SupabaseFunctionExtension,
    kmpExtension: KotlinMultiplatformExtension
) {
    val sourceSet = kmpExtension.sourceSets.findByName("jsMain") ?: return

    val outputDir = layout.buildDirectory
        .dir("generated/$SUPABASE_FUNCTION_OUTPUT_DIR/${sourceSet.name}/src")

    sourceSet.kotlin.srcDir(outputDir)

    tasks.register<SupabaseFunctionGenerateKotlinBridgeTask>(TASK_GENERATE_BRIDGE) {
        group = SUPABASE_FUNCTION_TASK_GROUP

        description = "Generate a kotlin function that acts as a bridge between " +
                "the `Deno.serve` and the kotlin main function."

        supabaseDir.convention(extension.supabaseDir)
        generatedSourceOutputDir.convention(outputDir)
        packageName.convention(extension.packageName)
        jsOutputName.convention(jsOutputName(kmpExtension))
        functionName.convention(extension.functionName)
        mainFunctionName.convention(KOTLIN_MAIN_FUNCTION_NAME)
    }
}

private fun Project.registerCopyJsTask(
    extension: SupabaseFunctionExtension,
    jsDependenciesProvider: Provider<Collection<JsDependency>>
) {
    tasks.register<SupabaseFunctionCopyJsTask>(TASK_COPY_JS) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Copy JS sources into supabase function directory."

        jsDependencies.convention(jsDependenciesProvider)
        supabaseDir.convention(extension.supabaseDir)
        functionName.convention(extension.functionName)
    }
}

private fun Project.registerCopyKotlinTask(
    extension: SupabaseFunctionExtension,
    environment: String,
) {
    val uppercaseEnvironment = environment.uppercaseFirstChar()
    val compileSyncTaskName = "js${uppercaseEnvironment}LibraryCompileSync"

    if (tasks.names.none { it == compileSyncTaskName }) {
        return
    }

    val taskName = TASK_GENERATE_ENVIRONMENT_TEMPLATE.format(uppercaseEnvironment)

    tasks.register<SupabaseFunctionCopyKotlinTask>(taskName) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Copy Kotlin generated sources into supabase function directory."

        compiledSourceDir.convention(
            layout.buildDirectory.dir("compileSync/js/main/${environment}Library/kotlin")
        )

        supabaseDir.convention(extension.supabaseDir)
        functionName.convention(extension.functionName)

        dependsOn(compileSyncTaskName)
        dependsOn(TASK_COPY_JS)
    }
}

private fun Project.registerDeployTask(extension: SupabaseFunctionExtension) {
    tasks.register<SupabaseFunctionDeployTask>(TASK_FUNCTION_DEPLOY) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Deploy function to remote project."

        supabaseDir.convention(extension.supabaseDir)
        functionName.convention(extension.functionName)
        projectRef.convention(extension.projectRef)
        verifyJwt.convention(extension.verifyJwt)
        importMap.convention(extension.importMap)

        dependsOn(TASK_GENERATE_PRODUCTION_ENVIRONMENT)
        dependsOn(TASK_AGGREGATE_IMPORT_MAP)
    }
}

private fun Project.registerServeTask(extension: SupabaseFunctionExtension) {
    tasks.register<SupabaseFunctionServeTask>(TASK_FUNCTION_SERVE) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Serve function locally."

        compiledSourceDir.convention(
            layout.buildDirectory.dir("compileSync/js/main/developmentLibrary/kotlin")
        )

        requestConfigFile.convention(
            layout.projectDirectory.file(REQUEST_CONFIG_FILE_NAME).orNone(project)
        )

        requestOutputDir.convention(layout.buildDirectory.dir("tmp/$SUPABASE_FUNCTION_OUTPUT_DIR"))
        rootProjectDir.convention(rootProject.layout.projectDirectory)
        projectDir.convention(layout.projectDirectory)
        supabaseDir.convention(extension.supabaseDir)
        functionName.convention(extension.functionName)
        verifyJwt.convention(extension.verifyJwt)
        importMap.convention(extension.importMap)
        stackTraceSourceMapStrategy.convention(StackTraceSourceMapStrategy.JsOnly)
        envFile.convention(extension.envFile.orNone())

        dependsOn(TASK_GENERATE_DEVELOPMENT_ENVIRONMENT)
        dependsOn(TASK_AGGREGATE_IMPORT_MAP)
    }
}

private fun Project.registerUpdateGitignoreTask(extension: SupabaseFunctionExtension) {
    val task = tasks.register<SupabaseFunctionUpdateGitignoreTask>(TASK_UPDATE_GITIGNORE) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Update .gitignore files."

        supabaseDir.convention(extension.supabaseDir)
        functionName.convention(extension.functionName)
        importMapEntry.convention(true)
        indexEntry.convention(true)
    }

    rootProject.tasks.named("prepareKotlinBuildScriptModel") {
        dependsOn(task)
    }
}

private fun Project.registerAggregateImportMapTask(
    extension: SupabaseFunctionExtension,
    importMapsDir: Provider<Directory>,
) {
    if (rootProject.extra.has(TASK_AGGREGATE_IMPORT_MAP)) {
        return
    }

    val taskProvider = rootProject.tasks.register<SupabaseFunctionAggregateImportMapTask>(
        name = TASK_AGGREGATE_IMPORT_MAP
    ) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Aggregate functions import maps."

        this.importMapsDir.convention(importMapsDir)
        supabaseDir.convention(extension.supabaseDir)
    }

    rootProject.tasks.named("prepareKotlinBuildScriptModel") {
        dependsOn(taskProvider)
    }

    rootProject.extra[TASK_AGGREGATE_IMPORT_MAP] = taskProvider
}

private fun Project.registerGenerateImportMapTask(
    extension: SupabaseFunctionExtension,
    jsDependenciesProvider: Provider<Collection<JsDependency>>,
    importMapsDir: Provider<Directory>
) {
    if (!rootProject.extra.has(TASK_AGGREGATE_IMPORT_MAP)) {
        error("Aggregate task provider not registered")
    }
    val taskProvider = tasks.register<SupabaseFunctionGenerateImportMapTask>(
        name = TASK_GENERATE_IMPORT_MAP
    ) {
        group = SUPABASE_FUNCTION_TASK_GROUP
        description = "Generate import map."

        packageJsonDir.convention(layout.buildDirectory.dir("tmp/jsPublicPackageJson"))
        functionName.convention(extension.functionName)
        jsDependencies.convention(jsDependenciesProvider)
        this.importMapsDir.convention(importMapsDir)

        dependsOn("jsPublicPackageJson")
    }

    checkNotNull(rootProject.extra.get(TASK_AGGREGATE_IMPORT_MAP) as? TaskProvider<*>).configure {
        dependsOn(taskProvider)
    }
}