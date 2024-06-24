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

import org.jetbrains.kotlin.com.google.gson.JsonElement
import org.jetbrains.kotlin.com.google.gson.annotations.SerializedName
import java.io.File

// Gson set nulls reflectively no matter on default values and non-null types
internal class Request : RequestOptions() {

    val name: String? = null
    val method: String? = null
    val body: JsonElement? = null
    val type: Type? = null
    val validation: RequestValidation? = null

    @Suppress("USELESS_ELVIS")
    val parameters: Map<String, String> = emptyMap()
        get() = field ?: emptyMap()

    @Transient
    var resolvedFile: File? = null

    override fun toString(): String {
        return "Request(" +
                "name=$name, " +
                "method=$method, " +
                "http=$http, " +
                "timeout=$timeout, " +
                "type=$type, " +
                "headers=$headers, " +
                "parameters=$parameters, " +
                "body=$body, " +
                "validation=$validation" +
                ")"
    }

    enum class Type {

        @SerializedName("plain")
        Plain,

        @SerializedName("json")
        Json,

        @SerializedName("file")
        File
    }
}