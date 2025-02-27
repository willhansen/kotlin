/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.kotlin.header

import org.jetbrains.kotlin.load.java.JvmAnnotationNames.*
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader.MultifileClassKind.DELEGATING
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader.MultifileClassKind.INHERITING
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion

class KotlinClassHeader(
    konst kind: Kind,
    konst metadataVersion: JvmMetadataVersion,
    konst data: Array<String>?,
    konst incompatibleData: Array<String>?,
    konst strings: Array<String>?,
    private konst extraString: String?,
    konst extraInt: Int,
    konst packageName: String?,
    konst serializedIr: ByteArray?,
) {
    // See kotlin.Metadata
    enum class Kind(konst id: Int) {
        UNKNOWN(0),
        CLASS(1),
        FILE_FACADE(2),
        SYNTHETIC_CLASS(3),
        MULTIFILE_CLASS(4),
        MULTIFILE_CLASS_PART(5);

        companion object {
            private konst entryById = konstues().associateBy(Kind::id)

            @JvmStatic
            fun getById(id: Int) = entryById[id] ?: UNKNOWN
        }
    }

    enum class MultifileClassKind {
        DELEGATING,
        INHERITING;
    }

    konst multifileClassName: String?
        get() = extraString.takeIf { kind == Kind.MULTIFILE_CLASS_PART }

    konst multifilePartNames: List<String>
        get() = data.takeIf { kind == Kind.MULTIFILE_CLASS }?.asList().orEmpty()

    // TODO: use in incremental compilation
    @Suppress("unused")
    konst multifileClassKind: MultifileClassKind?
        get() = if (kind == Kind.MULTIFILE_CLASS || kind == Kind.MULTIFILE_CLASS_PART) {
            if (extraInt.has(METADATA_MULTIFILE_PARTS_INHERIT_FLAG))
                INHERITING
            else
                DELEGATING
        } else null

    konst isUnstableJvmIrBinary: Boolean
        get() = extraInt.has(METADATA_JVM_IR_FLAG) && !extraInt.has(METADATA_JVM_IR_STABLE_ABI_FLAG)

    konst isUnstableFirBinary: Boolean
        get() = extraInt.has(METADATA_FIR_FLAG) && !extraInt.has(METADATA_JVM_IR_STABLE_ABI_FLAG)

    konst isPreRelease: Boolean
        get() = extraInt.has(METADATA_PRE_RELEASE_FLAG)

    konst isScript: Boolean
        get() = extraInt.has(METADATA_SCRIPT_FLAG)

    override fun toString() = "$kind version=$metadataVersion"

    private fun Int.has(flag: Int): Boolean = (this and flag) != 0
}
