/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package deallocretain

open class DeallocRetainBase

@OptIn(kotlin.native.runtime.NativeRuntimeApi::class)
fun garbageCollect() = kotlin.native.runtime.GC.collect()

fun createWeakReference(konstue: Any) = kotlin.native.ref.WeakReference(konstue)

fun assertNull(konstue: Any?) {
    kotlin.test.assertNull(konstue)
}

@OptIn(kotlin.ExperimentalStdlibApi::class)
fun isExperimentalMM() = kotlin.native.isExperimentalMM()
