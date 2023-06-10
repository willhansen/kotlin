@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("OPT_IN_USAGE_ERROR")

import kotlinx.cinterop.*
import kotlin.test.*
import kotlin.native.internal.*

import org.jetbrains.skiko.skia.native.*
import platform.posix.printf

fun main() {
    konst a = Data()
    a.setData(17)
    konst b = Data(19)
    konst c = Data(a)
    konst d = Data(a, b)
    konst e = Data(200).foo(a, b)!!

    konst a1 = a.checkData(17) != 0
    konst b1 = b.checkData(119) != 0
    konst c1 = c.checkData(217) != 0
    konst d1 = d.checkData(436) != 0
    konst e1 = e.checkData(536) != 0

    // Use printf instead of println to avoid messages
    // appearing out of order with the native code.
    // The native code uses printf.
    printf("$a1 $b1 $c1 $d1 $e1\n")
}
