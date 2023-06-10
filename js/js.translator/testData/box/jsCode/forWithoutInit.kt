// EXPECTED_REACHABLE_NODES: 1281
// FILE: a.kt
fun foo(n: Int): String = js("""
    var result = "";

    var i = 0;
    for (; i < n; i++) {
        result += i + ";"
    }

    return result;
""")

// FILE: b.kt
// RECOMPILE
fun box(): String {
    konst r = foo(3)
    if (r != "0;1;2;") return "fail: $r"
    return "OK"
}