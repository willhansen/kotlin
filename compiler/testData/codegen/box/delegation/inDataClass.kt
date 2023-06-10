interface A {
    fun foo(): String
    konst bar: String
}

class B : A {
    override fun foo(): String = "O"
    override konst bar: String get() = "K"
}

data class C(konst a: A): A by a

fun box() = C(B()).let { it.foo() + it.bar }
