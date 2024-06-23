package io.github.manriif.supabase.functions.serve.stacktrace

import com.atlassian.sourcemap.Mapping
import com.atlassian.sourcemap.ReadableSourceMap
import com.atlassian.sourcemap.ReadableSourceMapImpl
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logger
import java.io.File

internal class StackTraceProcessor(
    private val supabaseDirPath: String,
    private val compiledSourceDir: DirectoryProperty,
    private val sourceMapStrategy: StackTraceSourceMapStrategy,
    @Suppress("unused") private val logger: Logger
) {

    private var sourceMaps: Map<String, SourceMapEntry> = createSourceMapEntryMap()

    ///////////////////////////////////////////////////////////////////////////
    // Source Map
    ///////////////////////////////////////////////////////////////////////////

    private fun createSourceMapEntryMap(): Map<String, SourceMapEntry> {
        if (!sourceMapStrategy.kotlin) {
            return emptyMap()
        }

        return compiledSourceDir.get().asFileTree.matching {
            include { element ->
                !element.isDirectory && element.name.endsWith(".mjs.map")
            }
        }.associate { sourceFile ->
            sourceFile.nameWithoutExtension to SourceMapEntry(
                sourceMap = ReadableSourceMapImpl.fromSource(sourceFile.reader()),
                mapFile = sourceFile
            )
        }
    }

    private fun ReadableSourceMap.resolve(lineNumber: Int, column: Int): Pair<Mapping, Int>? {
        val mapping = getMapping(lineNumber, column)

        if (mapping != null) {
            return mapping to 0
        }

        // Most of the time lineNumber -1 and column -1 is resolved. Why : ?
        return getMapping(lineNumber - 1, column - 1)?.let { it to 1 }
    }

    fun reloadSourceMaps() {
        sourceMaps = createSourceMapEntryMap()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Stack trace
    ///////////////////////////////////////////////////////////////////////////

    private fun String.replaceFilePath(path: String): String {
        return kotlin.runCatching {
            replaceRange(
                startIndex = indexOf('(') + 1,
                endIndex = indexOf(')'),
                replacement = path
            )
        }.getOrElse {
            this
        }
    }

    private fun unknownSource(trace: String): String {
        return trace.replaceFilePath("Unknown Source")
    }

    private fun resolveJsSourceFile(trace: String): String {
        if (!sourceMapStrategy.js) {
            return unknownSource(trace)
        }

        val remoteFilePath = kotlin.runCatching {
            trace.substring(
                startIndex = trace.indexOf('(') + 1,
                endIndex = trace.indexOf(')')
            )
        }.getOrElse {
            return trace
        }

        val localFilePath = if (remoteFilePath.startsWith("file:///home/deno")) {
            // old supabase behaviour
            remoteFilePath.replace("file:///home/deno", supabaseDirPath)
        } else {
            // new supabase behaviour
            remoteFilePath.replace("file:///", "/")
        }

        val localFile = File(localFilePath.substringBefore(":"))

        if (!localFile.exists()) {
            return unknownSource(trace)
        }

        return trace.replaceFilePath(localFilePath)
    }

    fun resolveStackTrace(trace: String): String {
        if (!sourceMapStrategy.kotlin) {
            return resolveJsSourceFile(trace)
        }

        if (trace.trim().startsWith("at ") && !trace.endsWith(')')) {
            val formattedTrace = trace.replace("at ", "at (") + ')'
            return doResolveStackTrace(formattedTrace)
        }

        return doResolveStackTrace(trace)
    }

    private fun doResolveStackTrace(trace: String): String {

        val fileNameWithCursorLocation = kotlin.runCatching {
            trace.substring(
                startIndex = trace.lastIndexOf('/') + 1,
                endIndex = trace.lastIndexOf(')')
            )
        }.getOrElse {
            return resolveJsSourceFile(trace)
        }

        val (fileName, lineNumber, column) = fileNameWithCursorLocation.split(':')
        val entry = sourceMaps[fileName] ?: return resolveJsSourceFile(trace)

        val (mapping, offset) = kotlin.runCatching {
            entry.sourceMap.resolve(lineNumber.toInt(), column.toInt())
        }.getOrNull() ?: return resolveJsSourceFile(trace)

        val mapFileDirectory = entry.mapFile.parentFile
        val sourceFile = File(mapping.sourceFileName)
        val resolvedFile = mapFileDirectory.resolve(sourceFile)
        val sourceLine = mapping.sourceLine + offset

        if (!resolvedFile.exists()) {
            return when {
                sourceMapStrategy.js -> resolveJsSourceFile(trace)
                resolvedFile.name.isBlank() -> unknownSource(trace)
                else -> trace.replaceFilePath("${resolvedFile.name}:${sourceLine}")
            }
        }

        val location = "${resolvedFile.canonicalPath}:${sourceLine}"

        return if (mapping.sourceSymbolName != null && mapping.sourceSymbolName != "null") {
            "at ${mapping.sourceSymbolName} ($location)"
        } else {
            trace.replaceFilePath(location)
        }
    }
}