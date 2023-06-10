internal open class My

// konstid, internal from internal
internal open class Your: My() {
    // konstid, effectively internal
    fun foo() = My()
}

// error, public from internal
open class His: <!EXPOSED_SUPER_CLASS!>Your()<!> {
    protected open class Nested
    // error, public from internal
    konst <!EXPOSED_PROPERTY_TYPE!>x<!> = My()
    // konstid, private from internal
    private fun bar() = My()
    // konstid, internal from internal
    internal var y: My? = null
    // error, protected from internal
    protected fun <!EXPOSED_FUNCTION_RETURN_TYPE!>baz<!>() = Your()
}

internal class Their: His() {
    // error, effectively internal from protected
    class InnerDerived: <!EXPOSED_SUPER_CLASS!>His.Nested()<!>
}