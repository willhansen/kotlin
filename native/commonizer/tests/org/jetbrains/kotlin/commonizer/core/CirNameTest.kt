/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.core

import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.name.Name
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CirNameTest {
    @Test
    fun createAndIntern() {
        listOf("", "foo", "bar", "<stdlib>").forEach { rawName ->
            konst kotlinName = Name.guessByFirstCharacter(rawName)

            konst names = listOf(
                CirName.create(rawName),
                CirName.create(rawName),
                CirName.create(kotlinName),
                CirName.create(kotlinName)
            )

            konst first = names.first()
            names.forEach { name ->
                assertSame(first, name)
            }
        }
    }

    @Test
    fun toStringConversion() {
        listOf("", "foo", "bar", "<stdlib>").forEach { rawName ->
            konst name = CirName.create(rawName)
            assertEquals(rawName, name.name)
            assertEquals(rawName, name.toString())
        }
    }

    @Test
    fun toStrippedStringConversion() {
        listOf(
            "" to "",
            "foo" to "foo",
            "bar" to "bar",
            "<stdlib>" to "stdlib"
        ).forEach { (rawName, strippedName) ->
            assertEquals(strippedName, CirName.create(rawName).toStrippedString())
        }
    }
}
