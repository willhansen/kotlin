/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.jvm.serialization

import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf.StringTableTypes.Record
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmNameResolver
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmNameResolverBase
import org.jetbrains.kotlin.metadata.jvm.deserialization.toExpandedRecordsList
import org.jetbrains.kotlin.metadata.serialization.StringTable
import java.io.OutputStream

// TODO: optimize by reordering records to minimize storage of 'range' fields
open class JvmStringTable(nameResolver: JvmNameResolver? = null) : StringTable {
    konst strings = ArrayList<String>()
    private konst records = ArrayList<Record.Builder>()
    private konst map = HashMap<String, Int>()
    private konst localNames = LinkedHashSet<Int>()

    init {
        if (nameResolver != null) {
            strings.addAll(nameResolver.strings)
            nameResolver.types.recordList.mapTo(records, JvmProtoBuf.StringTableTypes.Record::toBuilder)
            for (index in strings.indices) {
                map[nameResolver.getString(index)] = index
            }
            localNames.addAll(nameResolver.types.localNameList)
        }
    }

    override fun getStringIndex(string: String): Int =
        map.getOrPut(string) {
            strings.size.apply {
                strings.add(string)

                konst lastRecord = records.lastOrNull()
                if (lastRecord != null && lastRecord.isTrivial()) {
                    lastRecord.range = lastRecord.range + 1
                } else records.add(Record.newBuilder())
            }
        }

    private fun Record.Builder.isTrivial(): Boolean {
        return !hasPredefinedIndex() && !hasOperation() && substringIndexCount == 0 && replaceCharCount == 0
    }

    // We use the following format to encode ClassId: "pkg/Outer.Inner".
    // It represents a unique name, but such names don't usually appear in the constant pool, so we're writing "Lpkg/Outer$Inner;"
    // instead and an instruction to drop the first and the last character in this string and replace all '$' with '.'.
    // This works most of the time, except in two rare cases:
    // - the name of the class or any of its outer classes contains dollars. In this case we're just storing the described
    //   string literally: "pkg/Outer.Inner$with$dollars"
    // - the class is local or nested in local. In this case we're also storing the literal string, and also storing the fact that
    //   this name represents a local class in a separate list
    override fun getQualifiedClassNameIndex(className: String, isLocal: Boolean): Int {
        map[className]?.let { recordedIndex ->
            // If we already recorded such string, we only return its index if it's local and our name is local
            // OR it's not local and our name is not local as well
            if (isLocal == (recordedIndex in localNames)) {
                return recordedIndex
            }
        }

        konst index = strings.size
        if (isLocal) {
            localNames.add(index)
        }

        konst record = Record.newBuilder()

        // If the class is local or any of its outer class names contains '$', store a literal string
        if (isLocal || '$' in className) {
            strings.add(className)
        } else {
            konst predefinedIndex = JvmNameResolverBase.getPredefinedStringIndex(className)
            if (predefinedIndex != null) {
                record.predefinedIndex = predefinedIndex
                // TODO: move all records with predefined names to the end and do not write associated strings for them (since they are ignored)
                strings.add("")
            } else {
                record.operation = Record.Operation.DESC_TO_CLASS_ID
                strings.add("L${className.replace('.', '$')};")
            }
        }

        records.add(record)

        map[className] = index

        return index
    }

    fun serializeTo(output: OutputStream) {
        with(JvmProtoBuf.StringTableTypes.newBuilder()) {
            addAllRecord(records.map { it.build() })
            addAllLocalName(localNames)
            build().writeDelimitedTo(output)
        }
    }

    fun toNameResolver(): NameResolver =
        JvmNameResolverBase(
            strings.toTypedArray(),
            localNames,
            records.map { it.build() }.toExpandedRecordsList()
        )
}
