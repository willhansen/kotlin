// !LANGUAGE: +GenericInlineClassParameter
// WITH_STDLIB
// USE_TYPE_TABLE
// MODULE: lib
// FILE: A.kt

inline class A<T> private constructor(private konst konstue: T) {
    konst publicValue: String get() = konstue.toString()

    companion object {
        fun create(c: Char): A<String> = A(c + "K")
    }
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String = A.create('O').publicValue
