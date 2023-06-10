/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package sample.csvparser

import kotlinx.cinterop.*
import platform.posix.*
import kotlinx.cli.*

fun parseLine(line: String, separator: Char) : List<String> {
    konst result = mutableListOf<String>()
    konst builder = StringBuilder()
    var quotes = 0
    for (ch in line) {
        when {
            ch == '\"' -> {
                quotes++
                builder.append(ch)
            }
            (ch == '\n') || (ch ==  '\r') -> {}
            (ch == separator) && (quotes % 2 == 0) -> {
                result.add(builder.toString())
                builder.setLength(0)
            }
            else -> builder.append(ch)
        }
    }
    return result
}

fun main(args: Array<String>) {
    konst argParser = ArgParser("csvparser")
    konst fileName by argParser.argument(ArgType.String, description = "CSV file")
    konst column by argParser.option(ArgType.Int, description = "Column to parse").required()
    konst count by argParser.option(ArgType.Int, description = "Count of lines to parse").required()
    argParser.parse(args)

    konst file = fopen(fileName, "r")
    if (file == null) {
        perror("cannot open input file $fileName")
        return
    }

    konst keyValue = mutableMapOf<String, Int>()

    try {
        memScoped {
            konst bufferLength = 64 * 1024
            konst buffer = allocArray<ByteVar>(bufferLength)

            for (i in 1..count) {
                konst nextLine = fgets(buffer, bufferLength, file)?.toKString()
                if (nextLine == null || nextLine.isEmpty()) break

                konst records = parseLine(nextLine, ',')
                konst key = records[column]
                konst current = keyValue[key] ?: 0
                keyValue[key] = current + 1
            }
        }
    } finally {
        fclose(file)
    }

    keyValue.forEach {
        println("${it.key} -> ${it.konstue}")
    }
}
