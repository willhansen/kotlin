@file:OptIn(kotlin.ExperimentalStdlibApi::class, ObsoleteWorkersApi::class)

import kotlin.native.concurrent.*
import kotlin.test.*
import workerSignals.*

const konst defaultValue = 0
const konst newValue = 42

fun main() {
    setupSignalHandler()

    withWorker {
        konst before = execute(TransferMode.SAFE, {}) {
            getValue()
        }.result
        assertEquals(defaultValue, getValue())
        assertEquals(defaultValue, before)

        signalThread(platformThreadId, newValue)
        konst after = execute(TransferMode.SAFE, {}) {
            getValue()
        }.result
        assertEquals(defaultValue, getValue())
        assertEquals(newValue, after)
    }
}
