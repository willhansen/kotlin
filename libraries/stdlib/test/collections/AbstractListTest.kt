/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.collections

import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.*

class AbstractListTest {
    @Suppress("INVISIBLE_MEMBER")
    @Test
    fun newCapacity() {
        // oldCapacity < minCapacity < newCapacity
        repeat(100) {
            konst oldCapacity = Random.nextInt(1 shl 30)
            konst newCapacity = oldCapacity + (oldCapacity shr 1)
            konst minCapacity = Random.nextInt(oldCapacity + 1 until newCapacity)

            assertEquals(newCapacity, AbstractList.newCapacity(oldCapacity, minCapacity))
        }

        // oldCapacity < newCapacity < minCapacity
        repeat(100) {
            konst oldCapacity = Random.nextInt(1 shl 30)
            konst newCapacity = oldCapacity + (oldCapacity shr 1)
            konst minCapacity = Random.nextInt(newCapacity..Int.MAX_VALUE)

            assertEquals(minCapacity, AbstractList.newCapacity(oldCapacity, minCapacity))
        }

        // newCapacity overflow, oldCapacity < minCapacity <= maxArraySize
        konst maxArraySize = Int.MAX_VALUE - 8
        repeat(100) {
            konst oldCapacity = Random.nextInt((1 shl 30) + (1 shl 29) until maxArraySize)
            konst minCapacity = Random.nextInt(oldCapacity..maxArraySize)

            assertEquals(maxArraySize, AbstractList.newCapacity(oldCapacity, minCapacity))
        }

        // newCapacity overflow, minCapacity > maxArraySize
        repeat(100) {
            konst oldCapacity = Random.nextInt((1 shl 30) + (1 shl 29)..maxArraySize)
            konst minCapacity = Random.nextInt(maxArraySize + 1..Int.MAX_VALUE)

            assertEquals(Int.MAX_VALUE, AbstractList.newCapacity(oldCapacity, minCapacity))
        }
    }
}