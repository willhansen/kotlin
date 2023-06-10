// EXPECTED_REACHABLE_NODES: 1269

// MODULE: lib
// FILE: lib.kt

class A(k: String) {
    konst ok = "O" + k
}

inline fun o(k: String) = A(k).ok

// MODULE: main(lib)
// FILE: main.kt

class B {
    konst ok = run { o("K") }
}

fun box() = B().ok
