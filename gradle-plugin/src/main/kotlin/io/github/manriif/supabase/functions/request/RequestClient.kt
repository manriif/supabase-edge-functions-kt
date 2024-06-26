/**
 * Copyright (c) 2024 Maanrifa Bacar Ali
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.manriif.supabase.functions.request

import io.github.manriif.supabase.functions.error.SupabaseFunctionRequestConfigException
import io.github.manriif.supabase.functions.supabase.SUPABASE_ANON_KEY
import io.github.manriif.supabase.functions.supabase.SUPABASE_API_URL
import io.github.manriif.supabase.functions.supabase.SUPABASE_SERVICE_ROLE_KEY
import io.github.manriif.supabase.functions.supabase.supabaseCommand
import io.github.manriif.supabase.functions.util.Color
import io.github.manriif.supabase.functions.util.colored
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.process.internal.ExecActionFactory
import org.jetbrains.kotlin.com.google.gson.Gson
import org.jetbrains.kotlin.com.google.gson.GsonBuilder
import org.jetbrains.kotlin.com.google.gson.JsonParser
import org.jetbrains.kotlin.com.google.gson.JsonPrimitive
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.timerTask
import kotlin.math.max

private val PlaceholderRegex = """\$\{(.*)\}""".toRegex()

internal class RequestClient(
    private val rootProjectDir: DirectoryProperty,
    private val projectDir: DirectoryProperty,
    private val requestOutputDir: DirectoryProperty,
    private val requestConfigFile: RegularFileProperty,
    private val functionName: Provider<String>,
    private val logger: Logger,
    private val options: RequestClientOptions
) {

    private val properties = mutableMapOf<String, String>()
    private var initialized = false

    private val httpClient by lazy { HttpClient.newHttpClient() }
    private val timer by lazy { Timer("Supabase Function") }
    private val gson by lazy { createGson() }

    private var requestConfig: RequestConfig? = null
    private var functionBaseUrl: String? = null
    private var currentTask: TimerTask? = null

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Initializes the request client.
     */
    fun initialize(execActionFactory: ExecActionFactory): Boolean {
        val result = doInitialization(execActionFactory)

        if (!result) {
            logger.error(
                "Automatic request sending is disabled, " +
                        "please ensure that Supabase CLI is up-to-date and running."
            )
        }

        return result
    }

    private fun doInitialization(execActionFactory: ExecActionFactory): Boolean {
        check(!initialized) { "Request processor already initialized" }

        val mainSteam = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()

        val action = execActionFactory.newExecAction().apply {
            isIgnoreExitValue = true
            workingDir = rootProjectDir.get().asFile
            standardOutput = mainSteam
            errorOutput = errorStream

            commandLine(supabaseCommand(), "status", "--output", "json")
        }

        val result = action.execute()
        val content = mainSteam.toString(Charsets.UTF_8)

        if (result.exitValue != 0) {
            logger.error("Failed to resolve supabase local development settings: $errorStream")
            return false
        }

        if (!parseStatusCommandResult(content)) {
            return false
        }

        if (options.logStatus) {
            printStatus(content)
        }

        configureFunctionUrl()
        reloadConfigFile()

        initialized = true
        return true
    }

    private fun parseStatusCommandResult(content: String): Boolean {
        val json = kotlin.runCatching {
            val jsonString = content.substring(
                startIndex = content.indexOf('{'),
                endIndex = content.lastIndexOf('}') + 1
            )

            JsonParser.parseString(jsonString).asJsonObject
        }.getOrElse { error ->
            logger.error("Failed to parse supabase status response: ${error.stackTraceToString()}")
            return false
        }

        json.keySet().forEach { key ->
            properties[key] = json.get(key).asString
        }

        val required = listOf(SUPABASE_API_URL, SUPABASE_ANON_KEY, SUPABASE_SERVICE_ROLE_KEY)

        return properties.keys.containsAll(required).also { value ->
            if (!value) {
                logger.error(
                    "Some required variables were not returned by " +
                            "`supabase status` command. Restarting supabase local development " +
                            "stack may solve the issue."
                )
            }
        }
    }

    private fun configureFunctionUrl() {
        functionBaseUrl = properties[SUPABASE_API_URL]?.removeSuffix("/") ?: return
        functionBaseUrl += "/functions/v1/${functionName.get()}"
    }

    private fun printStatus(content: String) {
        val disabledServices = content.substringBefore('\n')

        val allProperties = properties.entries.joinToString("\n") { (key, value) ->
            "$key = $value"
        }

        val message = """
            |$disabledServices
            |$allProperties
        """.trimMargin()

        logger.lifecycle(message)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Config
    ///////////////////////////////////////////////////////////////////////////

    private fun createGson(): Gson {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    fun reloadConfigFile() {
        if (!requestConfigFile.isPresent) {
            return
        }

        val configFile = requestConfigFile.get().asFile

        if (!configFile.exists()) {
            return
        }

        requestConfig = try {
            gson.fromJson(configFile.reader(), RequestConfig::class.java)
        } catch (exception: Throwable) {
            return logger.error("Failed to load request-config.json: ${exception.message}")
        }

        try {
            requestConfig?.checkValidity(projectDir)
        } catch (throwable: Throwable) {
            throw SupabaseFunctionRequestConfigException(
                "Invalid request-config.json configuration",
                throwable
            )
        }
    }

    private fun String.fillPlaceholders(): String {
        return replace(PlaceholderRegex) { match ->
            val value = match.groupValues.component2()

            try {
                requireNotNull(properties[value.uppercase()])
            } catch (throwable: Throwable) {
                throw SupabaseFunctionRequestConfigException(
                    message = "Property `${value}` does not exists.",
                    cause = throwable
                )
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Request
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sends all requests asynchronously.
     */
    fun sendRequestsAsync(onFinish: (() -> Unit)? = null) {
        currentTask?.cancel()

        currentTask = timerTask {
            sendRequests()
            onFinish?.invoke()
        }

        timer.schedule(currentTask, options.requestDelay)
    }

    /**
     * Sends all requests and returns true if no request failed.
     */
    private fun sendRequests(): Boolean {
        val baseUrl = functionBaseUrl ?: return true
        val config = requestConfig ?: return true

        if (config.requests.isEmpty()) {
            return true
        }

        val entries = config.requests.map { request ->
            request to sendRequest(config, request, baseUrl)
        }

        return entries.map valid@{ (request, response) ->
            if (options.logResponse && response != null) {
                logResponse(request, response)
            }

            validateResponse(request, response)
        }.all { it }
    }

    /**
     * Sends request and returns true it succeeded.
     */
    private fun sendRequest(
        config: RequestConfig,
        request: Request,
        baseUrl: String
    ): HttpResponse<ValidationResult>? {
        val queryString = if (request.parameters.isEmpty()) "" else {
            "?" + request.parameters.entries.joinToString("&") { (key, value) ->
                "$key=$value"
            }
        }

        val uri = URI(baseUrl + queryString)

        val httpRequestBuilder = HttpRequest
            .newBuilder(uri)
            .version(
                when (request.http ?: config.http) {
                    RequestOptions.HttpVersion.HTTP_2 -> HttpClient.Version.HTTP_2
                    RequestOptions.HttpVersion.HTTP_1_1, null -> HttpClient.Version.HTTP_1_1
                }

            )
            .method(request.method!!.uppercase(), request.bodyPublisher())

        val headers = mutableMapOf<String, String>().apply {
            putAll(config.headers)
            putAll(request.headers)
        }

        if (request.type == Request.Type.Json) {
            if (headers.keys.any { it.lowercase() == "content-type" }) {
                headers["Content-Type"] = "application/json"
            }
        }

        (request.timeout ?: config.timeout)?.let { timeout ->
            httpRequestBuilder.timeout(Duration.ofMillis(timeout))
        }

        headers.forEach { (name, value) ->
            httpRequestBuilder.setHeader(name, value.fillPlaceholders())
        }

        return try {
            httpClient.send(
                httpRequestBuilder.build(),
                request.bodyHandler()
            )
        } catch (throwable: Throwable) {
            logger.error(
                """
                    |An error occurred while sending request `${request.name}`:
                    |${throwable.stackTraceToString()}
                """.trimMargin()
            )

            null
        }
    }

    private fun Request.bodyPublisher(): HttpRequest.BodyPublisher? {
        return when (type) {
            Request.Type.Json -> body?.asJsonObject?.toString()?.let { content ->
                HttpRequest.BodyPublishers.ofString(content)
            }

            Request.Type.File -> resolvedFile?.let { file ->
                HttpRequest.BodyPublishers.ofFile(file.toPath())
            }

            Request.Type.Plain -> body?.asString?.let { content ->
                HttpRequest.BodyPublishers.ofString(content)
            }

            null -> HttpRequest.BodyPublishers.noBody()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Response
    ///////////////////////////////////////////////////////////////////////////

    private fun HttpHeaders.joinToString() = map().entries.joinToString("\n\t") { (key, values) ->
        "$key: ${values.joinToString()}"
    }

    private fun logResponse(request: Request, response: HttpResponse<ValidationResult>) {
        val message = """
            |
            |[${request.name}]
            |Type: ${request.type ?: "<not specified>"}
            |[Request]
            |Date: ${Date()}
            |Endpoint: ${response.uri()}
            |Method: ${request.method?.uppercase()}
            |Headers: [
            |    ${response.request().headers().joinToString()}
            |]
            |[Response]
            |Status: ${response.statusCode()}
            |Headers: [
            |    ${response.headers().joinToString()}
            |]
            |Body: ${response.body().toString(gson)}
        """.trimMargin()

        logger.lifecycle(message.colored(Color.Yellow))
    }

    private fun Request.bodyHandler(): HttpResponse.BodyHandler<ValidationResult> {
        return when (validation?.type) {
            RequestValidation.Type.Plain -> validationResultBodyHandler(
                subscribe = { HttpResponse.BodySubscribers.ofString(Charsets.UTF_8) },
                transform = ValidationResult::Plain
            )

            RequestValidation.Type.Json -> validationResultBodyHandler(
                subscribe = { HttpResponse.BodySubscribers.ofString(Charsets.UTF_8) },
                transform = { content ->
                    val json = kotlin.runCatching {
                        JsonParser.parseString(content).asJsonObject
                    }.getOrElse {
                        JsonPrimitive(content)
                    }

                    ValidationResult.Json(json)
                }
            )

            RequestValidation.Type.File -> validationResultBodyHandler(
                subscribe = {
                    val outputFile = requestOutputDir
                        .file("${functionName.get()}/${name}_result")
                        .get().asFile
                    outputFile.ensureParentDirsCreated()
                    HttpResponse.BodySubscribers.ofFile(outputFile.toPath())
                },
                transform = { ValidationResult.File(it.toFile()) }
            )

            null -> validationResultBodyHandler(
                subscribe = { HttpResponse.BodySubscribers.discarding() },
                transform = { ValidationResult.None }
            )
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Validation
    ///////////////////////////////////////////////////////////////////////////

    private fun validationContent(
        request: Request,
        state: String,
        stateColor: Color
    ): String = """
        |
        |[${"${request.name}".colored(Color.Magenta, bright = true)}]
        |State: ${state.colored(stateColor, bright = true)}
    """.trimMargin()

    private fun errorValidationContent(
        request: Request,
        reason: String,
        expected: Any?,
        actual: Any?
    ): String {
        return """
            |${validationContent(request, "Failed", Color.Red)}
            |Reason: ${reason.colored(Color.Red, bright = true)}
            |Expected: $expected
            |Actual: $actual
        """.trimMargin()
    }

    private fun validateResponse(
        request: Request,
        response: HttpResponse<ValidationResult>?
    ): Boolean {
        if (response == null) {
            logger.lifecycle(validationContent(request, "Failed", Color.Red))
            return false
        }

        if (request.validation == null) {
            logger.lifecycle(validationContent(request, "No validation", Color.Blue))
            return true
        }

        if (request.validation.status != response.statusCode()) {
            logger.lifecycle(
                errorValidationContent(
                    request = request,
                    reason = "Actual status code differs from expected",
                    expected = request.validation.status,
                    actual = response.statusCode()
                )
            )

            return false
        }

        val result = response.body()

        if (result.isValid(request.validation)) {
            logger.lifecycle(validationContent(request, "Success", Color.Green))
            return true
        }

        logger.lifecycle(
            errorValidationContent(
                request = request,
                reason = "Actual content differs from expected",
                expected = result.expected(request.validation, gson),
                actual = result.toString(gson)
            )
        )

        return false
    }

    private fun <T> validationResultBodyHandler(
        subscribe: (HttpResponse.ResponseInfo) -> HttpResponse.BodySubscriber<T>,
        transform: (T) -> ValidationResult,
    ): HttpResponse.BodyHandler<ValidationResult> {
        return HttpResponse.BodyHandler<ValidationResult> { responseInfo ->
            HttpResponse.BodySubscribers.mapping(subscribe(responseInfo), transform)
        }
    }
}