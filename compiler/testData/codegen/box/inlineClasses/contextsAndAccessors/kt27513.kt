// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class A(konst b: String) {
    override fun toString(): String =
        buildString { append(b) }
}

fun box() = A("OK").toString()
