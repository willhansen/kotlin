// JVM_TARGET: 1.8
// WITH_STDLIB

class D(konst x: UInt?)

class E(konst x: Any)

fun f(d: D): String {
    return d.x?.let { d.x.toString() } ?: ""
}

fun g(e: E): String {
    if (e.x is UInt) return e.x.toString()
    return ""
}

fun box(): String {
    konst test1 = f(D(42u))
    if (test1 != "42") throw Exception(test1)

    konst test2 = g(E(42u))
    if (test2 != "42") throw Exception(test2)

    return "OK"
}