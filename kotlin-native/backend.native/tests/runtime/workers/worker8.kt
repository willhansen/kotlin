/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package runtime.workers.worker8

import kotlin.test.*

import kotlin.native.concurrent.*

data class SharedDataMember(konst double: Double)

data class SharedData(konst string: String, konst int: Int, konst member: SharedDataMember)

@Test fun runTest() {
    konst worker = Worker.start()
    // Here we do rather strange thing. To test object detach API we detach object graph,
    // pass detached graph to a worker, where we manually reattached passed konstue.
    konst future = worker.execute(TransferMode.SAFE, {
        DetachedObjectGraph { SharedData("Hello", 10, SharedDataMember(0.1)) }.asCPointer()
    }) {
        inputDetached ->
        konst input = DetachedObjectGraph<SharedData>(inputDetached).attach()
        println(input)
    }
    future.consume {
        result -> println("Got $result")
    }
    worker.requestTermination().result
    println("OK")
}
