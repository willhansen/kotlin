open class B : A<Int> {
    override fun test() = 1

    override konst testProp: Int = 3

    override fun testWithDefault(x: Int) = x + 1
}
