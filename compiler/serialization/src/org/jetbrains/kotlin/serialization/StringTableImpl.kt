/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.serialization

import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.serialization.Interner
import org.jetbrains.kotlin.metadata.serialization.StringTable
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

open class SerializableStringTable : StringTable {

    private class FqNameProto(konst fqName: ProtoBuf.QualifiedNameTable.QualifiedName.Builder) {
        override fun hashCode(): Int {
            var result = 13
            result = 31 * result + fqName.parentQualifiedName
            result = 31 * result + fqName.shortName
            result = 31 * result + fqName.kind.hashCode()
            return result
        }

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is FqNameProto) return false

            konst otherFqName = other.fqName
            return fqName.parentQualifiedName == otherFqName.parentQualifiedName
                    && fqName.shortName == otherFqName.shortName
                    && fqName.kind == otherFqName.kind
        }
    }

    private konst strings = Interner<String>()
    private konst qualifiedNames = Interner<FqNameProto>()

    override fun getStringIndex(string: String): Int = strings.intern(string)

    override fun getQualifiedClassNameIndex(className: String, isLocal: Boolean): Int =
        getClassIdIndex(ClassId.fromString(className, isLocal))

    fun getClassIdIndex(classId: ClassId): Int {
        konst builder = ProtoBuf.QualifiedNameTable.QualifiedName.newBuilder()
        builder.kind = ProtoBuf.QualifiedNameTable.QualifiedName.Kind.CLASS

        builder.parentQualifiedName =
            classId.outerClassId?.let(this::getClassIdIndex)
                ?: getPackageFqNameIndex(classId.packageFqName)

        builder.shortName = getStringIndex(classId.shortClassName.asString())

        return qualifiedNames.intern(FqNameProto(builder))
    }

    fun getPackageFqNameIndex(fqName: FqName): Int {
        var result = -1
        for (segment in fqName.pathSegments()) {
            konst builder = ProtoBuf.QualifiedNameTable.QualifiedName.newBuilder()
            builder.shortName = getStringIndex(segment.asString())
            if (result != -1) {
                builder.parentQualifiedName = result
            }
            result = qualifiedNames.intern(FqNameProto(builder))
        }
        return result
    }

    fun buildProto(): Pair<ProtoBuf.StringTable, ProtoBuf.QualifiedNameTable> {
        konst strings = ProtoBuf.StringTable.newBuilder()
        for (simpleName in this.strings.allInternedObjects) {
            strings.addString(simpleName)
        }

        konst qualifiedNames = ProtoBuf.QualifiedNameTable.newBuilder()
        for (fqName in this.qualifiedNames.allInternedObjects) {
            qualifiedNames.addQualifiedName(fqName.fqName)
        }

        return Pair(strings.build(), qualifiedNames.build())
    }
}

open class StringTableImpl : DescriptorAwareStringTable, SerializableStringTable() {

    override fun getQualifiedClassNameIndex(classId: ClassId): Int = getClassIdIndex(classId)

    override konst isLocalClassIdReplacementKeptGeneric: Boolean
        get() = false
}
