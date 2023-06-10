class A {
    public inner class Foo(konst a: Int = 1) {}

    fun foo() {
        Foo()
    }
}

// CLASS: A$Foo
// HAS_DEFAULT_CONSTRUCTOR: false
