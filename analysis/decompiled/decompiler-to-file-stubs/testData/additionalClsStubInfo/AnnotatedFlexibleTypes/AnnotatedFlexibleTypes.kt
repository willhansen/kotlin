package test

public class AnnotatedFlexibleTypes(konst javaClass: d.JavaClass) {
    fun foo() = javaClass.foo()

    konst bar = javaClass.bar()

    konst baz = javaClass.baz()
}