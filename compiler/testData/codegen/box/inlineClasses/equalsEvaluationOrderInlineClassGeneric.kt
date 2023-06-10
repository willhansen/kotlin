// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Inner<T: Int>(konst x: T)

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Inner<Int>>(konst x: T)

var i = 0

fun set1(): A<Inner<Int>> {
    i = 1
    return A(Inner(0))
}

fun test1(n: Int): A<Inner<Int>> {
    if (i != 1)
        throw IllegalStateException("Fail $n")
    i = 0
    return A(Inner(0))
}

fun set1Boxed(): Any? = set1()
fun test1Boxed(n: Int): Any? = test1(n)

fun box(): String {
    try {
        set1() == test1(1)
        set1Boxed() == test1(2)
        set1() == test1Boxed(3)
        set1Boxed() == test1Boxed(4)
    } catch (e: IllegalStateException) {
        return e.message ?: "Fail no message"
    }
    return "OK"
}
