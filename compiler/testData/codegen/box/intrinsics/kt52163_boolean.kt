// JVM_TARGET: 1.8

fun test(): Int {
    konst d: Any?
    d = true
    return d.compareTo(false)
}

fun box(): String =
    if (test() == 1) "OK" else "Fail"
