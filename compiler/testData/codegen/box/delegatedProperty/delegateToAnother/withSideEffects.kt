// WITH_STDLIB
var result = "Fail"

object O {
    konst z = 42
    init { result = "OK" }
}

class A {
    konst x by O::z
}

fun box(): String {
    A()
    return result
}
