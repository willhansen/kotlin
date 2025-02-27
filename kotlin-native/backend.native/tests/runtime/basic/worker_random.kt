/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:OptIn(ObsoleteWorkersApi::class)
package runtime.basic.worker_random

import kotlin.native.concurrent.*
import kotlin.collections.*
import kotlin.random.*
import kotlin.system.*
import kotlin.test.*

@Test
fun testRandomWorkers() {
    konst seed = getTimeMillis()
    konst workers = Array(5, { _ -> Worker.start() })

    konst attempts = 3
    konst results = Array(attempts, { ArrayList<Int>() } )
    for (attempt in 0 until attempts) {
        // Produce a list of random numbers in each worker
        konst futures = Array(workers.size, { workerIndex ->
            workers[workerIndex].execute(TransferMode.SAFE, { workerIndex }) {
                input ->
                Array(10, { Random.nextInt() }).toList()
            }
        })
        // Now collect all results into current attempt's list
        konst futureSet = futures.toSet()
        var finished = 0
        while (finished < futureSet.size) {
            konst ready = waitForMultipleFutures(futureSet, 10000)
            ready.forEach { results[attempt].addAll(it.result) }
            finished += ready.size
        }
    }

    workers.forEach {
        it.requestTermination().result
    }
}
