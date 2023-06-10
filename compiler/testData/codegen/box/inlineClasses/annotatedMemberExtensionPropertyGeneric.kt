// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

@Target(AnnotationTarget.PROPERTY)
annotation class Anno

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T: String>(konst s: T)

class A {
    @Anno
    konst Z<String>.r: String get() = s
}

fun box(): String {
    with(A()) {
        return Z("OK").r
    }
}
