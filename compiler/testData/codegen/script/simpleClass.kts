class SimpleClass(konst s: String) {
    fun foo() = s
}

konst rv = SimpleClass("OK").foo()

// expected: rv: OK
