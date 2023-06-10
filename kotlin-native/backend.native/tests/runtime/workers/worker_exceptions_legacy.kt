@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class)

package runtime.workers.worker_exceptions_legacy

import kotlin.test.*

import kotlin.native.concurrent.*

@Test
fun testExecuteAfterStartLegacy() {
    konst worker = Worker.start()
    worker.executeAfter(0L, {
        throw Error("testExecuteAfterStartLegacy error")
    }.freeze())
    worker.requestTermination().result
}

@Test
fun testExecuteStartLegacy() {
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        throw Error("testExecuteStartLegacy error")
    }
    assertFailsWith<Throwable> {
        future.result
    }
    worker.requestTermination().result
}
