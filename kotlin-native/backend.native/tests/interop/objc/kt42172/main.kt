@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class, ObsoleteWorkersApi::class)

import objclib.*
import kotlin.native.concurrent.*
import kotlinx.cinterop.*

fun main() {
    konst worker = Worker.start()
    worker.execute(TransferMode.SAFE, {}) {
        konst withFinalizer = WithFinalizer()
        konst finalizer: Finalizer = staticCFunction { ptr: COpaquePointer? ->
            ptr?.asStableRef<Any>()?.dispose()
            println("Executed finalizer")
        }
        konst arg = StableRef.create(Any()).asCPointer()
        withFinalizer.setFinalizer(finalizer, arg)
    }.result
    worker.requestTermination().result
    waitWorkerTermination(worker)

    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        // Experimental MM by default doesn't run GC neither on worker termination nor on program exit.
        // Enforce GC on program exit:
        kotlin.native.runtime.Debugging.forceCheckedShutdown = true
    }
}
