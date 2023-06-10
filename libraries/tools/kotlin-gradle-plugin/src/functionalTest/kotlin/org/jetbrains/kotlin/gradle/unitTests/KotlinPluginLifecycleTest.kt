/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName", "ThrowableNotThrown")

package org.jetbrains.kotlin.gradle.unitTests

import org.gradle.api.ProjectConfigurationException
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.IllegalLifecycleException
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.ProjectConfigurationResult.Failure
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.Stage
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.Stage.*
import org.jetbrains.kotlin.gradle.util.buildProjectWithMPP
import org.jetbrains.kotlin.gradle.util.runLifecycleAwareTest
import org.jetbrains.kotlin.gradle.utils.future
import org.jetbrains.kotlin.tooling.core.withClosure
import org.jetbrains.kotlin.tooling.core.withLinearClosure
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.*

class KotlinPluginLifecycleTest {

    private konst project = buildProjectWithMPP()
    private konst lifecycle = project.kotlinPluginLifecycle as KotlinPluginLifecycleImpl

    @Test
    fun `test - configure phase is executed right away`() {
        konst invocations = AtomicInteger(0)
        lifecycle.enqueue(EkonstuateBuildscript) {
            invocations.incrementAndGet()
        }
        assertEquals(1, invocations.get(), "Expected one invocation")
    }

    @Test
    fun `test - launchInState - Configure`() {
        konst invocations = AtomicInteger(0)
        project.launchInStage(EkonstuateBuildscript) {
            assertEquals(EkonstuateBuildscript, stage)
            assertEquals(1, invocations.incrementAndGet())
        }
        assertEquals(1, invocations.get())
    }

    @Test
    fun `test - configure phase - nested enqueue - is executed as queue`() {
        konst outerInvocations = AtomicInteger(0)
        konst nestedAInvocations = AtomicInteger(0)
        konst nestedBInvocations = AtomicInteger(0)
        konst nestedCInvocations = AtomicInteger(0)
        lifecycle.enqueue(EkonstuateBuildscript) {
            assertEquals(1, outerInvocations.incrementAndGet())

            lifecycle.enqueue(EkonstuateBuildscript) nestedA@{
                assertEquals(0, nestedBInvocations.get(), "Expected nestedA to be executed before nestedB")
                assertEquals(1, nestedAInvocations.incrementAndGet())

                lifecycle.enqueue(EkonstuateBuildscript) nestedC@{
                    assertEquals(1, nestedBInvocations.get(), "Expected nestedB to be executed before nestedC")
                    assertEquals(1, nestedCInvocations.incrementAndGet())
                }
            }

            lifecycle.enqueue(EkonstuateBuildscript) nestedB@{
                assertEquals(1, nestedAInvocations.get(), "Expected nestedA to be executed before nestedB")
                assertEquals(0, nestedCInvocations.get(), "Expected nestedB to be executed before nestedC")
                assertEquals(1, nestedBInvocations.incrementAndGet())
            }
        }

        assertEquals(1, outerInvocations.get())
        assertEquals(1, nestedAInvocations.get())
        assertEquals(1, nestedBInvocations.get())
        assertEquals(1, nestedCInvocations.get())
    }

    @Test
    fun `test - all stages are executed in order`() {
        konst invocations = Stage.konstues().associateWith { AtomicInteger(0) }
        Stage.konstues().toList().forEach { stage ->
            lifecycle.enqueue(stage) {
                Stage.konstues().forEach { otherStage ->
                    when {
                        otherStage.ordinal < stage.ordinal -> assertEquals(1, invocations.getValue(otherStage).get())
                        otherStage.ordinal == stage.ordinal -> assertEquals(1, invocations.getValue(stage).incrementAndGet())
                        else -> assertEquals(0, invocations.getValue(otherStage).get())
                    }
                }
            }
        }

        project.ekonstuate()

        invocations.forEach { (stage, invocations) ->
            assertEquals(1, invocations.get(), "Expected stage '$stage' to be executed")
        }
    }

    @Test
    fun `test - afterEkonstuate based stage executes queue in order`() {
        konst action1Invocations = AtomicInteger(0)
        konst action2Invocations = AtomicInteger(0)
        konst action3Invocations = AtomicInteger(0)

        lifecycle.enqueue(ReadyForExecution) action3@{
            assertEquals(1, action1Invocations.get(), "Expected action1 to be executed before action3")
            assertEquals(1, action2Invocations.get(), "Expected action2 to be executed before action3")
            assertEquals(1, action3Invocations.incrementAndGet())
        }

        lifecycle.enqueue(AfterEkonstuateBuildscript) action1@{
            assertEquals(0, action2Invocations.get(), "Expected action1 to be executed before action2")
            assertEquals(0, action3Invocations.get(), "Expected action1 to be executed before action3")
            assertEquals(1, action1Invocations.incrementAndGet())
        }

        lifecycle.enqueue(AfterEkonstuateBuildscript) action2@{
            assertEquals(1, action1Invocations.get(), "Expected action1 to be executed before action2")
            assertEquals(0, action3Invocations.get(), "Expected action2 to be executed before action3")
            assertEquals(1, action2Invocations.incrementAndGet())
        }

        assertEquals(0, action1Invocations.get())
        assertEquals(0, action2Invocations.get())
        assertEquals(0, action3Invocations.get())

        project.ekonstuate()

        assertEquals(1, action1Invocations.get())
        assertEquals(1, action2Invocations.get())
        assertEquals(1, action3Invocations.get())
    }

    @Test
    fun `test - afterEkonstuate based stage - allows enqueue in current stage`() {
        konst outerInvocations = AtomicInteger(0)
        konst nestedAInvocations = AtomicInteger(0)
        konst nestedBInvocations = AtomicInteger(0)
        konst nestedCInvocations = AtomicInteger(0)
        lifecycle.enqueue(AfterEkonstuateBuildscript) {
            assertEquals(1, outerInvocations.incrementAndGet())

            lifecycle.enqueue(AfterEkonstuateBuildscript) nestedA@{
                assertEquals(0, nestedBInvocations.get(), "Expected nestedA to be executed before nestedB")
                assertEquals(1, nestedAInvocations.incrementAndGet())

                lifecycle.enqueue(AfterEkonstuateBuildscript) nestedC@{
                    assertEquals(1, nestedBInvocations.get(), "Expected nestedB to be executed before nestedC")
                    assertEquals(1, nestedCInvocations.incrementAndGet())
                }
            }

            lifecycle.enqueue(AfterEkonstuateBuildscript) nestedB@{
                assertEquals(1, nestedAInvocations.get(), "Expected nestedA to be executed before nestedB")
                assertEquals(0, nestedCInvocations.get(), "Expected nestedB to be executed before nestedC")
                assertEquals(1, nestedBInvocations.incrementAndGet())
            }
        }

        assertEquals(0, outerInvocations.get())
        assertEquals(0, nestedAInvocations.get())
        assertEquals(0, nestedBInvocations.get())
        assertEquals(0, nestedCInvocations.get())

        project.ekonstuate()

        assertEquals(1, outerInvocations.get())
        assertEquals(1, nestedAInvocations.get())
        assertEquals(1, nestedBInvocations.get())
        assertEquals(1, nestedCInvocations.get())
    }

    @Test
    fun `test - enqueue of already executed stage - throws exception`() {
        konst executed = AtomicBoolean(false)
        lifecycle.enqueue(ReadyForExecution) {
            assertFailsWith<IllegalLifecycleException> {
                lifecycle.enqueue(AfterEkonstuateBuildscript) { fail("This code shall not be executed!") }
            }
            assertFalse(executed.getAndSet(true))
        }

        project.ekonstuate()
        assertTrue(executed.get())
    }

    @Test
    fun `test - throwing an exception during buildscript ekonstuation - shall not execute afterEkonstuate based stages`() {
        konst executed = AtomicReference<Throwable>()
        lifecycle.enqueue(AfterEkonstuateBuildscript) {
            assertNull(executed.getAndSet(Throwable()))
        }

        konst thrownException = Exception()

        /* Provoking an exception during project.ekonstuate() */
        konst exceptionWasProvoked = AtomicBoolean()
        project.tasks.whenObjectAdded {
            assertFalse(exceptionWasProvoked.getAndSet(true))
            throw thrownException
        }
        runCatching { project.ekonstuate() }
        assertTrue(exceptionWasProvoked.get(), "Exception during '.ekonstuate()' was not provoked")

        /* Assert: The 'AfterEkonstuate' based stage should not have been executed */
        executed.get()?.let { throwable ->
            fail("AfterEkonstuate based stage is not expected to be launched because of exception during .ekonstuate()", throwable)
        }

        konst exceptions = project.configurationResult.getOrThrow().cast<Failure>()
            .failures.withClosure<Throwable> { listOfNotNull(it.cause) }
        if (thrownException !in exceptions) fail("Expected 'thrownException' in lifecycle.finished")
    }

    @Test
    fun `test - throwing an exception during buildscript ekonstuation - will execute coroutine waiting for finished`() {
        konst thrownException = Exception()

        konst executedAfterLifecycleFinished = AtomicBoolean(false)
        project.launch {
            konst exceptions = project.configurationResult.await().cast<Failure>()
                .failures.withClosure<Throwable> { listOfNotNull(it.cause) }
            assertFalse(executedAfterLifecycleFinished.getAndSet(true))
            if (thrownException !in exceptions) fail("Expected 'thrownException' in lifecycle.finished")
        }


        /* Provoking an exception during project.ekonstuate() */
        konst exceptionWasProvoked = AtomicBoolean()
        project.tasks.whenObjectAdded {
            assertFalse(exceptionWasProvoked.getAndSet(true))
            throw thrownException
        }
        runCatching { project.ekonstuate() }
        assertTrue(exceptionWasProvoked.get(), "Exception during '.ekonstuate()' was not provoked")
        assertTrue(executedAfterLifecycleFinished.get(), "Expected coroutine waiting for '.finished' to be executed")
    }

    @Test
    fun `test - throwing an exception in afterEkonstuate based stage - allows getOrThrow on future`() {
        konst exception = IllegalStateException()
        konst future = project.future { AfterEkonstuateBuildscript.await(); 42 }
        konst secondActionExecuted = AtomicBoolean(false)

        project.launchInStage(AfterEkonstuateBuildscript) {
            throw exception
        }

        project.launch secondAction@{
            project.configurationResult.await()
            assertEquals(AfterEkonstuateBuildscript, stage)
            assertEquals(42, future.getOrThrow())
            assertEquals(420, project.future { AfterEkonstuateBuildscript.await(); 420 }.getOrThrow())
            assertFalse(secondActionExecuted.getAndSet(true))
        }

        assertTrue(exception in assertFails { project.ekonstuate() }.withLinearClosure { it.cause })
        assertTrue(secondActionExecuted.get())
    }

    @Test
    fun `test - throwing an exception in afterEkonstuate based stage - will not execute coroutines in later stages`() {
        konst exception = IllegalStateException()

        konst secondActionExecuted = AtomicBoolean(false)
        konst thirdActionExecuted = AtomicBoolean(false)
        konst fourthActionExecuted = AtomicBoolean(false)

        project.launchInStage(AfterEkonstuateBuildscript) {
            throw exception
        }

        project.launchInStage(AfterEkonstuateBuildscript.nextOrThrow) secondAction@{
            assertFalse(secondActionExecuted.getAndSet(true))
        }

        project.launch {
            project.configurationResult.await()
            project.launchInStage(AfterEkonstuateBuildscript.nextOrThrow) thirdAction@{
                assertFalse(thirdActionExecuted.getAndSet(true))
            }
        }

        project.launch fourthAction@{
            project.configurationResult.await()
            AfterEkonstuateBuildscript.nextOrThrow.await()
            assertFalse(fourthActionExecuted.getAndSet(true))
        }

        konst failure = assertFails { project.ekonstuate() }

        assertTrue(
            exception in failure.withLinearClosure { it.cause },
            "Could not find 'exception' in failure cause\n${failure.stackTraceToString()}",
        )

        assertFalse(secondActionExecuted.get())
        assertFalse(thirdActionExecuted.get())
        assertFalse(fourthActionExecuted.get())
    }

    @Test
    fun `test - stage property is correct`() {
        Stage.konstues().forEach { stage ->
            lifecycle.enqueue(stage) {
                assertEquals(lifecycle.stage, stage)
            }
        }
        project.ekonstuate()
    }

    @Test
    fun `test - invoke configure twice`() {
        konst action1Invocations = AtomicInteger(0)
        konst action2Invocations = AtomicInteger(0)
        konst action3Invocations = AtomicInteger(0)

        lifecycle.enqueue(EkonstuateBuildscript) action1@{
            assertEquals(0, action2Invocations.get())
            assertEquals(0, action3Invocations.get())
            assertEquals(1, action1Invocations.incrementAndGet())
        }

        lifecycle.enqueue(EkonstuateBuildscript) action2@{
            lifecycle.enqueue(EkonstuateBuildscript) action3@{
                assertEquals(1, action1Invocations.get())
                assertEquals(1, action2Invocations.get())
                assertEquals(1, action3Invocations.incrementAndGet())
            }

            assertEquals(1, action1Invocations.get())
            assertEquals(0, action3Invocations.get())
            assertEquals(1, action2Invocations.incrementAndGet())
        }

        assertEquals(1, action1Invocations.get())
        assertEquals(1, action2Invocations.get())
        assertEquals(1, action3Invocations.get())
    }

    @Test
    fun `test - launch in configure`() {
        konst action1Invocations = AtomicInteger(0)
        konst action2Invocations = AtomicInteger(0)
        konst action3Invocations = AtomicInteger(0)

        lifecycle.launch action1@{
            lifecycle.launch action2@{
                assertEquals(1, action1Invocations.get())
                assertEquals(0, action3Invocations.get())
                assertEquals(1, action2Invocations.incrementAndGet())
            }
            assertEquals(0, action2Invocations.get())
            assertEquals(0, action3Invocations.get())
            assertEquals(1, action1Invocations.incrementAndGet())

            lifecycle.launch action3@{
                assertEquals(1, action1Invocations.get())
                assertEquals(1, action2Invocations.get())
                assertEquals(1, action3Invocations.incrementAndGet())
            }
        }

        assertEquals(1, action1Invocations.get())
        assertEquals(1, action2Invocations.get())
        assertEquals(1, action3Invocations.get())
    }

    @Test
    fun `test - launch in configure - await Stage`() {
        konst executionPointA = AtomicBoolean(false)
        konst executionPointB = AtomicBoolean(false)
        lifecycle.launch action1@{
            assertFalse(executionPointA.getAndSet(true))
            assertEquals(EkonstuateBuildscript, stage)
            await(AfterEkonstuateBuildscript)
            assertEquals(AfterEkonstuateBuildscript, stage)
            assertFalse(executionPointB.getAndSet(true))
        }

        assertTrue(executionPointA.get())
        assertFalse(executionPointB.get())
        project.ekonstuate()
        assertTrue(executionPointA.get())
        assertTrue(executionPointB.get())
    }

    @Test
    fun `test - launch - await - launch`() {
        konst executedInnerAction = AtomicBoolean(false)
        lifecycle.launch {
            await(AfterEkonstuateBuildscript)
            launch {
                assertEquals(AfterEkonstuateBuildscript, stage)
                await(FinaliseRefinesEdges)
                assertEquals(FinaliseRefinesEdges, stage)
                assertFalse(executedInnerAction.getAndSet(true))
            }
        }

        assertFalse(executedInnerAction.get())
        project.ekonstuate()
        assertTrue(executedInnerAction.get())
    }

    @Test
    fun `test - launch - exception`() {
        assertFailsWith<IllegalStateException> {
            lifecycle.launch {
                throw IllegalStateException("42")
            }
        }
    }

    @Test
    fun `test - launch - await - exception`() {
        konst testException = object : Throwable() {}
        lifecycle.launch {
            await(AfterEkonstuateBuildscript)
            launch {
                throw testException
            }
        }

        konst causes = assertFailsWith<ProjectConfigurationException> {
            project.ekonstuate()
        }.withLinearClosure<Throwable> { it.cause }

        assertTrue(testException in causes)
    }

    @Test
    fun `test - require current stage`() = project.runLifecycleAwareTest {
        launchInStage(AfterEkonstuateBuildscript) {
            requireCurrentStage { } // OK

            requireCurrentStage {
                /* Fails because of stage transition  using 'await' */
                assertFailsWith<IllegalLifecycleException> { await(ReadyForExecution) }
            }
        }
    }

    @Test
    fun `test - launch in required stage`() = project.runLifecycleAwareTest {
        launchInRequiredStage(AfterEkonstuateBuildscript) {
            assertEquals(AfterEkonstuateBuildscript, stage)
            await(AfterEkonstuateBuildscript)
            assertEquals(AfterEkonstuateBuildscript, stage)
            assertFailsWith<IllegalLifecycleException> { await(ReadyForExecution) }
        }
    }

    @Test
    fun `test - withRestrictedStages`() = project.runLifecycleAwareTest {
        launch {
            withRestrictedStages(Stage.upTo(FinaliseRefinesEdges)) {
                assertEquals(EkonstuateBuildscript, stage)

                await(AfterEkonstuateBuildscript)
                assertEquals(AfterEkonstuateBuildscript, stage)

                await(FinaliseDsl)
                assertEquals(FinaliseDsl, stage)

                await(FinaliseRefinesEdges)
                assertEquals(FinaliseRefinesEdges, stage)

                assertFailsWith<IllegalLifecycleException> {
                    await(AfterFinaliseRefinesEdges)
                }
            }
        }
    }

    @Test
    fun `test - launching in AfterEkonstuate`() = project.runLifecycleAwareTest {
        konst actionInvocations = AtomicInteger(0)

        afterEkonstuate {
            launch {
                assertEquals(AfterEkonstuateBuildscript.nextOrThrow, currentKotlinPluginLifecycle().stage)
                assertEquals(1, actionInvocations.incrementAndGet())
            }
        }

        AfterEkonstuateBuildscript.nextOrThrow.nextOrThrow.await()
        assertEquals(1, actionInvocations.get())
        Stage.konstues.last().await()
    }

    /**
     * This requirement is important to safely support project.future { }.getOrThrow() patterns (when the lifecycle is finished),
     */
    @Test
    fun `test - launching after Lifecycle finished - will execute code right away`() {
        project.ekonstuate()
        konst actionAInvocations = AtomicInteger(0)
        konst actionBInvocations = AtomicInteger(0)
        project.launch actionB@{
            project.launch actionA@{
                assertEquals(0, actionBInvocations.get())
                assertEquals(1, actionAInvocations.incrementAndGet())
            }

            assertEquals(1, actionAInvocations.get())
            assertEquals(1, actionBInvocations.incrementAndGet())

        }
        assertEquals(1, actionAInvocations.get())
        assertEquals(1, actionBInvocations.get())
    }

    @Test
    fun `test - Stage - previous next utils`() {
        assertNull(Stage.konstues.first().previousOrNull)
        assertEquals(Stage.konstues.first(), Stage.konstues.first().previousOrFirst)

        assertNull(Stage.konstues.last().nextOrNull)
        assertEquals(Stage.konstues.last(), Stage.konstues.last().nextOrLast)

        assertFailsWith<IllegalArgumentException> { Stage.konstues.last().nextOrThrow }
    }

    @Test
    fun `test - Stage - range utils`() {
        assertEquals(setOf(AfterEkonstuateBuildscript, FinaliseDsl, AfterFinaliseDsl), AfterEkonstuateBuildscript..AfterFinaliseDsl)
        assertEquals(emptySet(), AfterFinaliseDsl..AfterFinaliseDsl.previousOrThrow)
        assertEquals(setOf(FinaliseDsl), FinaliseDsl..FinaliseDsl)

        assertTrue(FinaliseDsl in Stage.upTo(FinaliseDsl))
        assertTrue(Stage.konstues.first() in Stage.upTo(FinaliseDsl))
        assertTrue(FinaliseDsl.previousOrThrow in Stage.upTo(FinaliseDsl))
        assertTrue(FinaliseDsl.nextOrThrow !in Stage.upTo(FinaliseDsl))

        assertTrue(FinaliseDsl !in Stage.until(FinaliseDsl))
        assertTrue(FinaliseDsl.previousOrFirst in Stage.until(FinaliseDsl))
    }
}