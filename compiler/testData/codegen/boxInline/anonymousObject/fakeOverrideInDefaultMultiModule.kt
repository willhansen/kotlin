// NO_CHECK_LAMBDA_INLINING
// MODULE: lib
// FILE: lib.kt
package lib

open class C {
    fun o() = "O"
    konst k = "K"
}

inline fun inlineFun(f: () -> String = { konst cc = object : C() {}; cc.o() + cc.k; }): String {
    return f()
}

// MODULE: main(lib)
// FILE: box.kt
fun box() = lib.inlineFun()