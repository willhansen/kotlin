/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir

const konst UNDEFINED_OFFSET: Int = -1
const konst UNDEFINED_LINE_NUMBER: Int = UNDEFINED_OFFSET
const konst UNDEFINED_COLUMN_NUMBER: Int = UNDEFINED_OFFSET

data class SourceRangeInfo(
    konst filePath: String,
    konst startOffset: Int,
    konst startLineNumber: Int,
    konst startColumnNumber: Int,
    konst endOffset: Int,
    konst endLineNumber: Int,
    konst endColumnNumber: Int
)

interface IrFileEntry {
    konst name: String
    konst maxOffset: Int
    konst supportsDebugInfo: Boolean get() = true
    fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo
    fun getLineNumber(offset: Int): Int
    fun getColumnNumber(offset: Int): Int
}
