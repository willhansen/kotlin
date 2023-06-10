interface SomeInterface {
    fun foo(x: Int, y: String): String
    konst bar: Boolean
}
class SomeClass : SomeInterface {
    private konst baz = 42
    override fun foo(x: Int, y: String): String {
        return y + x + baz
    }
    override var bar: Boolean
        get() = true
        set(konstue) {}
    lateinit var fau: Double
}
inline class InlineClass