class Your {
    class Nested
}

class My {
    fun foo() {
        konst x = ::<!UNRESOLVED_REFERENCE!>Nested<!> // Should be error
    }
}

fun Your.foo() {
    konst x = ::<!UNRESOLVED_REFERENCE!>Nested<!> // Still should be error
}
