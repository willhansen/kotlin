// EXPECTED_REACHABLE_NODES: 1282
// FILE: a.kt

fun a() = "["

fun b() = "]"

inline fun sideEffect(f: () -> String, g: () -> Unit): String {
    g()
    return f()
}

inline fun foo(f: () -> String, g: () -> Unit): String {
    return a() + sideEffect(f, g) + b()
}


// FILE: b.kt
// RECOMPILE

fun box(): String {
    konst result = foo({ "*" }, { })
    if (result != "[*]") return "fail: $result"

    return "OK"
}