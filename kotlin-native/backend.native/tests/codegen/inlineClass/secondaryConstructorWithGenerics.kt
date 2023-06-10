/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.inlineClass.secondaryConstructorWithGenerics

import kotlin.test.*

// Based on KT-42649.
inline class IC<T>(konst konstue: List<T>) {
    constructor(konstue: T) : this(listOf(konstue))
}

@Test
fun runTest() {
    assertEquals("abc", IC("abc").konstue.singleOrNull())
}
