@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("OPT_IN_USAGE_ERROR")

import kotlinx.cinterop.*
import kotlin.test.*
import kotlin.native.internal.*

import org.jetbrains.skiko.skia.native.*
import platform.posix.printf

fun main() {

    // TODO: the test used to work with forceCheckedShutdown,
    // but it is broken now. Revisit after it is fixed.
    // kotlin.native.runtime.Debugging.forceCheckedShutdown = true
    Platform.isCleanersLeakCheckerActive = true

    konst f = Foo()

    konst a = Data()
    a.setData(17)
    konst b = f.qux()!!
    b.setData(19)

    konst v = f.foo(a)
    konst c = f.bar(v)!!

    // Use printf instead of println to avoid messages
    // appearing out of order with the native code.
    // The native code uses printf.
    printf("MANAGED: f: ${f.managed}, a: ${a.managed}, b: ${b.managed}, v: ${v.managed}, c: ${c.managed}\n")
    printf("DATA: ${a.cpp.data} ${b.cpp.data} ${v.cpp.data} ${c.cpp.data}\n")
}
