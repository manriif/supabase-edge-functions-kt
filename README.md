[example]: https://github.com/manriif/supabase-edge-functions-kt-example

# Supabase Edge Functions Kotlin

Build, serve and deploy Supabase Edge Functions with Kotlin and Gradle.

The project aims to bring the ability of writing and deploying Supabase Edge Functions using Kotlin
as primary programming language.

[![](https://img.shields.io/badge/Stability-experimental-orange)]()
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![IR](https://img.shields.io/badge/Kotlin%2FJS-IR_only-yellow)](https://kotl.in/jsirsupported)
[![API](https://img.shields.io/badge/API-dokka-green)]()
[![Maven Central](https://img.shields.io/maven-central/v/io.github.manriif.supabase-functions/github-plugin?label=MavenCentral&logo=apache-maven)](https://search.maven.org/artifact/org.jetbrains.dokka/io.github.manriif.supabase-functions)
[![Gradle Plugin](https://img.shields.io/gradle-plugin-portal/v/io.github.manriif.supabase-functions?label=Gradle&logo=gradle)](https://plugins.gradle.org/plugin/io.github.manriif.supabase-functions)
[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)
[![slack](https://img.shields.io/badge/slack-%23supabase--kt-purple.svg?logo=slack)](https://kotlinlang.slack.com/archives/C06QXPC7064)

## Get started

It is recommended to use your favorite IntelliJ based IDE such as IntelliJ IDEA or Android Studio.

Also, it is recommended to have one gradle subproject per function.
Inspiration on how to structure your gradle project can be found in the [example][example].

### Gradle setup

If you plan to write multiple functions, declare the plugin in the root build script:

```kotlin
// <root>/build.gradle.kts

plugins {
    id("io.github.manriif.supabase-functions") version "0.0.1" apply false
}
```

Apply the Gradle plugin in the build script of your project:

```kotlin
// <function>/build.gradle.kts

plugins {
    id("io.github.manriif.supabase-functions")
}

supabaseFunction {
    packageName = "org.example.function" // Required, package of the main function
    functionName = "my-function" // Optional, default to the project name
    supabaseDir = file("supabase") // Optional, default to <root>/supabase
    envFile = file(".env.local") // Optional, default to <supabaseDir>/.env.local
    projectRef = "supabase-project-ref" // Optional, no default value
    importMap = false // Optional, default to true
    verifyJwt = false // Optional, default to true
}
```

### Kotlin/JS setup

Apply the Kotlin Multiplatform plugin in the build script of your project:

```kotlin
// <function>/build.gradle.kts

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    js(IR) {
        binaries.library() // Required
        useEsModules() // Required
        nodejs() // Required
    }
}
```

### Main function

An [example][example] repository is available
to get you started faster.

The only requirement for the magic to work is to write an entry function that accepts a
single `org.w3c.fetch.Request` parameter and returns a `org.w3c.fetch.Response`.

The function can be marked with suspend modifier.

In any kotlin source file of your project (function):

```kotlin
// src/jsMain/kotlin/org/example/function/serve.kt

package org.example.function

suspend fun serve(request: Request): Response {
    return Response(body = "Hello, world !")
}
```

### Run

After a successful gradle sync and if you are using an IntelliJ based IDE, you will see new run configurations for your function.

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/manriif/supabase-edge-functions-kt/dev/docs/run_config_dark.png">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/manriif/supabase-edge-functions-kt/dev/docs/run_config_light.png">
  <img alt="Run configurations" src="https://raw.githubusercontent.com/manriif/supabase-edge-functions-kt/dev/docs/run_config_light.png">
</picture>

Run: 

- `<function-name> deploy` for deploying the function to the remote project.
- `<function-name> inspect` for inspecting the javascript code with Chrome DevTools.
- `<function-name> request` for verifying the function (send preconfigured request(s)).
- `<function-name> serve` for serving the function locally.

## Features

Belows the features offered by the plugin.

| Name                                  | ☑️  |
|---------------------------------------|-----|
| Write Kotlin code                     | ✅️  |
| Write Javascript code                 | ✅️  |
| NPM support                           | ✅️  |
| Multi-module support                  | ✅️  |
| Serve function                        | ✅️  |
| [Verify function](#automatic-request) | ✅️  |
| [Deploy function](#deployment)        | ✅️  |
| [Import map](#import-map)             | ✅️  |
| [Debugging](#debugging)               | 🚧️ |

## Modules

The project provides convenient modules which covers the needs related to the development of supabase
functions.

Available modules:

- [binding-deno](modules/binding-deno/MODULE.md)
- [fetch-json](modules/fetch-json/MODULE.md)

## Advanced usage

### Continuous build

The plugin provides first class support for Gradle continuous build and configuration cache.
This results in faster builds and an uninterrupted development workflow.

Serve related tasks (serve, request, inspect) will automatically reload after file changes
are detected by gradle.

### Main function

The plugin will, by default, generate a kotlin function that acts as a bridge between your main 
function and the Deno serve function. This also results in the generation of the function's `index.ts` 
file.

<details>
  <summary>Disable the bridge function</summary>

If, for some reasons you do not want this behaviour, you can simply disable the related task:

```kotlin
// <function>/build.gradle.kts

tasks {
    supabaseFunctionGenerateKotlinBridge {
        enabled = false
    }
}
```

It is then your responsibility to connect the two worlds.

</details>

<details>
  <summary>Change the main function name</summary>

By default the main function name is `serve`.
If this name struggles seducing you, you can change it by editing your function level build script.
Let's say you want your main function to be named `handleRequest`:

```kotlin
// <function>/build.gradle.kts

tasks {
    supabaseFunctionGenerateKotlinBridge {
        mainFunctionName = "handleRequest"
    }
}
```

After that, your main function should looks like: 

```kotlin
// src/jsMain/kotlin/org/example/function/serve.kt

package org.example.function

suspend fun handleRequest(request: Request): Response {
    return Response(body = "Hello, world !")
}
```
</details>

### JavaScript

You can embed local JavaScript sources from a subproject, other subproject or even through a
composite build project. 
The JavaScript source code must be placed in the `src/<source-set>/js` of the target project.
There is no restriction regarding the kotlin source-set. It can be `commonMain`, `jsMain`, both, 
or any other source-set that the `jsMain` source-set depends on. 
This gives you complete flexibility on how you structure your modules.

<details>
  <summary>JavaScript rules</summary>

Working with JavaScript must be done according to a few rules:

- There cannot be the same file (same name and same path relative to the `js` directory) within two
different source-sets of the same project (module).

- There is a magical keyword `module` which must be used to refer to the local project when importing. 
This keyword ensures proper resolution of js files among all included projects and depending on
the call site.

</details>

<details>
<summary>Import an exported Kotlin function into a JavaScript file</summary>

```javascript
// src/jsMain/js/bonjour.js

import { howAreYou } from 'module'; // howAreYou is an exported Kotlin function

export function bonjour(name) {
    return "Bonjour " + name + ", " + howAreYou();
}
```

More explanation on how to consume Kotlin code from JavaScript [here](https://kotlinlang.org/docs/js-to-kotlin-interop.html).
</details>

<details>
<summary>Import an exported JavaScript function into a Kotlin file</summary>

```kotlin
// src/jsMain/kotlin/org/example/function/Bonjour.kt
@file:JsModule("module/bonjour.js") // full path to the js file relative to the js directory after module/

package org.example.function

external fun bonjour(name: String): String
```

More explanation on how to consume Javascript code in Kotlin [here](https://kotlinlang.org/docs/js-interop.html).
</details>

### Import map

The plugin automatically configures a single import_map.json file which take cares of NPM dependencies 
and local js sources files. The file is generated under the `supabase/functions` directory and aggregates
all the single `import_map.json` files of each individual function.

You can specify this import_map.json file in your favorite JavaScript IDE and it's Deno configuration.

<details>
  <summary>Generate the import_map.json</summary>

The task responsible for generating the file is triggered after a successful project sync but you can manually
trigger it by running:

`./gradlew :supabaseFunctionAggregateImportMap`

</details>

<details>
<summary>Modify the generated file</summary>

You can add entries to the generated `import_map.json` by writing your own
`import_map_template.json` file under the `supabase/functions` directory. 
This file will take precedence over any other `import_map.json`, 
meaning that your entries will not be overwritten. This allows you to force a specific version
for an NPM package.

Do not directly modify the generated `import_map.json` as it will be overwritten.

</details>

<details>
  <summary>Disable the feature</summary>

If, for some reasons you want to manually manage the import map, you can disable the related task(s):

<details>
  <summary>For a single function</summary>

```kotlin
// <function>/build.gradle.kts

supabaseFunction {
    importMap = false
}

tasks {
    supabaseFunctionGenerateImportMap {
        enabled = false
    }
}
```

</details>

<details>
  <summary>For all functions</summary>

```kotlin
// <root>/build.gradle.kts

tasks.withType<SupabaseFunctionAggregateImportMapTask> {
    enabled = false
}
```
</details>

Keep in mind that you should manually create and populate necessary import_map.json file(s).

</details>

### Automatic request

With the aim of limiting tools and speeding up function development time, the plugin provides the
ability to automatically send preconfigured requests to the function endpoint.

<details>
  <summary>Configuration</summary>

Under the project (function) directory, create a `request-config.json` file:

```json5
{
    "headers": { // Optional, defaults headers for all requests
        "authorization": "Bearer ${ANON_KEY}" // ${ANON_KEY} will be resolved at runtime. You can use
                                              // any variable printed by the `supabase status` command
    },
    "requests": [ // Required, list of requests that should be performed
        {
            "name": "Response body should be 'Hello, world !!'", // Required, the name of the request
            "method": "get", // Required, the http method: get, post, put, patch, option, delete, etc
            "headers": { // Optional, request headers
                "authorization": "Bearer ${SERVICE_ROLE_KEY}" // Override default
            },
            "parameters": {  // Optional, URI parameters
                "name": "Paul"
            },
            "type": "plain", // Optional, the type of the request: `plain`, `json` or `file` 
            "body": "",      // Conditional, body of the request, required if a type is specified
            "body": "John",  // Body of the request for `plain` type, must be a valid string
            "body": {        // Body of the request for `json` type, must be a valid json object
                "from": 0,
                "to": 10
            }, 
            "body": "./file-to-upload.png", // Body of the request for `file` type. File path must be 
                                           // relative to the project directory
            "validation": {   // Optional, used for assertions
                "status": 400, // Optional, the expected response status code, default to 200
                "type": "plain",// Optional, the expected response type: `plain`, `json` or `file` 
                "body": "", // Conditional, expected response body, required if a type is specified
                "body": "Hello, world !", // Expected body for `plain` type
                "body": { // Expected body for `json` type
                    "cities": [
                        {
                            name: "Bordeaux",
                            country: "France"
                        }
                    ]
                },
                "body": "./expected-body.txt" // Body of the request for `file` type. File path must 
                                              // be relative to the project directory
            }
        }
    ]
}
```

You can further customize the behaviour of the serve task for auto request: 

```kotlin
// <function>/build.gradle.kts

tasks {
    supabaseFunctionServe {
        autoRequest {
            logResponse = true // Print request and response details
            logStatus = true // Print available supabase variables
        }
    }
}
```

It is also possible to pass gradle parameters for altering the behaviour and avoid modifying 
gradle script: 

- pass `-PsupFunLogResponse" for printing request and response details
- pass `-PsupFunLogStatus" for printing available supabase variables

And:

`./gradlew :functions:hello-world:supabaseFunctionServe -PsupFunLogResponse -PsubFunLogStatus`

</details>

<details>
  <summary>Continuous build</summary>

When using continuous build, requests are sent after files changes are detected by gradle.
However, depending on your function size, the requests may be sent too quickly and not allow enough 
time for the supabase hot loader to process the changes. This can lead to race condition issues and
results in edge function invocation error.

To solve the problem, it is possible to delay the requests sending:

```kotlin
// <function>/build.gradle.kts

tasks {
    supabaseFunctionServe {
        autoRequest {
            sendRequestOnCodeChangeDelay = 1000 // milliseconds, default to 500.
        }
    }
}
```

Note that changes to the `request-config.json` file will also trigger live reload, which let you edit 
it while the task is running.

</details>

### Debugging

#### Logging

#### Inspection

#### Kotlin code

### Run configurations

Run configurations, for each function, are automatically created for IntelliJ based IDEs.

<details>
<summary>Configure</summary>

You can choose which run configuration to generate:

```kotlin
// <function>/build.gradle.kts

supabaseFunction {
    runConfiguration {
        deploy = false           // Enable the deploy run configuration, true by default

        serve {                  // Serve run configuration
            enabled = false      // Enable the configuration, true by default
            continuous = false   // continuous build enabled by default, true by default
        }

        inspect {                // Inspect run configuration
            enabled = false      // Enable the configuration, true by default
            continuous = false   // continuous build enabled by default, true by default
        }

        request {                // Request run configuration
            enabled = false      // Enable the configuration, true by default
            continuous = false   // continuous build enabled by default, true by default
        }
    }
}
```

</details>

### Deployment

Function can be deployed to the remote project by running the `<function> deploy` run configuration or by running 
the gradle command:

`./gradlew functions:hello-world:supabaseFunctionDeploy`

Before deploying the function, make sure you have correctly [linked](https://supabase.com/docs/reference/cli/supabase-link) 
the remote project.

### Gitignore

It is generally a good practice not to import files that are generated to VCS. Thus, and by its nature,
the plugin provides a task for creating or updating necessary `.gitignore` files. Existing `.gitignore `
files will not be overwritten but completed with missing entries.

<details>
<summary>Disable or edit the task</summary>

You can disable the task or change its behaviour at the project level:

```kotlin
// <function>/build.gradle.kts

tasks {
    supabaseFunctionServe {
        enabled = false // Disable the task
        importMapEntry = false // Prevent the task from adding the import_map.json to .gitignore
                               // This could be necessary if you manually configured the import map
        indexEntry = false // Prevent the task from adding the index.ts to .gitignore
                           // This could be necessary if you manually created the index.ts file
    }
}
```

</details>

## Limitations

Following limitations applies:

- Kotlin versions before 2.0 are not supported
- browser JS subtarged is not supported
- `per-file` and `whole-program` JS IR [output granularity](https://kotlinlang.org/docs/js-ir-compiler.html#output-mode) are not supported.
- Depending on a Kotlin library that uses require() may lead to runtime error