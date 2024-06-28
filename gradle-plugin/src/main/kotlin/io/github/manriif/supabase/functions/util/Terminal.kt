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
package io.github.manriif.supabase.functions.util

import java.io.File

private const val ESC = "\u001B["
/*private const val OSC = "\u001B]"
private const val BEL = "\u0007"
private const val SEP = ";"*/

///////////////////////////////////////////////////////////////////////////
// Color
///////////////////////////////////////////////////////////////////////////

internal enum class Color(private val value: Int) {
    Black(0),
    Red(1),
    Green(2),
    Yellow(3),
    Blue(4),
    Magenta(5),
    Cyan(6),
    White(7),
    Default(9);

    fun text(): Int = value + 30

    fun textBright(): Int = value + 90
}

private fun ansiColor(value: Any): String {
    return "${ESC}${value}m"
}

internal fun String.colored(color: Color, bright: Boolean = false): String {
    val value = if (bright) {
        color.textBright()
    } else {
        color.text()
    }

    return "${ansiColor(value)}${this}${ansiColor(0)}"
}

///////////////////////////////////////////////////////////////////////////
// Link
///////////////////////////////////////////////////////////////////////////

@Suppress("UNUSED_PARAMETER")
internal fun File.link(label: String): String {
    return canonicalPath /*listOf(
        OSC,
        "8",
        SEP,
        SEP,
        "file://$canonicalPath",
        BEL,
        label,
        OSC,
        "8",
        SEP,
        SEP,
        BEL
    ).joinToString("")*/
}