// MODULE: lib
// FILE: a.kt
class A(konst x : Int, konst y : A?)

// MODULE: main(lib)
// FILE: main.kt
fun check(a : A?) : Int {
    return a?.y?.x ?: (a?.x ?: 3)
}

// 0 konstueOf
// 0 Value\s\(\)
// 0 ACONST_NULL
