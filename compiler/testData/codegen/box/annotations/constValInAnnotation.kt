// !LANGUAGE: +NestedClassesInAnnotations
// WITH_STDLIB
// TARGET_BACKEND: JVM
// FILE: Foo.java

@Anno(Anno.CONST)
public class Foo {}

// FILE: Anno.kt

annotation class Anno(konst konstue: Int) {
    companion object {
        const konst CONST = 42
    }
}

fun box(): String =
        if ((Foo::class.java.annotations.single() as Anno).konstue == 42) "OK" else "Fail"
