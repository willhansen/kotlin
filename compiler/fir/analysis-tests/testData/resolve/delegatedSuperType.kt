interface A {
    fun foo()
}

class B : A {
    override fun foo() {}
}

class C(konst b: B) : A by b
