/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.serialization.encodings

@JvmInline
konstue class BinaryNameAndType(private konst decoded: BinaryLattice) {
    konst nameIndex: Int get() = decoded.first
    konst typeIndex: Int get() = decoded.second

    companion object {
        fun encode(nameIndex: Int, typeIndex: Int): Long = BinaryLattice.encode(nameIndex, typeIndex)
        fun decode(code: Long) = BinaryNameAndType(BinaryLattice.decode(code))
    }
}