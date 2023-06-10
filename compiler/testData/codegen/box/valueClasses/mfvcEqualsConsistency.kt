// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import java.lang.AssertionError
import kotlin.math.abs

@JvmInline
konstue class MFVC1(konst x: Int, konst y: Int) {
    fun equals(other: MFVC1): Boolean {
        return abs(x - other.x) < 2 && abs(y - other.y) < 2
    }
}

@JvmInline
konstue class MFVC2(konst x: Int, konst y: Int) {
    override fun equals(other: Any?): Boolean {
        if (other !is MFVC2) {
            return false
        }
        return abs(x - other.x) < 2 && abs(y - other.y) < 2
    }
}

fun box(): String {
    konst a1Typed: MFVC1 = MFVC1(1, 2)
    konst b1Typed: MFVC1 = MFVC1(2, 3)
    konst c1Typed: MFVC1 = MFVC1(3, 4)
    konst a1Untyped: Any = a1Typed
    konst b1Untyped: Any = b1Typed
    konst c1Untyped: Any = c1Typed

    konst a2Typed: MFVC2 = MFVC2(1, 2)
    konst b2Typed: MFVC2 = MFVC2(2, 3)
    konst c2Typed: MFVC2 = MFVC2(3, 4)
    konst a2Untyped: Any = a2Typed
    konst b2Untyped: Any = b2Typed
    konst c2Untyped: Any = c2Typed

    require(a1Typed == a1Typed && a1Untyped == a1Untyped)
    require(a1Typed == b1Typed && a1Untyped == b1Untyped)
    require(a1Typed != c1Typed && a1Untyped != c1Untyped)
    require(b1Typed == a1Typed && b1Untyped == a1Untyped)
    require(b1Typed == b1Typed && b1Untyped == b1Untyped)
    require(b1Typed == c1Typed && b1Untyped == c1Untyped)
    require(c1Typed != a1Typed && c1Untyped != a1Untyped)
    require(c1Typed == b1Typed && c1Untyped == b1Untyped)
    require(c1Typed == c1Typed && c1Untyped == c1Untyped)

    require(a2Typed == a2Typed && a2Untyped == a2Untyped)
    require(a2Typed == b2Typed && a2Untyped == b2Untyped)
    require(a2Typed != c2Typed && a2Untyped != c2Untyped)
    require(b2Typed == a2Typed && b2Untyped == a2Untyped)
    require(b2Typed == b2Typed && b2Untyped == b2Untyped)
    require(b2Typed == c2Typed && b2Untyped == c2Untyped)
    require(c2Typed != a2Typed && c2Untyped != a2Untyped)
    require(c2Typed == b2Typed && c2Untyped == b2Untyped)
    require(c2Typed == c2Typed && c2Untyped == c2Untyped)

    return "OK"
}
