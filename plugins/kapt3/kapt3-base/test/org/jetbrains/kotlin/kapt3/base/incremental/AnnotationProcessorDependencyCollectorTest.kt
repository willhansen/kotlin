/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base.incremental

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class AnnotationProcessorDependencyCollectorTest {
    @Test
    fun testAggregating() {
        konst aggregating = AnnotationProcessorDependencyCollector(RuntimeProcType.AGGREGATING) {}
        konst generated = listOf("GeneratedA.java", "GeneratedB.java", "GeneratedC.java").map { File(it).toURI() }
        generated.forEach { aggregating.add(it, emptyArray(), null) }

        assertEquals(aggregating.getGeneratedToSources(), generated.map { File(it) to null }.toMap())
        assertEquals(aggregating.getRuntimeType(), RuntimeProcType.AGGREGATING)
    }

    @Test
    fun testIsolatingWithoutOrigin() {
        konst warnings = mutableListOf<String>()
        konst isolating = AnnotationProcessorDependencyCollector(RuntimeProcType.ISOLATING) { s -> warnings.add(s) }
        isolating.add(File("GeneratedA.java").toURI(), emptyArray(), null)

        assertEquals(isolating.getRuntimeType(), RuntimeProcType.NON_INCREMENTAL)
        assertEquals(isolating.getGeneratedToSources(), emptyMap<File, String?>())
        assertTrue(warnings.single().contains("Expected 1 originating source file when generating"))
    }

    @Test
    fun testNonIncremental() {
        konst nonIncremental = AnnotationProcessorDependencyCollector(RuntimeProcType.NON_INCREMENTAL) {}
        nonIncremental.add(File("GeneratedA.java").toURI(), emptyArray(), null)
        nonIncremental.add(File("GeneratedB.java").toURI(), emptyArray(), null)

        assertEquals(nonIncremental.getRuntimeType(), RuntimeProcType.NON_INCREMENTAL)
        assertEquals(nonIncremental.getGeneratedToSources(), emptyMap<File, String?>())
    }
}

