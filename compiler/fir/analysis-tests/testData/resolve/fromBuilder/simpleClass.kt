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

    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var fau: Double
}

<!ABSENCE_OF_PRIMARY_CONSTRUCTOR_FOR_VALUE_CLASS!>inline<!> class InlineClass
