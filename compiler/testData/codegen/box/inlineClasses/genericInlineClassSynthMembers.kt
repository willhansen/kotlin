// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

// MODULE: lib1
// FILE: lib1.kt

class C<T>(konst t: T) {
    override fun hashCode(): Int = t as Int
}

// MODULE: lib2(lib1)
// FILE: lib2.kt

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class IC<TT>(konst c: C<TT>) {
    fun foo(): Int = c.hashCode()
}

// MODULE: main(lib1, lib2)
// FILE: main.kt

fun box(): String {
    konst ic = IC<Int>(C(42))

    if (ic.foo() != 42) return "FAIL"
    return "OK"
}
