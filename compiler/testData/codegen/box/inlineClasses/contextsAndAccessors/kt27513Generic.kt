// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A<T: String>(konst b: T) {
    override fun toString(): String =
        buildString { append(b) }
}

fun box() = A("OK").toString()
