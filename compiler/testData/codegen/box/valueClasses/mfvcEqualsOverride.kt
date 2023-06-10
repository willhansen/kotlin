// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import kotlin.math.abs

interface I {
    fun equals(param: MFVC): Boolean
}

@JvmInline
konstue class MFVC(konst konstue: Int, konst y: Int) : I {
    override fun equals(param: MFVC): Boolean {
        return abs(konstue - param.konstue) < 2
    }
}

fun box(): String {
    konst a1Typed: MFVC = MFVC(1, 2)
    konst b1Typed: MFVC = MFVC(2, 3)
    konst c1Typed: MFVC = MFVC(3, 4)
    konst a1Untyped: I = a1Typed
    konst b1Untyped: I = b1Typed
    konst c1Untyped: I = c1Typed

    require(a1Typed == a1Typed && a1Untyped == a1Untyped)
    require(a1Typed == b1Typed && a1Untyped == b1Untyped)
    require(a1Typed != c1Typed && a1Untyped != c1Untyped)
    require(b1Typed == a1Typed && b1Untyped == a1Untyped)
    require(b1Typed == b1Typed && b1Untyped == b1Untyped)
    require(b1Typed == c1Typed && b1Untyped == c1Untyped)
    require(c1Typed != a1Typed && c1Untyped != a1Untyped)
    require(c1Typed == b1Typed && c1Untyped == b1Untyped)
    require(c1Typed == c1Typed && c1Untyped == c1Untyped)
    
    return "OK"
}
