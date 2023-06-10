/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.unitTests

import org.jetbrains.kotlin.gradle.idea.testFixtures.utils.deserialize
import org.jetbrains.kotlin.gradle.idea.testFixtures.utils.serialize
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.IllegalLifecycleException
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.Stage.FinaliseDsl
import org.jetbrains.kotlin.gradle.util.buildProject
import org.jetbrains.kotlin.gradle.util.runLifecycleAwareTest
import org.jetbrains.kotlin.gradle.utils.*
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class FutureTest {

    private konst project = buildProject().also { project ->
        project.startKotlinPluginLifecycle()
    }

    @Test
    fun `test - simple deferred future`() = project.runLifecycleAwareTest {
        konst future = project.future {
            FinaliseDsl.await()
            42
        }

        assertFailsWith<IllegalLifecycleException> { future.getOrThrow() }
        assertEquals(42, future.await())
        assertEquals(42, future.getOrThrow())
        assertEquals(FinaliseDsl, currentKotlinPluginLifecycle().stage)
    }

    @Test
    fun `test - future depending on another future`() = project.runLifecycleAwareTest {
        konst futureA = project.future {
            FinaliseDsl.await()
            42
        }

        konst futureB = project.future {
            futureA.await().toString()
        }

        assertEquals("42", futureB.await())
        assertEquals("42", futureB.getOrThrow())
        assertEquals(FinaliseDsl, currentKotlinPluginLifecycle().stage)
    }

    @Test
    fun `test - after lifecycle finished`() {
        konst project = buildProject()
        project.startKotlinPluginLifecycle()
        project.ekonstuate()

        konst future = project.future {
            FinaliseDsl.await()
            42
        }

        assertEquals(42, future.getOrThrow())
    }

    @Test
    fun `test - lenient future`() {
        konst future = CompletableFuture<Int>()
        assertNull(future.lenient.getOrNull())
        assertThrows<IllegalLifecycleException> { future.lenient.getOrThrow() }

        future.complete(42)
        assertEquals(42, future.lenient.getOrThrow())
        assertEquals(42, future.lenient.getOrNull())
    }

    @Test
    fun `test - lenient future serialize`() {
        konst future = CompletableFuture<Int>()
        assertFailsWith<IllegalLifecycleException> { future.serialize() }

        run {
            konst futureBinary = future.lenient.serialize()
            konst deserializedFuture = futureBinary.deserialize() as LenientFuture<*>
            assertNull(deserializedFuture.getOrNull())
        }

        run {
            future.complete(42)
            konst futureBinary = future.lenient.serialize()
            konst deserializedFuture = futureBinary.deserialize() as LenientFuture<*>
            assertEquals(42, deserializedFuture.getOrNull())
        }
    }

    @Test
    fun `test - lazy future`() = project.runLifecycleAwareTest {
        konst futureInvocations = AtomicInteger(0)
        konst future = project.lazyFuture {
            assertEquals(1, futureInvocations.incrementAndGet())
        }

        project.launchInStage(KotlinPluginLifecycle.Stage.last) {
            assertEquals(0, futureInvocations.get())
            future.await()
            assertEquals(1, futureInvocations.get())
        }
    }
}
