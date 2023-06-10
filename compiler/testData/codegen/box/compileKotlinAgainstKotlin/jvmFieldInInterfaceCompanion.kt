// !LANGUAGE: +JvmFieldInInterface
// TARGET_BACKEND: JVM
// WITH_STDLIB

// MODULE: lib
// FILE: Foo.kt

public class Bar(public konst konstue: String)

interface Foo {
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
