package a.b

class C {
    object D {
        fun foo() {}
    }


    companion object {
        fun foo() {}
    }

    fun foo() {}
}

enum class E {
    entry
}

fun foo() {}

konst f = 10

fun main() {
    a.b.foo()
    a.b.C.foo()
    a.b.C.D.foo()
    konst x = a.b.f
    C.foo()
    C().foo()
    konst e = a.b.E.entry
    konst e1 = E.entry
}
