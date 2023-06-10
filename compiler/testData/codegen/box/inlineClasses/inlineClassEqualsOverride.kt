// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

import kotlin.math.abs

@JvmInline
konstue class IC1(konst konstue: Double) {
    fun equals(other: IC1): Boolean {
        return abs(konstue - other.konstue) < 0.1
    }
}

interface I {
    fun equals(param: IC2): Boolean
}

@JvmInline
konstue class IC2(konst konstue: Int) : I {
    override operator fun equals(param: IC2): Boolean {
        return abs(konstue - param.konstue) < 2
    }
}

@JvmInline
konstue class IC3(konst konstue: Int) {

}

@JvmInline
konstue class IC4(konst konstue: Int) {
    override fun equals(other: Any?) = TODO()
}

@JvmInline
konstue class IC5(konst konstue: Int) {
    operator fun equals(other: IC5): Nothing = TODO()
}

@JvmInline
konstue class IC6(konst konstue: Int) {
    override fun equals(other: Any?): Nothing = TODO()
}

inline fun <reified T> assertThrows(block: () -> Unit): Boolean {
    try {
        block.invoke()
    } catch (t: Throwable) {
        return t is T
    }
    return false
}


fun box() = when {
    IC1(1.0) != IC1(1.05) -> "Fail 1.1"
    (IC1(1.0) as Any) != IC1(1.05) -> "Fail 1.2"
    IC1(1.0) != (IC1(1.05) as Any) -> "Fail 1.3"
    (IC1(1.0) as Any) != (IC1(1.05) as Any) -> "Fail 1.4"

    IC1(1.0) == IC1(1.2) -> "Fail 2.1"
    (IC1(1.0) as Any) == IC1(1.2) -> "Fail 2.2"
    IC1(1.0) == (IC1(1.2) as Any) -> "Fail 2.3"
    (IC1(1.0) as Any) == (IC1(1.2) as Any) -> "Fail 2.4"

    IC2(5) != IC2(6) -> "Fail 3.1"
    (IC2(5) as Any) != IC2(6) -> "Fail 3.2"
    IC2(5) != (IC2(6) as Any) -> "Fail 3.3"
    (IC2(5) as Any) != (IC2(6) as Any) -> "Fail 3.4"

    IC2(5) == IC2(7) -> "Fail 4.1"
    (IC2(5) as Any) == IC2(7) -> "Fail 4.2"
    IC2(5) == (IC2(7) as Any) -> "Fail 4.3"
    (IC2(5) as Any) == (IC2(7) as Any) -> "Fail 4.4"

    IC3(5) != IC3(5) -> "Fail 5.1"
    (IC3(5) as Any) != IC3(5) -> "Fail 5.2"
    IC3(5) != (IC3(5) as Any) -> "Fail 5.3"
    (IC3(5) as Any) != (IC3(5) as Any) -> "Fail 5.4"

    IC3(5) == IC3(6) -> "Fail 6.1"
    (IC3(5) as Any) == IC3(6) -> "Fail 6.2"
    IC3(5) == (IC3(6) as Any) -> "Fail 6.3"
    (IC3(5) as Any) == (IC3(6) as Any) -> "Fail 6.4"

    IC1(1.0) == Any() -> "Fail 7.1"
    (IC1(1.0) as Any) == Any() -> "Fail 7.2"

    !assertThrows<NotImplementedError> { IC5(0) == IC5(1) } -> "Fail 8.1"
    !assertThrows<NotImplementedError> { IC6(0) == IC6(1) } -> "Fail 8.2"


    else -> "OK"
}
