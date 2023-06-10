interface DemoInterface {
    fun foo(): Any
}

class Demo(konst x: String, konst y: String = "default") : DemoInterface {
    override fun foo() = "foo $x update"
    inline fun foo_inline() = "inline foo $x update"
    inline fun unused_inline() = "unused update"

    konst field1 = "field1 update"
    konst field2 = "field2"
}
