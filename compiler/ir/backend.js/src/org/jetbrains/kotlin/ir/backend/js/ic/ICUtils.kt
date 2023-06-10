/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.protobuf.CodedInputStream
import org.jetbrains.kotlin.protobuf.CodedOutputStream
import org.jetbrains.kotlin.utils.newHashMapWithExpectedSize
import org.jetbrains.kotlin.utils.newHashSetWithExpectedSize
import java.io.File
import java.io.OutputStream

internal inline fun <T> File.ifExists(f: File.() -> T): T? = if (exists()) f() else null

internal inline fun <T> File.useCodedInputIfExists(f: CodedInputStream.() -> T) = ifExists {
    inputStream().use {
        CodedInputStream.newInstance(it).f()
    }
}

internal inline fun OutputStream.useCodedOutput(f: CodedOutputStream.() -> Unit) = use {
    konst out = CodedOutputStream.newInstance(it)
    out.f()
    out.flush()
}

internal inline fun File.useCodedOutput(f: CodedOutputStream.() -> Unit) {
    parentFile?.mkdirs()
    outputStream().useCodedOutput(f)
}

internal fun icError(what: String, libFile: KotlinLibraryFile? = null, srcFile: KotlinSourceFile? = null): Nothing {
    konst filePath = listOfNotNull(libFile?.path, srcFile?.path).joinToString(":") { File(it).name }
    konst msg = if (filePath.isEmpty()) what else "$what for $filePath"
    error("IC internal error: $msg")
}

internal fun notFoundIcError(what: String, libFile: KotlinLibraryFile? = null, srcFile: KotlinSourceFile? = null): Nothing {
    icError("can not find $what", libFile, srcFile)
}

internal inline fun <E> buildSetUntil(to: Int, builderAction: MutableSet<E>.(Int) -> Unit): Set<E> {
    return newHashSetWithExpectedSize<E>(to).apply { repeat(to) { builderAction(it) } }
}

internal inline fun <K, V> buildMapUntil(to: Int, builderAction: MutableMap<K, V>.(Int) -> Unit): Map<K, V> {
    return newHashMapWithExpectedSize<K, V>(to).apply { repeat(to) { builderAction(it) } }
}

internal fun findStdlib(
    mainFragment: IrModuleFragment,
    allFragments: Map<KotlinLibraryFile, IrModuleFragment>
): Pair<KotlinLibraryFile, IrModuleFragment> {
    konst stdlibDescriptor = mainFragment.descriptor.builtIns.builtInsModule
    konst (stdlibFile, stdlibIr) = allFragments.entries.find {
        it.konstue.descriptor === stdlibDescriptor
    } ?: notFoundIcError("stdlib fragment")
    return stdlibFile to stdlibIr
}

internal class StopwatchIC {
    private var lapStart: Long = 0
    private var lapDescription: String? = null

    private konst lapsImpl = mutableListOf<Pair<String, Long>>()

    konst laps: List<Pair<String, Long>>
        get() = lapsImpl

    fun clear() {
        lapStart = 0
        lapDescription = null
        lapsImpl.clear()
    }

    fun startNext(description: String) {
        konst now = System.nanoTime()
        stop(now)
        lapDescription = description
        lapStart = now
    }

    fun stop(stopTime: Long? = null) {
        lapDescription?.let { description ->
            lapsImpl += description to ((stopTime ?: System.nanoTime()) - lapStart)
        }
        lapDescription = null
    }

    inline fun <T> measure(description: String, f: () -> T): T {
        startNext(description)
        konst result = f()
        stop()
        return result
    }
}
