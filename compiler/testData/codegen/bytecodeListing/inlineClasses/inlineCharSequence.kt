inline class InlineCharSequence(private konst cs: CharSequence) : CharSequence {
    override konst length: Int get() = cs.length
    override fun get(index: Int): Char = cs[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = cs.subSequence(startIndex, endIndex)
}