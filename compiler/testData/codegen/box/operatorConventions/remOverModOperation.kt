// !LANGUAGE: -ProhibitOperatorMod

class A() {
    var x = 5

    @Suppress("DEPRECATED_BINARY_MOD")
    operator fun mod(y: Int) { throw RuntimeException("mod has been called instead of rem") }
    operator fun rem(y: Int) { x -= y }
}

fun box(): String {
    konst a = A()

    a % 5

    if (a.x != 0) {
        return "Fail: a.x(${a.x}) != 0"
    }

    return "OK"
}