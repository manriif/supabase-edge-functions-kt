package io.github.manriif.supabase.functions.serve.stacktrace

import com.atlassian.sourcemap.ReadableSourceMap
import java.io.File

internal data class SourceMapEntry(
    val sourceMap: ReadableSourceMap,
    val mapFile: File
)