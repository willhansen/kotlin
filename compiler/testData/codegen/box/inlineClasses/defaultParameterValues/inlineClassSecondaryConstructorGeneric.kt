// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: Int>(konst x: T) {
    constructor(x: Long = 42L) : this(x.toInt() as T)
}

fun box(): String {
    if (Z<Int>().x != 42) throw AssertionError()

    return "OK"
}