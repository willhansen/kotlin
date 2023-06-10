// IGNORE_BACKEND: JVM
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

interface X<T> {
    operator fun plus(n: Int) : T
    fun next(): T = this + 1
}

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Int>(konst konstue: T) : X<A<Int>> {
    override operator fun plus(n: Int): A<Int> = A(konstue + n)
}

fun box(): String {
    konst res = A(1).next()
    return if (res.konstue == 2) "OK" else "FAIL $res"
}
