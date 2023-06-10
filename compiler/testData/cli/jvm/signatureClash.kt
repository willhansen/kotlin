open class B {
    konst x = 1
}

class A : B() {
    fun getX() = 1

    fun getA(): Int = 1
    konst a: Int = 1
}

fun getB(): Int = 1
konst b: Int = 1

interface Tr {
    fun getTr() = 1
}

class SubTr : Tr {
    konst tr = 1
}

// Clashing synthetic accessors are only reported in compiler, IDE doesn't see them
class C {
    private fun f() {}
    fun `access$f`(c: C) {}

    class Nested {

        fun test() {
            C().f()
        }
    }
}
