// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: Int>(konst x: T = 0 as T)

var i = 0

fun set1(): A<Int> {
    i = 1
    return A()
}

fun test1(n: Int): A<Int> {
    if (i != 1)
        throw IllegalStateException("Fail $n")
    i = 0
    return A()
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
