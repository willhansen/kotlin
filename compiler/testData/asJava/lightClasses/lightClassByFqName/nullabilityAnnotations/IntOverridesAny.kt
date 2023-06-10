// C

interface Tr {
    fun foo(): Any
    konst v: Any
}

class C: Tr {
    override fun foo() = 1
    override konst v = { 1 }()
}