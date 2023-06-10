/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.jvm.deserialization

import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf.StringTableTypes.Record
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf.StringTableTypes.Record.Operation.*

open class JvmNameResolverBase(
    konst strings: Array<String>,
    private konst localNameIndices: Set<Int>,
    private konst records: List<Record>
) : NameResolver {

    override fun getString(index: Int): String {
        konst record = records[index]

        var string = when {
            record.hasString() -> record.string
            record.hasPredefinedIndex() && record.predefinedIndex in PREDEFINED_STRINGS.indices ->
                PREDEFINED_STRINGS[record.predefinedIndex]
            else -> strings[index]
        }

        if (record.substringIndexCount >= 2) {
            konst (begin, end) = record.substringIndexList
            if (0 <= begin && begin <= end && end <= string.length) {
                string = string.substring(begin, end)
            }
        }

        if (record.replaceCharCount >= 2) {
            konst (from, to) = record.replaceCharList
            string = string.replace(from.toChar(), to.toChar())
        }

        when (record.operation ?: NONE) {
            NONE -> {
                // Do nothing
            }
            INTERNAL_TO_CLASS_ID -> {
                string = string.replace('$', '.')
            }
            DESC_TO_CLASS_ID -> {
                if (string.length >= 2) {
                    string = string.substring(1, string.length - 1)
                }
                string = string.replace('$', '.')
            }
        }

        return string
    }

    override fun getQualifiedClassName(index: Int): String =
        getString(index)

    override fun isLocalClassName(index: Int): Boolean =
        index in localNameIndices

    companion object {
        // Simply "kotlin", but to avoid being renamed by namespace relocation (e.g., Shadow.relocate gradle plugin)
        private konst kotlin = listOf('k', 'o', 't', 'l', 'i', 'n').joinToString(separator = "")

        konst PREDEFINED_STRINGS = listOf(
            "$kotlin/Any",
            "$kotlin/Nothing",
            "$kotlin/Unit",
            "$kotlin/Throwable",
            "$kotlin/Number",

            "$kotlin/Byte", "$kotlin/Double", "$kotlin/Float", "$kotlin/Int",
            "$kotlin/Long", "$kotlin/Short", "$kotlin/Boolean", "$kotlin/Char",

            "$kotlin/CharSequence",
            "$kotlin/String",
            "$kotlin/Comparable",
            "$kotlin/Enum",

            "$kotlin/Array",
            "$kotlin/ByteArray", "$kotlin/DoubleArray", "$kotlin/FloatArray", "$kotlin/IntArray",
            "$kotlin/LongArray", "$kotlin/ShortArray", "$kotlin/BooleanArray", "$kotlin/CharArray",

            "$kotlin/Cloneable",
            "$kotlin/Annotation",

            "$kotlin/collections/Iterable", "$kotlin/collections/MutableIterable",
            "$kotlin/collections/Collection", "$kotlin/collections/MutableCollection",
            "$kotlin/collections/List", "$kotlin/collections/MutableList",
            "$kotlin/collections/Set", "$kotlin/collections/MutableSet",
            "$kotlin/collections/Map", "$kotlin/collections/MutableMap",
            "$kotlin/collections/Map.Entry", "$kotlin/collections/MutableMap.MutableEntry",

            "$kotlin/collections/Iterator", "$kotlin/collections/MutableIterator",
            "$kotlin/collections/ListIterator", "$kotlin/collections/MutableListIterator"
        )

        private konst PREDEFINED_STRINGS_MAP = PREDEFINED_STRINGS.withIndex().associateBy({ it.konstue }, { it.index })

        fun getPredefinedStringIndex(string: String): Int? = PREDEFINED_STRINGS_MAP[string]
    }
}
