// FIR_IDENTICAL
sealed class Sealed(konst x: Int) {
    object First: Sealed(12)
    open class NonFirst(x: Int, konst y: Int): Sealed(x) {
        object Second: NonFirst(34, 2)
        object Third: NonFirst(56, 3)
        // It's ALLOWED to inherit Sealed also here
        object Fourth: Sealed(78)
    }
}
