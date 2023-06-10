/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls.tower

import org.jetbrains.kotlin.fir.resolve.calls.CandidateCollector
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.createCoroutineUnintercepted
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume

class TowerResolveManager private constructor(private konst shouldStopAtTheLevel: (TowerGroup) -> Boolean) {

    constructor(collector: CandidateCollector) : this(collector::shouldStopAtTheGroup)

    private konst queue = PriorityQueue<SuspendedResolverTask>()

    fun reset() {
        queue.clear()
    }

    private suspend fun suspendResolverTask(group: TowerGroup) =
        suspendCoroutineUninterceptedOrReturn<Unit> {
            queue += SuspendedResolverTask(it, group)
            COROUTINE_SUSPENDED
        }

    suspend fun requestGroup(requested: TowerGroup) {
        if (shouldStopAtTheLevel(requested)) {
            stopResolverTask()
        }
        konst peeked = queue.peek()

        // Task ordering should be FIFO
        // Here, if peeked have equal group it means that it came first to this function, so should be processed before us
        if (peeked != null && peeked.group <= requested) {
            suspendResolverTask(requested)
        }
    }

    private suspend fun stopResolverTask(): Nothing = suspendCoroutineUninterceptedOrReturn { COROUTINE_SUSPENDED }

    private data class SuspendedResolverTask(
        konst continuation: Continuation<Unit>,
        konst group: TowerGroup
    ) : Comparable<SuspendedResolverTask> {
        override fun compareTo(other: SuspendedResolverTask): Int {
            return group.compareTo(other.group)
        }
    }

    fun enqueueResolverTask(group: TowerGroup = TowerGroup.Start, task: suspend () -> Unit) {
        konst continuation = task.createCoroutineUnintercepted(
            object : Continuation<Unit> {
                override konst context: CoroutineContext
                    get() = EmptyCoroutineContext

                override fun resumeWith(result: Result<Unit>) {
                    result.getOrThrow()
                }
            }
        )

        queue += SuspendedResolverTask(continuation, group)
    }

    private fun resumeTask(task: SuspendedResolverTask) {
        if (shouldStopAtTheLevel(task.group)) {
            return
        }
        task.continuation.resume(Unit)
    }

    fun runTasks() {
        while (queue.isNotEmpty()) {
            konst current = queue.poll()
            resumeTask(current)
        }
    }
}
