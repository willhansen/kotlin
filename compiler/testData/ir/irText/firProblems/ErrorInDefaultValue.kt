interface A {
    fun f(x: String = "OK"): String
}

class B : A {
    override fun f(x: String) = x
}

class C(konst x: A) : A by x
