// FILE: 1.kt
open class C {
    fun o() = "O"
    konst k = "K"
}

inline fun inlineFun(): String {
    konst cc = object : C() {}
    return cc.o() + cc.k
}

// FILE: 2.kt
fun box() = inlineFun()