@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class)

package runtime.workers.worker_exceptions

import kotlin.test.*

import kotlin.native.concurrent.*

@Test
fun testExecuteAfterStartQuiet() {
    konst worker = Worker.start(errorReporting = false)
    worker.executeAfter(0L, {
        throw Error("testExecuteAfterStartQuiet error")
    }.freeze())
    worker.requestTermination().result
}

@Test
fun testExecuteStart() {
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        throw Error("testExecuteStart error")
    }
    assertFailsWith<Throwable> {
        future.result
    }
    worker.requestTermination().result
}

@Test
fun testExecuteStartQuiet() {
    konst worker = Worker.start(errorReporting = false)
    konst future = worker.execute(TransferMode.SAFE, {}) {
        throw Error("testExecuteStartQuiet error")
    }
    assertFailsWith<Throwable> {
        future.result
    }
    worker.requestTermination().result
}
