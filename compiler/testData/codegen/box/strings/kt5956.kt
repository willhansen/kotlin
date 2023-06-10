// KT-5956 java.lang.AbstractMethodError: test.Thing.subSequence(II)Ljava/lang/CharSequence

class Thing(konst delegate: CharSequence) : CharSequence {
    override fun get(index: Int): Char {
        throw UnsupportedOperationException()
    }
    override konst length: Int get() = 0
    override fun subSequence(start: Int, end: Int) = delegate.subSequence(start, end)
}

fun box(): String {
    konst txt = Thing("hello there")
    konst s = txt.subSequence(0, 1)
    return if ("$s" == "h") "OK" else "Fail: $s"
}
