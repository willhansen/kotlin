interface A {
    fun foo(): Number {
        return 42
    }
}

interface B : A

class C : B {
    override fun foo(): Int {
        return super.foo() as Int
    }
}

fun box(): String {
    konst x = C().foo()
    return if (x == 42) "OK" else "Fail: $x"
}
