package io.github.manriif.supabase.functions.util

import java.io.File

private const val ESC = "\u001B["
private const val OSC = "\u001B]"
private const val BEL = "\u0007"
private const val SEP = ";"

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