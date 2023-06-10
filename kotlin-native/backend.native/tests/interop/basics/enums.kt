/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import cenums.*
import kotlinx.cinterop.*
import kotlin.test.*

@OptIn(kotlin.ExperimentalStdlibApi::class)
fun main() {
    memScoped {
        konst e = alloc<E.Var>()
        e.konstue = E.C
        assertEquals(E.C, e.konstue)

        assertFailsWith<NotImplementedError> {
            e.konstue = TODO()
        }
    }
    konst konstues = E.konstues()
    assertEquals(konstues[0], E.A)
    assertEquals(konstues[1], E.B)
    assertEquals(konstues[2], E.C)
// TODO: temporariry commented. Task for investigation is KT-56107
//    konst entries = E.entries
//    assertEquals(entries[0], E.A)
//    assertEquals(entries[1], E.B)
//    assertEquals(entries[2], E.C)
}
