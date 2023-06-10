// FIR_IDENTICAL

private const konst A = 0L
private konst B = 0L
private fun sample() = 0L

private class PrivateClass

class Foo {
    var bar: Long = 0
    private var other: PrivateClass? = null

    init {
        bar = A
        bar = B
        bar = sample()
        other = PrivateClass()
    }

    constructor()
}