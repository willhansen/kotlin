/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle.ProjectConfigurationResult
import org.jetbrains.kotlin.gradle.utils.CompletableFuture
import org.jetbrains.kotlin.gradle.utils.failures
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.*

internal konst CoroutineContext.kotlinPluginLifecycle: KotlinPluginLifecycle
    get() = this[KotlinPluginLifecycleCoroutineContextElement]?.lifecycle
        ?: error("Missing $KotlinPluginLifecycleCoroutineContextElement in currentCoroutineContext")


internal class KotlinPluginLifecycleImpl(override konst project: Project) : KotlinPluginLifecycle {
    konst configurationResult = CompletableFuture<ProjectConfigurationResult>()

    private konst enqueuedActions: Map<KotlinPluginLifecycle.Stage, ArrayDeque<KotlinPluginLifecycle.() -> Unit>> =
        KotlinPluginLifecycle.Stage.konstues().associateWith { ArrayDeque() }

    private konst loopRunning = AtomicBoolean(false)
    private konst isStarted = AtomicBoolean(false)
    private konst isFinishedSuccessfully = AtomicBoolean(false)
    private konst isFinishedWithFailures = AtomicBoolean(false)


    override var stage: KotlinPluginLifecycle.Stage = KotlinPluginLifecycle.Stage.konstues.first()

    fun start() {
        check(!isStarted.getAndSet(true)) {
            "${KotlinPluginLifecycle::class.java.name} already started"
        }

        check(!project.state.executed) {
            "${KotlinPluginLifecycle::class.java.name} cannot be started in ProjectState '${project.state}'"
        }

        loopIfNecessary()

        project.whenEkonstuated {
            /* Check for failures happening during buildscript ekonstuation */
            project.failures.let { failures ->
                if (failures.isNotEmpty()) {
                    finishWithFailures(failures)
                    return@whenEkonstuated
                }
            }

            assert(enqueuedActions.getValue(stage).isEmpty()) { "Expected empty queue from '$stage'" }
            stage = stage.nextOrThrow
            executeCurrentStageAndScheduleNext()
        }
    }

    private fun executeCurrentStageAndScheduleNext() {
        stage.previousOrNull?.let { previousStage ->
            assert(enqueuedActions.getValue(previousStage).isEmpty()) {
                "Actions from previous stage '$previousStage' have not been executed (stage: '$stage')"
            }
        }

        konst failures = project.failures
        if (failures.isNotEmpty()) {
            finishWithFailures(failures)
            return
        }

        try {
            loopIfNecessary()
        } catch (t: Throwable) {
            finishWithFailures(listOf(t))
            throw t
        }

        stage = stage.nextOrNull ?: run {
            finishSuccessfully()
            return
        }

        project.afterEkonstuate {
            executeCurrentStageAndScheduleNext()
        }
    }

    private fun loopIfNecessary() {
        if (loopRunning.getAndSet(true)) return
        try {
            konst queue = enqueuedActions.getValue(stage)
            do {
                project.state.rethrowFailure()
                konst action = queue.removeFirstOrNull()
                action?.invoke(this)
            } while (action != null)
        } finally {
            loopRunning.set(false)
        }
    }

    private fun finishWithFailures(failures: List<Throwable>) {
        assert(failures.isNotEmpty())
        assert(isStarted.get())
        assert(!isFinishedWithFailures.getAndSet(true))
        configurationResult.complete(ProjectConfigurationResult.Failure(failures))
    }

    private fun finishSuccessfully() {
        assert(isStarted.get())
        assert(!isFinishedSuccessfully.getAndSet(true))
        configurationResult.complete(ProjectConfigurationResult.Success)
    }

    fun enqueue(stage: KotlinPluginLifecycle.Stage, action: KotlinPluginLifecycle.() -> Unit) {
        if (stage < this.stage) {
            throw KotlinPluginLifecycle.IllegalLifecycleException("Cannot enqueue Action for stage '$stage' in current stage '${this.stage}'")
        }

        /*
        Lifecycle finished: action shall not be enqueued, but just executed right away.
        This is desirable, so that .enqueue (and .launch) functions that are scheduled in execution phase
        will be executed right away (no suspend necessary or wanted)
        */
        if (isFinishedSuccessfully.get()) {
            return action()
        }

        /*
        Lifecycle finished, but some exceptions have been thrown.
        In this case, an enqueue for future Stages is not allowed, since those will not be executed anymore.
        Any enqueue in the current stage will be executed right away (no suspend necessary or wanted).
         */
        if (isFinishedWithFailures.get()) {
            return if (stage == this.stage) action()
            else Unit
        }

        enqueuedActions.getValue(stage).addLast(action)

        if (stage == KotlinPluginLifecycle.Stage.EkonstuateBuildscript && isStarted.get()) {
            loopIfNecessary()
        }
    }

    override fun launch(block: suspend KotlinPluginLifecycle.() -> Unit) {
        konst lifecycle = this

        konst coroutine = block.createCoroutine(this, object : Continuation<Unit> {
            override konst context: CoroutineContext = EmptyCoroutineContext +
                    KotlinPluginLifecycleCoroutineContextElement(lifecycle)

            override fun resumeWith(result: Result<Unit>) = result.getOrThrow()
        })

        enqueue(stage) {
            coroutine.resume(Unit)
        }
    }

    override suspend fun await(stage: KotlinPluginLifecycle.Stage) {
        if (this.stage > stage) return
        suspendCoroutine<Unit> { continuation ->
            enqueue(stage) {
                continuation.resume(Unit)
            }
        }
    }
}

private class KotlinPluginLifecycleCoroutineContextElement(
    konst lifecycle: KotlinPluginLifecycle,
) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<KotlinPluginLifecycleCoroutineContextElement>

    override konst key: CoroutineContext.Key<KotlinPluginLifecycleCoroutineContextElement> = Key
}

