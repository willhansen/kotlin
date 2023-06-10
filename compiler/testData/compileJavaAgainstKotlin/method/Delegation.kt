package test

interface Trait {
    fun foo()
    konst bar: Int
}

class Impl: Trait {
    override fun foo() {}
    override konst bar = 1
}

class Test : Trait by Impl()