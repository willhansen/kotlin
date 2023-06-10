// !LANGUAGE: +InlineClasses
// MODULE: lib
// FILE: A.kt

package a

inline class S(konst konstue: String) {
    inline konst k: String
        get() = konstue + "K"
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    return a.S("O").k
}
