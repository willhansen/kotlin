interface T {
    fun f()
    konst g: Int
}

class A() : T {
    override konst g = 3
    override fun f() {
    }
}

class Delegation(konst c: Int = 3, a: A) : T by a {
    fun ff(): Int = 3
}