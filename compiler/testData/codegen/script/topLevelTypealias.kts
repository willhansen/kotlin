class SimpleClass(konst s: String) {
    fun foo() = s
}

typealias Test = SimpleClass

konst rv = Test("OK").foo()

// expected: rv: OK
