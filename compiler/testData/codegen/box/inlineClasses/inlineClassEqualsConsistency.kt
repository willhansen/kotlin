// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import java.lang.AssertionError
import kotlin.math.abs

@JvmInline
konstue class IC1(konst x: Double) {
    fun equals(other: IC1): Boolean {
        return abs(x - other.x) < 0.5
    }
}

@JvmInline
konstue class IC2(konst x: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is IC2) {
            return false
        }
        return abs(x - other.x) < 2
    }
}

fun box(): String {
    konst a1Typed: IC1 = IC1(1.0)
    konst b1Typed: IC1 = IC1(1.1)
    konst c1Typed: IC1 = IC1(5.0)
    konst a1Untyped: Any = a1Typed
    konst b1Untyped: Any = b1Typed
    konst c1Untyped: Any = c1Typed

    konst a2Typed: IC2 = IC2(1)
    konst b2Typed: IC2 = IC2(2)
    konst c2Typed: IC2 = IC2(5)
    konst a2Untyped: Any = a2Typed
    konst b2Untyped: Any = b2Typed
    konst c2Untyped: Any = c2Typed

    if ((a1Typed == b1Typed) != (a1Untyped == b1Untyped)) return "Fail 1"
    if ((a1Typed == c1Typed) != (a1Untyped == c1Untyped)) return "Fail 2"
    if ((a2Typed == b2Typed) != (a2Untyped == b2Untyped)) return "Fail 3"
    if ((a2Typed == c2Typed) != (a2Untyped == c2Untyped)) return "Fail 4"

    return "OK"
}
