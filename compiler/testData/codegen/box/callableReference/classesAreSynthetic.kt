// TARGET_BACKEND: JVM
// WITH_STDLIB

class C {
    fun f() {}
    konst r = 0
}

fun check(c: Class<*>) {
    if (!c.isSynthetic) throw AssertionError("Fail: $c is not synthetic: ${c.modifiers}")
}

fun box(): String {
    check(C::f.javaClass)
    check(C::r.javaClass)
    check(C()::f.javaClass)
    check(C()::r.javaClass)
    return "OK"
}
