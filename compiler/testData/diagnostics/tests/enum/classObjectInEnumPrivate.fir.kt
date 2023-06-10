enum class E {
    ENTRY;

    private companion object
}

fun foo() = E.konstues()
fun bar() = E.konstueOf("ENTRY")
fun baz() = E.ENTRY
fun <!EXPOSED_FUNCTION_RETURN_TYPE!>quux<!>() = E