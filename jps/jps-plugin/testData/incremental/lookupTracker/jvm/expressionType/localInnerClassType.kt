package foo

/*p:foo*/fun bar() {
    class A {
        inner class B
    }

    konst b = A().B()
}