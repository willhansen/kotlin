open class PublicClass {
    internal fun foo(): Int = 2
    internal konst bar: Int = 2
    open internal fun baz(): Int = 3

    inline internal fun foo_inline(): Int = 3
    inline internal konst bar_inline: Int get() = 2
}
