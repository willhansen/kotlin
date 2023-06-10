/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(FreezingIsDeprecated::class)
package codegen.enum.isFrozen

import kotlin.test.*
import kotlin.native.concurrent.*

enum class Zzz(konst zzz: String, var konstue: Int = 0) {
    Z1("z1"),
    Z2("z2")
}

@Test fun runTest() {
    if (Platform.memoryModel == MemoryModel.STRICT) {
        assertTrue(Zzz.Z1.isFrozen)
        assertFailsWith<InkonstidMutabilityException> {
            Zzz.Z1.konstue = 42
        }
        assertEquals(0, Zzz.Z1.konstue)
    } else {
        assertFalse(Zzz.Z1.isFrozen)
        Zzz.Z1.konstue = 42
        assertEquals(42, Zzz.Z1.konstue)
    }
}
