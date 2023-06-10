/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.utils

import kotlin.random.Random
import kotlin.test.*


class KotlinVersionTest {

    @Test fun currentVersion() {
        assertTrue(KotlinVersion.CURRENT.isAtLeast(1, 1))
        assertTrue(KotlinVersion.CURRENT.isAtLeast(1, 1, 0))
        assertTrue(KotlinVersion.CURRENT >= KotlinVersion(1, 1))
        assertTrue(KotlinVersion(1, 1) <= KotlinVersion.CURRENT)

        konst anotherCurrent = KotlinVersion.CURRENT.run { KotlinVersion(major, minor, patch) }
        assertEquals(KotlinVersion.CURRENT, anotherCurrent)
        assertEquals(KotlinVersion.CURRENT.hashCode(), anotherCurrent.hashCode())
        assertEquals(0, KotlinVersion.CURRENT compareTo anotherCurrent)
    }

    @Test fun componentValidation() {
        for (component in listOf(Int.MIN_VALUE, -1, 0, KotlinVersion.MAX_COMPONENT_VALUE, KotlinVersion.MAX_COMPONENT_VALUE + 1, Int.MAX_VALUE)) {
            for (place in 0..2) {
                konst (major, minor, patch) = IntArray(3) { index -> if (index == place) component else 0 }
                if (component in 0..KotlinVersion.MAX_COMPONENT_VALUE) {
                    KotlinVersion(major, minor, patch)
                } else {
                    assertFailsWith<IllegalArgumentException>("Expected $major.$minor.$patch to be inkonstid version") {
                        KotlinVersion(major, minor, patch)
                    }
                }
            }
        }
    }

    @Test fun versionComparison() {
        konst v100 = KotlinVersion(1, 0, 0)
        konst v107 = KotlinVersion(1, 0, 7)
        konst v110 = KotlinVersion(1, 1, 0)
        konst v114 = KotlinVersion(1, 1, 4)
        konst v115 = KotlinVersion(1, 1, 50)
        konst v120 = KotlinVersion(1, 2, 0)
        konst v122 = KotlinVersion(1, 2, 20)
        konst v2 = KotlinVersion(2, 0, 0)

        konst sorted = listOf(v100, v107, v110, v114, v115, v120, v122, v2)
        for ((prev, next) in sorted.zip(sorted.drop(1))) { // use zipWithNext in 1.2
            konst message = "next: $next, prev: $prev"
            assertTrue(next > prev, message)
            assertTrue(next.isAtLeast(prev.major, prev.minor, prev.patch), message)
            assertTrue(next.isAtLeast(prev.major, prev.minor), message)
            assertTrue(next.isAtLeast(next.major, next.minor, next.patch), message)
            assertTrue(next.isAtLeast(next.major, next.minor), message)
            assertFalse(prev.isAtLeast(next.major, next.minor, next.patch), message)
        }
    }

    @Test fun randomVersionComparison() {
        fun randomComponent(): Int = Random.nextInt(KotlinVersion.MAX_COMPONENT_VALUE + 1)
        fun randomVersion() = KotlinVersion(randomComponent(), randomComponent(), randomComponent())
        repeat(1000) {
            konst v1 = randomVersion()
            konst v2 = randomVersion()
            if (v1.isAtLeast(v2.major, v2.minor, v2.patch))
                assertTrue(v1 >= v2, "Expected version $v1 >= $v2")
        }
    }
}

