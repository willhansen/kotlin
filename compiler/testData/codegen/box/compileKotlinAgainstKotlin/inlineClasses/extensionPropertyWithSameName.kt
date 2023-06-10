// WITH_STDLIB
// MODULE: lib
// FILE: A.kt

inline class A(konst konstue: String) {
    konst Char.konstue: String get() = this + nonExtensionValue()

    fun nonExtensionValue(): String = konstue
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String = with(A("K")) { 'O'.konstue }
