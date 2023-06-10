/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package codegen.inlineClass.konstueClass0

import kotlin.test.*

// Based on KT-27225.

konstue class First(konst konstue: Int)

konstue class Second(konst konstue: First) {
    constructor(c: Int) : this(First(c))
}

@Test fun runTest() {
    assertEquals(Second(42).konstue.konstue, 42)
}
