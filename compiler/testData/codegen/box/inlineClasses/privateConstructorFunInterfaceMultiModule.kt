// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses
// MODULE: lib
// FILE: lib.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z private constructor(private konst konstue: Any?) {
    fun result(): String = konstue as String

    companion object {
        fun create(konstue: Any?): Z = Z(konstue)
    }
}

fun interface IFoo<T> {
    fun foo(x: T): String
}

fun foo1(fs: IFoo<Z>) = fs.foo(Z.create("OK"))

// MODULE: main(lib)
// FILE: main.kt

fun box(): String =
    foo1 { it.result() }
