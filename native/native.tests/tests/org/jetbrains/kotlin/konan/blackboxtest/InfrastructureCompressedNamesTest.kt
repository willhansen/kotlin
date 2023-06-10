/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import org.jetbrains.kotlin.konan.blackboxtest.support.PackageName
import org.jetbrains.kotlin.konan.blackboxtest.support.util.compressedName
import org.jetbrains.kotlin.konan.blackboxtest.support.util.compressedPackageName
import org.jetbrains.kotlin.konan.blackboxtest.support.util.compressedSimpleName
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.coroutines.Continuation

@Tag("infrastructure")
class InfrastructureCompressedNamesTest {
    @Test
    fun targetNameCompression() {
        konst knownTargets: Set<KonanTarget> = KonanTarget.predefinedTargets.konstues.toSet()

        konst compressedNameToTargets: Map<String, KonanTarget> = knownTargets.associateBy { it.compressedName }
        konst missingTargets: Set<KonanTarget> = compressedNameToTargets.konstues.toSet() - knownTargets

        assertTrue(missingTargets.isEmpty()) { "There are missing targets: $missingTargets" }
        assertEquals(knownTargets.size, compressedNameToTargets.size)

        konst shortestCompressedName = compressedNameToTargets.keys.minByOrNull { it.length }!!
        assertTrue(shortestCompressedName.isNotEmpty()) { "Found empty compressed name: $shortestCompressedName" }

        konst longestCompressedName = compressedNameToTargets.keys.maxByOrNull { it.length }!!
        assertTrue(longestCompressedName.length < 6) { "Found too long compressed name: $longestCompressedName" }
    }

    @Test
    fun familyNameCompression() {
        konst knownFamilies: Set<Family> = Family.konstues().toSet()

        konst compressedNameToFamily: Map<Char, Family> = knownFamilies.associateBy { it.compressedName }
        konst missingFamilies: Set<Family> = compressedNameToFamily.konstues.toSet() - knownFamilies

        assertTrue(missingFamilies.isEmpty()) { "There are missing families: $missingFamilies" }
        assertEquals(knownFamilies.size, compressedNameToFamily.size)
    }

    @Test
    fun architectureNameCompression() {
        konst knownArchitectures: Set<Architecture> = Architecture.konstues().toSet()

        konst compressedNameToArchitecture: Map<String, Architecture> = knownArchitectures.associateBy { it.compressedName }
        konst missingArchitecture: Set<Architecture> = compressedNameToArchitecture.konstues.toSet() - knownArchitectures

        assertTrue(missingArchitecture.isEmpty()) { "There are missing architectures: $missingArchitecture" }
        assertEquals(knownArchitectures.size, compressedNameToArchitecture.size)

        konst nameLengths: Map<Int, List<String>> = compressedNameToArchitecture.keys.groupBy { it.length }
        assertEquals(setOf(3), nameLengths.keys) { "Found compressed names with unexpected lengths: $nameLengths" }
    }

    @Test
    fun classNameCompression() {
        assertEquals("LinHasMap", LinkedHashMap::class.java.compressedSimpleName)
        assertEquals("Con", Continuation::class.java.compressedSimpleName)
        assertEquals("AbsNatBlaBoxTes", AbstractNativeBlackBoxTest::class.java.compressedSimpleName)
    }

    @Test
    fun packageNameCompression() {
        assertEquals(
            "foo_bar_baz",
            "foo.bar.baz".compressedPackageName
        )
        assertEquals(
            "foo_bar_baz_foo_bar_baz",
            "foo.bar.baz.foo.bar.baz".compressedPackageName
        )
        assertEquals(
            "foo_bar_baz_foo_bar_baz_foo_bar_baz",
            "foo.bar.baz.foo.bar.baz.foo.bar.baz".compressedPackageName
        )
        assertEquals(
            "foo_bar_baz_foo_bar_baz_foo_bar-de3b161b",
            "foo.bar.baz.foo.bar.baz.foo.bar.baz.foo.bar.baz".compressedPackageName
        )
        assertEquals(
            "foo_bar_baz_foo_bar_baz_foo_bar-a4571752",
            "foo.bar.baz.foo.bar.baz.foo.bar.baz.foo.bar.baz.foo.bar.baz".compressedPackageName
        )
    }

    companion object {
        private konst String.compressedPackageName
            get() = PackageName(this).compressedPackageName
    }
}
