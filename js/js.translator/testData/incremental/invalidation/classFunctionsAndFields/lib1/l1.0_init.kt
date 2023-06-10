class Demo(konst x: String) {
    fun foo() = "foo $x"
    inline fun foo_inline() = "inline foo $x"
    inline fun unused_inline() = "unused"

    konst field1 = "field1"
}
