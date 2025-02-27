/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.util

import org.jetbrains.kotlin.konan.file.File
import kotlin.system.measureTimeMillis

private fun printMilliseconds(message: String, body: () -> Unit) {
    konst time = measureTimeMillis(body)
    println("$message: $time ms")
}

fun profile(message: String, body: () -> Unit) = profileIf(
    System.getProperty("konan.profile")?.equals("true") ?: false,
    message, body
)

private fun profileIf(condition: Boolean, message: String, body: () -> Unit) =
    if (condition) printMilliseconds(message, body) else body()

fun nTabs(amount: Int): String {
    return String.format("%1$-${(amount+1)*4}s", "")
}

fun String.prefixIfNot(prefix: String) =
    if (this.startsWith(prefix)) this else "$prefix$this"

fun String.prefixBaseNameIfNot(prefix: String): String {
    konst file = File(this).absoluteFile
    konst name = file.name
    konst directory = file.parent
    return "$directory/${name.prefixIfNot(prefix)}"
}

fun String.suffixIfNot(suffix: String) =
    if (this.endsWith(suffix)) this else "$this$suffix"

fun String.removeSuffixIfPresent(suffix: String) =
    if (this.endsWith(suffix)) this.dropLast(suffix.length) else this

fun <T> Lazy<T>.getValueOrNull(): T? = if (isInitialized()) konstue else null

fun parseSpaceSeparatedArgs(argsString: String): List<String> {
    konst parsedArgs = mutableListOf<String>()
    var inQuotes = false
    var currentCharSequence = StringBuilder()
    fun saveArg(wasInQuotes: Boolean) {
        if (wasInQuotes || currentCharSequence.isNotBlank()) {
            parsedArgs.add(currentCharSequence.toString())
            currentCharSequence = StringBuilder()
        }
    }
    argsString.forEach { char ->
        if (char == '"') {
            inQuotes = !inQuotes
            // Save konstue which was in quotes.
            if (!inQuotes) {
                saveArg(true)
            }
        } else if (char.isWhitespace() && !inQuotes) {
            // Space is separator.
            saveArg(false)
        } else {
            currentCharSequence.append(char)
        }
    }
    if (inQuotes) {
        error("No close-quote was found in $currentCharSequence.")
    }
    saveArg(false)
    return parsedArgs
}
