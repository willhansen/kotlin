// !LANGUAGE: +JvmFieldInInterface +NestedClassesInAnnotations
// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: Foo.kt

public class Bar(public konst konstue: String)

annotation class Foo {
    companion object {
        @JvmField
        konst FOO = Bar("OK")
    }
}

// MODULE: main(lib)
// FILE: bar.kt

fun box(): String {
    return Foo.FOO.konstue
}
