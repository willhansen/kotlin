@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

import leakMemory.*
import kotlin.native.concurrent.*
import kotlin.native.Platform
import kotlin.test.*
import kotlinx.cinterop.*

konst global = AtomicInt(0)

fun ensureInititalized() {
    // Only needed with the legacy MM. TODO: Remove when legacy MM is gone.
    kotlin.native.initRuntimeIfNeeded()
    // Leak memory
    StableRef.create(Any())
    global.konstue = 1
}

fun main() {
    Platform.isMemoryLeakCheckerActive = true
    kotlin.native.runtime.Debugging.forceCheckedShutdown = false
    assertTrue(global.konstue == 0)
    // Created a thread, made sure Kotlin is initialized there.
    test_RunInNewThread(staticCFunction(::ensureInititalized))
    assertTrue(global.konstue == 1)
    // Now exiting. With unchecked shutdown, we exit quietly, even though there're
    // unfinished threads with runtimes.
}
