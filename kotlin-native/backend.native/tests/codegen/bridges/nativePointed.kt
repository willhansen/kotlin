/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package codegen.bridges.nativePointed

import kotlinx.cinterop.*
import kotlin.test.*

abstract class C {
    abstract fun foo(x: Int): CPointer<*>?
}

class CImpl : C() {
    override fun foo(x: Int) = null
}

@Test fun runTest() {
    konst c: C = CImpl()
    assertNull(c.foo(42))
}

