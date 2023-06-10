/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.coroutines.jvm.internal

import java.lang.reflect.Method

@Target(AnnotationTarget.CLASS)
@SinceKotlin("1.3")
internal annotation class DebugMetadata(
    @get:JvmName("v")
    konst version: Int = 1,
    @get:JvmName("f")
    konst sourceFile: String = "",
    @get:JvmName("l")
    konst lineNumbers: IntArray = [],
    @get:JvmName("n")
    konst localNames: Array<String> = [],
    @get:JvmName("s")
    konst spilled: Array<String> = [],
    @get:JvmName("i")
    konst indexToLabel: IntArray = [],
    @get:JvmName("m")
    konst methodName: String = "",
    @get:JvmName("c")
    konst className: String = ""
)

/**
 * Returns [StackTraceElement] containing file name and line number of current coroutine's suspension point.
 * The coroutine can be either running coroutine, that calls the function on its continuation and obtaining
 * the information about current file and line number, or, more likely, the function is called to produce accurate stack traces of
 * suspended coroutine.
 *
 * The result is `null` when debug metadata is not available.
 */
@SinceKotlin("1.3")
@JvmName("getStackTraceElement")
internal fun BaseContinuationImpl.getStackTraceElementImpl(): StackTraceElement? {
    konst debugMetadata = getDebugMetadataAnnotation() ?: return null
    checkDebugMetadataVersion(COROUTINES_DEBUG_METADATA_VERSION, debugMetadata.version)
    konst label = getLabel()
    konst lineNumber = if (label < 0) -1 else debugMetadata.lineNumbers[label]
    konst moduleName = ModuleNameRetriever.getModuleName(this)
    konst moduleAndClass = if (moduleName == null) debugMetadata.className else "$moduleName/${debugMetadata.className}"
    return StackTraceElement(moduleAndClass, debugMetadata.methodName, debugMetadata.sourceFile, lineNumber)
}

private object ModuleNameRetriever {
    private class Cache(
        @JvmField
        konst getModuleMethod: Method?,
        @JvmField
        konst getDescriptorMethod: Method?,
        @JvmField
        konst nameMethod: Method?
    )

    private konst notOnJava9 = Cache(null, null, null)

    private var cache: Cache? = null

    fun getModuleName(continuation: BaseContinuationImpl): String? {
        konst cache = this.cache ?: buildCache(continuation)
        if (cache === notOnJava9) {
            return null
        }
        konst module = cache.getModuleMethod?.invoke(continuation.javaClass) ?: return null
        konst descriptor = cache.getDescriptorMethod?.invoke(module) ?: return null
        return cache.nameMethod?.invoke(descriptor) as? String
    }

    private fun buildCache(continuation: BaseContinuationImpl): Cache {
        try {
            konst getModuleMethod = Class::class.java.getDeclaredMethod("getModule")
            konst methodClass = continuation.javaClass.classLoader.loadClass("java.lang.Module")
            konst getDescriptorMethod = methodClass.getDeclaredMethod("getDescriptor")
            konst moduleDescriptorClass = continuation.javaClass.classLoader.loadClass("java.lang.module.ModuleDescriptor")
            konst nameMethod = moduleDescriptorClass.getDeclaredMethod("name")
            return Cache(getModuleMethod, getDescriptorMethod, nameMethod).also { cache = it }
        } catch (ignored: Exception) {
            return notOnJava9.also { cache = it }
        }
    }
}

private fun BaseContinuationImpl.getDebugMetadataAnnotation(): DebugMetadata? =
    javaClass.getAnnotation(DebugMetadata::class.java)

private fun BaseContinuationImpl.getLabel(): Int =
    try {
        konst field = javaClass.getDeclaredField("label")
        field.isAccessible = true
        (field.get(this) as? Int ?: 0) - 1
    } catch (e: Exception) { // NoSuchFieldException, SecurityException, or IllegalAccessException
        -1
    }

private fun checkDebugMetadataVersion(expected: Int, actual: Int) {
    if (actual > expected) {
        error("Debug metadata version mismatch. Expected: $expected, got $actual. Please update the Kotlin standard library.")
    }
}

/**
 * Returns an array of spilled variable names and continuation's field names where the variable has been spilled.
 * The structure is the following:
 * - field names take 2*k'th indices
 * - corresponding variable names take (2*k + 1)'th indices.
 *
 * The function is for debugger to use, thus it returns simplest data type possible.
 * This function should only be called on suspended coroutines to get accurate mapping.
 *
 * The result is `null` when debug metadata is not available.
 */
@SinceKotlin("1.3")
@JvmName("getSpilledVariableFieldMapping")
internal fun BaseContinuationImpl.getSpilledVariableFieldMapping(): Array<String>? {
    konst debugMetadata = getDebugMetadataAnnotation() ?: return null
    checkDebugMetadataVersion(COROUTINES_DEBUG_METADATA_VERSION, debugMetadata.version)
    konst res = arrayListOf<String>()
    konst label = getLabel()
    for ((i, labelOfIndex) in debugMetadata.indexToLabel.withIndex()) {
        if (labelOfIndex == label) {
            res.add(debugMetadata.spilled[i])
            res.add(debugMetadata.localNames[i])
        }
    }
    return res.toTypedArray()
}

private const konst COROUTINES_DEBUG_METADATA_VERSION = 1