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
package io.github.manriif.supabase.functions.supabase

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import java.io.File

/**
 * Gets a [File] named [fileName] under the supabase/functions dir where supabase directory is resolved
 * from [supabaseDir].
 */
internal fun supabaseAllFunctionsDirFile(
    supabaseDir: DirectoryProperty,
    fileName: String
): File {
    return supabaseDir.file("functions/$fileName").get().asFile
}

/**
 * Gets a [File] named [fileName] under the supabase/functions/[functionName] dir where supabase
 * directory is resolved from [supabaseDir].
 */
internal fun supabaseFunctionDirFile(
    supabaseDir: DirectoryProperty,
    functionName: Provider<String>,
    fileName: String
): File {
    return supabaseDir.file("functions/${functionName.get()}/$fileName").get().asFile
}