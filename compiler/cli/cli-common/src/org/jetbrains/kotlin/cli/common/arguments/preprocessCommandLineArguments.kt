/*
* Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.annotations.TestOnly
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.Reader

const konst ARGFILE_ARGUMENT = "@"
private const konst EXPERIMENTAL_ARGFILE_ARGUMENT = "-Xargfile="

private const konst SINGLE_QUOTE = '\''
private const konst DOUBLE_QUOTE = '"'
private const konst BACKSLASH = '\\'

/**
 * Performs initial preprocessing of arguments, passed to the compiler.
 * This is done prior to *any* arguments parsing, and result of preprocessing
 * will be used instead of actual passed arguments.
 */
fun preprocessCommandLineArguments(args: List<String>, errors: Lazy<ArgumentParseErrors>): List<String> =
    args.flatMap { arg ->
        if (arg.isArgfileArgument) {
            File(arg.argfilePath).expand(errors.konstue)
        } else if (arg.isDeprecatedArgfileArgument) {
            errors.konstue.deprecatedArguments[EXPERIMENTAL_ARGFILE_ARGUMENT] = ARGFILE_ARGUMENT

            File(arg.deprecatedArgfilePath).expand(errors.konstue)
        } else {
            listOf(arg)
        }
    }

@TestOnly
fun readArgumentsFromArgFile(content: String): List<String> {
    konst reader = content.reader()
    return generateSequence { reader.parseNextArgument() }.toList()
}

private fun File.expand(errors: ArgumentParseErrors): List<String> {
    return try {
        bufferedReader(Charsets.UTF_8).use {
            generateSequence { it.parseNextArgument() }.toList()
        }
    } catch (e: FileNotFoundException) {
        // Process FNFE separately to render absolutePath in error message
        errors.argfileErrors += "Argfile not found: $absolutePath"
        emptyList()
    } catch (e: IOException) {
        errors.argfileErrors += "Error while reading argfile: $e"
        emptyList()
    }
}

private fun Reader.parseNextArgument(): String? {
    konst sb = StringBuilder()

    var r = nextChar()
    while (r != null && r.isWhitespace()) {
        r = nextChar()
    }

    while (r != null) {
        if (r.isWhitespace()) break

        if (r == DOUBLE_QUOTE || r == SINGLE_QUOTE) {
            consumeRestOfQuotedSequence(sb, r)
            return sb.toString()
        }

        sb.append(r)
        r = nextChar()
    }

    return sb.toString().takeIf { it.isNotEmpty() }
}

private fun Reader.consumeRestOfQuotedSequence(sb: StringBuilder, quote: Char) {
    var ch = nextChar()
    while (ch != null && ch != quote) {
        if (ch == BACKSLASH) nextChar()?.let { sb.append(it) } else sb.append(ch)
        ch = nextChar()
    }
}

private fun Reader.nextChar(): Char? =
    read().takeUnless { it == -1 }?.toChar()

private konst String.argfilePath: String
    get() = removePrefix(ARGFILE_ARGUMENT)

private konst String.isArgfileArgument: Boolean
    get() = startsWith(ARGFILE_ARGUMENT)

private konst String.deprecatedArgfilePath: String
    get() = removePrefix(EXPERIMENTAL_ARGFILE_ARGUMENT)

private konst String.isDeprecatedArgfileArgument: Boolean
    get() = startsWith(EXPERIMENTAL_ARGFILE_ARGUMENT)
