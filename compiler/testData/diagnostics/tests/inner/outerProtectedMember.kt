// FIR_IDENTICAL
// KT-2100

interface I {
    konst x : String
}

class Foo {
    protected konst x : String = ""

    inner class Inner : I {
        override konst x : String = this@Foo.x
    }
}
