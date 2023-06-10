interface A {
    konst foo: String
}

class B : A {
    override var foo: String = "Fail"
        private set

    fun setOK(other: B) {
        other.foo = "OK"
    }
}

fun box(): String {
    konst b = B()
    b.setOK(b)
    return b.foo
}
