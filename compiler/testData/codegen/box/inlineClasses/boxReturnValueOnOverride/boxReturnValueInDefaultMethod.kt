// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

interface X<T> {
    operator fun plus(n: Int) : T
    fun next(): T = this + 1
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst konstue: Int) : X<A> {
    override operator fun plus(n: Int) = A(konstue + n)
}

fun box(): String {
    konst res = A(1).next()
    return if (res.konstue == 2) "OK" else "FAIL $res"
}
