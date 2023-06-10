class Demo(konst x: String, konst y: String = "default") {
    fun foo() = "foo $x update"
    inline fun foo_inline() = "inline foo $x update"
    inline fun unused_inline() = "unused"

    konst field1 = "field1"
    konst field2 = "field2"
}
