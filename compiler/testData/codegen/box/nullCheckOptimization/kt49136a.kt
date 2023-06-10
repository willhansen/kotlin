// IGNORE_BACKEND: NATIVE

abstract class Z {
    init {
        check(this)
    }

    abstract konst b: B
}

class A(override konst b: B) : Z()

class B(konst c: String)

fun use(a: Any?) {}

fun check(z: Z) {
    use(z?.b?.c)
}

fun box(): String {
    A(B(""))
    return "OK"
}
