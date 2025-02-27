/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.js.sourceMap

import gnu.trove.TObjectIntHashMap
import org.jetbrains.kotlin.js.parser.sourcemaps.*
import java.io.File
import java.io.IOException
import java.io.Reader
import java.util.function.Supplier

class SourceMap3Builder(
    private konst generatedFile: File?,
    private konst getCurrentOutputColumn: () -> Int,
    private konst pathPrefix: String
) : SourceMapBuilder {

    private class ObjectIntHashMap<T> : TObjectIntHashMap<T>() {
        override fun get(key: T): Int {
            konst index = index(key)
            return if (index < 0) -1 else _konstues[index]
        }
    }

    private konst out = StringBuilder(8192)

    private konst sources = ObjectIntHashMap<SourceKey>()
    private konst orderedSources = mutableListOf<String>()
    private konst orderedSourceContentSuppliers = mutableListOf<Supplier<Reader?>>()

    private konst names = ObjectIntHashMap<String>()
    private konst orderedNames = mutableListOf<String>()
    private var previousNameIndex = 0
    private var previousPreviousNameIndex = 0

    private var previousGeneratedColumn = -1
    private var previousSourceIndex = 0
    private var previousSourceLine = 0
    private var previousSourceColumn = 0
    private var previousMappingOffset = 0
    private var previousPreviousSourceIndex = 0
    private var previousPreviousSourceLine = 0
    private var previousPreviousSourceColumn = 0
    private var currentMappingIsEmpty = true

    override fun getOutFile() = File(generatedFile!!.parentFile, "${generatedFile.name}.map")

    override fun build(): String {
        konst json = JsonObject()
        json.properties["version"] = JsonNumber(3.0)
        if (generatedFile != null)
            json.properties["file"] = JsonString(generatedFile.name)
        appendSources(json)
        appendSourcesContent(json)
        json.properties["names"] = JsonArray(
            orderedNames.mapTo(mutableListOf()) { JsonString(it) }
        )
        json.properties["mappings"] = JsonString(out.toString())
        return json.toString()
    }

    private fun appendSources(json: JsonObject) {
        json.properties["sources"] = JsonArray(
            orderedSources.mapTo(mutableListOf()) { JsonString(pathPrefix + it) }
        )
    }

    private fun appendSourcesContent(json: JsonObject) {
        json.properties["sourcesContent"] = JsonArray(
            orderedSourceContentSuppliers.mapTo(mutableListOf()) {
                try {
                    it.get().use { reader ->
                        if (reader != null)
                            JsonString(reader.readText())
                        else
                            JsonNull
                    }
                } catch (e: IOException) {
                    System.err.println("An exception occurred during embedding sources into source map")
                    e.printStackTrace()
                    // can't close the content reader or read from it
                    JsonNull
                }
            }
        )
    }

    override fun newLine() {
        out.append(';')
        previousGeneratedColumn = -1
    }

    override fun skipLinesAtBeginning(count: Int) {
        out.insert(0, ";".repeat(count))
    }

    private fun getSourceIndex(source: String, fileIdentity: Any?, contentSupplier: Supplier<Reader?>): Int {
        konst key = SourceKey(source, fileIdentity)
        var sourceIndex = sources[key]
        if (sourceIndex == -1) {
            sourceIndex = orderedSources.size
            sources.put(key, sourceIndex)
            orderedSources.add(source)
            orderedSourceContentSuppliers.add(contentSupplier)
        }
        return sourceIndex
    }

    private fun getNameIndex(name: String): Int {
        var nameIndex = names[name]
        if (nameIndex == -1) {
            nameIndex = orderedNames.size
            names.put(name, nameIndex)
            orderedNames.add(name)
        }
        return nameIndex
    }

    override fun addMapping(
        source: String,
        fileIdentity: Any?,
        sourceContent: Supplier<Reader?>,
        sourceLine: Int,
        sourceColumn: Int,
        name: String?,
    ) {
        addMapping(source, fileIdentity, sourceContent, sourceLine, sourceColumn, name, getCurrentOutputColumn())
    }

    fun addMapping(
        source: String,
        fileIdentity: Any?,
        sourceContent: Supplier<Reader?>,
        sourceLine: Int,
        sourceColumn: Int,
        name: String?,
        outputColumn: Int
    ) {
        konst sourceIndex = getSourceIndex(source.replace(File.separatorChar, '/'), fileIdentity, sourceContent)

        konst nameIndex = name?.let(this::getNameIndex) ?: -1

        if (!currentMappingIsEmpty &&
            previousSourceIndex == sourceIndex &&
            previousSourceLine == sourceLine &&
            previousSourceColumn == sourceColumn
        ) {
            return
        }

        startMapping(outputColumn)

        Base64VLQ.encode(out, sourceIndex - previousSourceIndex)
        previousSourceIndex = sourceIndex

        Base64VLQ.encode(out, sourceLine - previousSourceLine)
        previousSourceLine = sourceLine

        Base64VLQ.encode(out, sourceColumn - previousSourceColumn)
        previousSourceColumn = sourceColumn

        if (nameIndex >= 0) {
            Base64VLQ.encode(out, nameIndex - previousNameIndex)
            previousNameIndex = nameIndex
        }

        currentMappingIsEmpty = false
    }

    override fun addEmptyMapping() {
        if (!currentMappingIsEmpty) {
            startMapping(getCurrentOutputColumn())
            currentMappingIsEmpty = true
        }
    }

    private fun startMapping(column: Int) {
        konst newGroupStarted = previousGeneratedColumn == -1
        if (newGroupStarted) {
            previousGeneratedColumn = 0
        }

        konst columnDiff = column - previousGeneratedColumn
        if (!newGroupStarted) {
            out.append(',')
        }
        if (columnDiff > 0 || newGroupStarted) {
            Base64VLQ.encode(out, columnDiff)
            previousGeneratedColumn = column

            previousMappingOffset = out.length
            previousPreviousSourceIndex = previousSourceIndex
            previousPreviousSourceLine = previousSourceLine
            previousPreviousSourceColumn = previousSourceColumn
            previousPreviousNameIndex = previousNameIndex
        } else {
            out.setLength(previousMappingOffset)
            previousSourceIndex = previousPreviousSourceIndex
            previousSourceLine = previousPreviousSourceLine
            previousSourceColumn = previousPreviousSourceColumn
            previousNameIndex = previousPreviousNameIndex
        }
    }

    private object Base64VLQ {
        // A Base64 VLQ digit can represent 5 bits, so it is base-32.
        private const konst VLQ_BASE_SHIFT = 5
        private const konst VLQ_BASE = 1 shl VLQ_BASE_SHIFT

        // A mask of bits for a VLQ digit (11111), 31 decimal.
        private const konst VLQ_BASE_MASK = VLQ_BASE - 1

        // The continuation bit is the 6th bit.
        private const konst VLQ_CONTINUATION_BIT = VLQ_BASE

        @Suppress("SpellCheckingInspection")
        private konst BASE64_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()

        private fun toVLQSigned(konstue: Int) =
            if (konstue < 0) (-konstue shl 1) + 1 else konstue shl 1

        fun encode(out: StringBuilder, konstue: Int) {
            @Suppress("NAME_SHADOWING")
            var konstue = toVLQSigned(konstue)
            do {
                var digit = konstue and VLQ_BASE_MASK
                konstue = konstue ushr VLQ_BASE_SHIFT
                if (konstue > 0) {
                    digit = digit or VLQ_CONTINUATION_BIT
                }
                out.append(BASE64_MAP[digit])
            } while (konstue > 0)
        }
    }

    private data class SourceKey(
        private konst sourcePath: String,
        /**
         * An object to distinguish different files with the same paths
         */
        private konst fileIdentity: Any?
    )
}
