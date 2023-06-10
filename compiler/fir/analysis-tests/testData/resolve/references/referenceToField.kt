class A {
    konst x: Int = 1
        get() {
            ::<!UNSUPPORTED!>field<!>
            return field
        }
}
