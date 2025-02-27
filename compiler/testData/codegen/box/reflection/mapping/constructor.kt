// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.jvm.*

class K {
    class Nested
    inner class Inner
}

class Secondary {
    constructor(x: Int) {}
}

fun check(f: KFunction<Any>) {
    assert(f.javaMethod == null) { "Fail f method" }
    assert(f.javaConstructor != null) { "Fail f constructor" }
    konst c = f.javaConstructor!!

    assert(c.kotlinFunction != null) { "Fail m function" }
    konst ff = c.kotlinFunction!!

    assert(f == ff) { "Fail f != ff" }
}

fun box(): String {
    check(::K)
    check(K::Nested)
    check(K::Inner)
    check(::Secondary)

    return "OK"
}
