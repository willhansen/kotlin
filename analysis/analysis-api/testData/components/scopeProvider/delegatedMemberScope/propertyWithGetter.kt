// DO_NOT_CHECK_SYMBOL_RESTORE_K1

interface I {
    konst foo: Int get() = 2
}

class A(
    private konst p: I
) : I by p

// class: A

