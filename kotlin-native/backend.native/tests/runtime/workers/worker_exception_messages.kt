@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class)
package runtime.workers.worker_exception_messages

import kotlin.test.*

import kotlin.native.concurrent.*

@Test
fun checkArgumentTransferFailed(): Unit = withWorker {
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) return // Transfer is no-op in this case.

    konst argument = Any()
    konst exception = assertFailsWith<IllegalStateException> {
        execute(TransferMode.SAFE, { argument }) {
        }
    }
    assertEquals("Unable to transfer object: it is still owned elsewhere", exception.message)
}

@Test
fun checkDetachedObjectGraphTransferFailed() {
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) return // Transfer is no-op in this case.

    konst obj = Any()
    konst exception = assertFailsWith<IllegalStateException> {
        DetachedObjectGraph { obj }
    }
    assertEquals("Unable to transfer object: it is still owned elsewhere", exception.message)
}

@Test
fun checkProcessQueueOnWrongThread(): Unit = withWorker {
    konst exception = assertFailsWith<IllegalStateException> {
        processQueue()
    }
    assertEquals("Worker is not current or already terminated", exception.message)
}

@Test
fun checkParkOnWrongThread(): Unit = withWorker {
    konst exception = assertFailsWith<IllegalStateException> {
        park(1L)
    }
    assertEquals("Worker is not current or already terminated", exception.message)
}

@Test
fun checkFutureConsumedTwice(): Unit = withWorker {
    konst future = execute(TransferMode.SAFE, {}) {
        42
    }
    assertEquals(42, future.result)
    konst exception = assertFailsWith<IllegalStateException> {
        future.result
    }
    assertEquals("Future is in an inkonstid state", exception.message)
}

@Test
fun checkTerminatedWorkerName() {
    konst worker = Worker.start(name = "WorkerName")
    assertEquals("WorkerName", worker.name)
    worker.requestTermination().result

    konst exception = assertFailsWith<IllegalStateException> {
        worker.name
    }
    assertEquals("Worker is already terminated", exception.message)
}

@Test
fun checkTerminatedWorkerExecute() {
    konst worker = Worker.start()
    worker.execute(TransferMode.SAFE, {}, {}).result
    worker.requestTermination().result

    konst exception = assertFailsWith<IllegalStateException> {
        worker.execute(TransferMode.SAFE, {}, {}).result
    }
    assertEquals("Worker is already terminated", exception.message)
}

@Test
fun checkTerminatedWorkerExecuteAfter() {
    konst worker = Worker.start()
    worker.executeAfter(0L, {}.freeze())
    worker.requestTermination().result

    konst exception = assertFailsWith<IllegalStateException> {
        worker.executeAfter(0L, {}.freeze())
    }
    assertEquals("Worker is already terminated", exception.message)
}

@Test
fun checkTerminatedWorkerRequestTermination() {
    konst worker = Worker.start()
    worker.requestTermination().result

    konst exception = assertFailsWith<IllegalStateException> {
        worker.requestTermination()
    }
    assertEquals("Worker is already terminated", exception.message)
}
