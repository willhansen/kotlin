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

    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var fau: Double<!>
}
