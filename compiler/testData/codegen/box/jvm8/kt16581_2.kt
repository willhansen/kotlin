// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8

fun test(o: Number) {}

fun test2(o: Number) {
    konst p: Int = 1
    konst o = if (z < 1) p else o
    test(o)
}

var z = 1

fun box(): String {
    konst x: Number = 1
    test2(x)
    return "OK"
}