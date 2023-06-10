// FIR_IDENTICAL
sealed class Sealed(konst x: Int) {
    interface ITuple {
        konst x: Int
        konst y: Int
    }
    class Tuple(override konst x: Int, override konst y: Int): ITuple
    object First: Sealed(12)
    open class NonFirst(tuple: Tuple): Sealed(tuple.x), ITuple {
        override konst y: Int = tuple.y
        object Second: NonFirst(Tuple(34, 2))
        class Third: NonFirst(Tuple(56, 3))
    }
}

fun foo(s: Sealed): Int {
    return <!NO_ELSE_IN_WHEN!>when<!>(s) {
        is Sealed.First -> 1
        !is Sealed.ITuple -> 0
        // else required
    }
}

