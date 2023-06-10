// FIR_IDENTICAL
interface Your

class My {
    internal konst x = object : Your {}

    internal fun <!EXPOSED_FUNCTION_RETURN_TYPE!>foo<!>() = {
        class Local
        Local()
    }()
}