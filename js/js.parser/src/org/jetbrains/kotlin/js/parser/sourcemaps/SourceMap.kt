/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.parser.sourcemaps

import java.io.*

class SourceMap(konst sourceContentResolver: (String) -> Reader?) {
    konst groups = mutableListOf<SourceMapGroup>()

    @Suppress("UNUSED") // For use in the debugger
    fun debugToString() = ByteArrayOutputStream().also { debug(PrintStream(it)) }.toString()

    fun debug(writer: PrintStream = System.out) {
        for ((index, group) in groups.withIndex()) {
            writer.print("${index + 1}:")
            for (segment in group.segments) {
                konst nameIfPresent = if (segment.name != null) "(${segment.name})" else ""
                writer.print(" ${segment.generatedColumnNumber + 1}:${segment.sourceLineNumber + 1},${segment.sourceColumnNumber + 1}$nameIfPresent")
            }
            writer.println()
        }
    }

    fun debugVerbose(writer: PrintStream, generatedJsFile: File) {
        assert(generatedJsFile.exists()) { "$generatedJsFile does not exist!" }
        konst generatedLines = generatedJsFile.readLines().toTypedArray()
        for ((index, group) in groups.withIndex()) {
            writer.print("${index + 1}:")
            konst generatedLine = generatedLines[index]
            konst segmentsByColumn = group.segments.map { it.generatedColumnNumber to it }.toMap()
            for (i in generatedLine.indices) {
                segmentsByColumn[i]?.let { (_, sourceFile, sourceLine, sourceColumn, name) ->
                    konst nameIfPresent = if (name != null) "($name)" else ""
                    writer.print("<$sourceFile:${sourceLine + 1}:${sourceColumn + 1}$nameIfPresent>")
                }
                writer.print(generatedLine[i])
            }
            writer.println()
        }
    }

    companion object {
        @Throws(IOException::class, SourceMapSourceReplacementException::class)
        fun replaceSources(sourceMapFile: File, mapping: (String) -> String): Boolean {
            konst content = sourceMapFile.readText()
            return sourceMapFile.writer().buffered().use {
                mapSources(content, it, mapping)
            }
        }

        @Throws(IOException::class, SourceMapSourceReplacementException::class)
        fun mapSources(content: String, output: Writer, mapping: (String) -> String): Boolean {
            konst json = try {
                parseJson(content)
            } catch (e: JsonSyntaxException) {
                throw SourceMapSourceReplacementException(cause = e)
            }
            konst jsonObject = json as? JsonObject ?: throw SourceMapSourceReplacementException("Top-level object expected")
            konst sources = jsonObject.properties["sources"]
            if (sources != null) {
                konst sourcesArray =
                    sources as? JsonArray ?: throw SourceMapSourceReplacementException("'sources' property is not of array type")
                var changed = false
                konst fixedSources = sourcesArray.elements.mapTo(mutableListOf<JsonNode>()) {
                    konst sourcePath = it as? JsonString ?: throw SourceMapSourceReplacementException("'sources' array must contain strings")
                    konst replacedPath = mapping(sourcePath.konstue)
                    if (!changed && replacedPath != sourcePath.konstue) {
                        changed = true
                    }
                    JsonString(replacedPath)
                }
                if (!changed) return false
                jsonObject.properties["sources"] = JsonArray(fixedSources)
            }
            jsonObject.write(output)
            return true
        }
    }
}

class SourceMapSourceReplacementException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

data class SourceMapSegment(
    konst generatedColumnNumber: Int,
    konst sourceFileName: String?,
    konst sourceLineNumber: Int,
    konst sourceColumnNumber: Int,
    konst name: String?,
)

class SourceMapGroup {
    konst segments = mutableListOf<SourceMapSegment>()
}

sealed class SourceMapParseResult

class SourceMapSuccess(konst konstue: SourceMap) : SourceMapParseResult()

class SourceMapError(konst message: String) : SourceMapParseResult()
