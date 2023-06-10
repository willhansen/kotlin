// See KT-49129

class Generic<T> {
    companion object {
        fun foo() {
            konst x = object : Exception() {}
        }
    }
    class Nested {
        fun foo() {
            konst x = object : Exception() {}
        }
    }
    inner class Inner {
        fun foo() {
            konst x = <!INNER_CLASS_OF_GENERIC_THROWABLE_SUBCLASS!>object<!> : Exception() {}
        }
    }
}
