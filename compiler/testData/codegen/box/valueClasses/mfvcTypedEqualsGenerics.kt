// WITH_STDLIB
// LANGUAGE: +ValueClasses, +CustomEqualsInValueClasses
// TARGET_BACKEND: JVM_IR
// CHECK_BYTECODE_LISTING

@JvmInline
konstue class MFVC1<T : Number>(konst x: T, konst other: Int) {
    fun equals(x: Int, other: Int) = false
    fun equals(other: MFVC1<*>) = true
}

class Generic<T, R>(konst x: T, konst y: R)

@JvmInline
konstue class MFVC2<T, R>(konst konstue: Generic<T, R>, konst other: Int) {
    fun equals(konstue: MFVC1<Double>, other: Int) = false
    fun equals(other: MFVC2<*, *>) = true
}

@JvmInline
konstue class MFVC3<T>(konst konstue: T, konst other: Int) {
    fun equals(konstue: Int, other: Int) = false
    fun equals(other: MFVC3<*>) = true
}

@JvmInline
konstue class MFVC4<T>(konst konstue: T, konst other: Int) {
    fun equals(konstue: Any, other: Int) = false
    fun equals(other: MFVC4<*>) = true
}


fun box() = when {
    MFVC1(5.0, 100) != MFVC1(3, 100) -> "Fail 1.1"
    (MFVC1(5.0, 100) as Any) != MFVC1(3, 100) -> "Fail 1.2"
    MFVC1(5.0, 100) != (MFVC1(3, 100) as Any) -> "Fail 1.3"
    (MFVC1(5.0, 100) as Any) != (MFVC1(3, 100) as Any) -> "Fail 1.4"

    MFVC2(Generic("aba", 5.0), 100) != MFVC2(Generic(3, 8), 100) -> "Fail 2.1"
    (MFVC2(Generic("aba", 5.0), 100) as Any) != MFVC2(Generic(3, 8), 100) -> "Fail 2.2"
    MFVC2(Generic("aba", 5.0), 100) != (MFVC2(Generic(3, 8), 100) as Any) -> "Fail 2.3"
    (MFVC2(Generic("aba", 5.0), 100) as Any) != (MFVC2(Generic(3, 8), 100) as Any) -> "Fail 2.4"

    MFVC3("x", 100) != MFVC3("y", 100) -> "Fail 3.1"
    (MFVC3("x", 100) as Any) != MFVC3("y", 100) -> "Fail 3.2"
    MFVC3("x", 100) != (MFVC3("y", 100) as Any) -> "Fail 3.3"
    (MFVC3("x", 100) as Any) != (MFVC3("y", 100) as Any) -> "Fail 3.4"

    MFVC4("aba", 100) != MFVC4("caba", 100) -> "Fail 4.1"
    (MFVC4("aba", 100) as Any) != MFVC4("caba", 100) -> "Fail 4.2"
    MFVC4("aba", 100) != (MFVC4("caba", 100) as Any) -> "Fail 4.3"
    (MFVC4("aba", 100) as Any) != (MFVC4("caba", 100) as Any) -> "Fail 4.4"

    else -> "OK"
}
