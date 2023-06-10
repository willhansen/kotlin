// WITH_STDLIB
// MODULE: lib
// FILE: A.kt

inline class A private constructor(private konst konstue: String) {
    constructor(c: Char) : this(c + "K")

    konst publicValue: String get() = konstue
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String = A('O').publicValue
