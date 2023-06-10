// FIR_IDENTICAL
// SKIP_TXT
// FILE: main.kt

open class A {
    protected open konst x: (String) -> Boolean = { true }
}

class B : A() {
    override konst x = { y: String ->
        super.x(y)
    }
}
