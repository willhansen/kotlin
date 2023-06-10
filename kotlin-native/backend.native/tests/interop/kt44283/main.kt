@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

import kotlinx.cinterop.*
import kt44283.*
import kotlin.native.concurrent.AtomicInt
import kotlin.test.*

konst callbackCounter = AtomicInt(0)

fun main() {
    konst func = staticCFunction<CValue<TestStruct>, Unit> {
        kotlin.native.runtime.GC.collect() // Helps to ensure that "runtime" is already initialized.

        memScoped {
            println("Hello, Kotlin/Native! ${it.ptr.pointed.d}")
        }
        callbackCounter.increment()
    }

    assertEquals(0, callbackCounter.konstue)
    invokeFromThread(func.reinterpret())
    assertEquals(1, callbackCounter.konstue)
}