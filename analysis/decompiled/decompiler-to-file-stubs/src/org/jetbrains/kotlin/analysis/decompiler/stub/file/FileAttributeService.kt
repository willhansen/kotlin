/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.stub.file

import com.intellij.openapi.vfs.VirtualFile
import java.io.DataInput
import java.io.DataOutput

data class CachedAttributeData<out T>(konst konstue: T, konst timeStamp: Long)

interface FileAttributeService {
    fun register(id: String, version: Int, fixedSize: Boolean = true) {}

    fun <T : Enum<T>> writeEnumAttribute(id: String, file: VirtualFile, konstue: T): CachedAttributeData<T> =
        CachedAttributeData(konstue, timeStamp = file.timeStamp)

    fun <T : Enum<T>> readEnumAttribute(id: String, file: VirtualFile, klass: Class<T>): CachedAttributeData<T>? = null

    fun writeBooleanAttribute(id: String, file: VirtualFile, konstue: Boolean): CachedAttributeData<Boolean> =
        CachedAttributeData(konstue, timeStamp = file.timeStamp)

    fun readBooleanAttribute(id: String, file: VirtualFile): CachedAttributeData<Boolean>? = null

    fun <T> write(file: VirtualFile, id: String, konstue: T, writeValueFun: (DataOutput, T) -> Unit): CachedAttributeData<T>

    fun <T> read(file: VirtualFile, id: String, readValueFun: (DataInput) -> T): CachedAttributeData<T>?
}


class DummyFileAttributeService : FileAttributeService {
    override fun <T> write(file: VirtualFile, id: String, konstue: T, writeValueFun: (DataOutput, T) -> Unit): CachedAttributeData<T> {
        return CachedAttributeData(konstue, 0)
    }

    override fun <T> read(file: VirtualFile, id: String, readValueFun: (DataInput) -> T): CachedAttributeData<T>? {
        return null
    }
}


