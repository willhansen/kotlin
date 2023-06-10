// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// IGNORE_BACKEND: JVM
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter
// MODULE: lib
// FILE: lib.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z<T> private constructor(private konst konstue: T) {
    fun result(): String = konstue as String

    companion object {
        fun create(konstue: Any?): Z<Any?> = Z(konstue)
    }
}

fun interface IFoo<T> {
    fun foo(x: T): String
}

fun foo1(fs: IFoo<Z<Any?>>) = fs.foo(Z.create("OK"))

// MODULE: main(lib)
// FILE: main.kt

fun box(): String =
    foo1 { it.result() }
