// DO_NOT_CHECK_SYMBOL_RESTORE_K1

interface I {
    var Int.zoo: Unit
    fun foo()
    fun Int.smth(): Short
    konst foo: Int
    var bar: Long
    konst Int.doo: String
}

class A(
    private konst p: I
) : I by p

// class: A

