// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers

class A {
    konst a = 1
}

class B {
    konst b = 2
}

class C {
    konst c = 3
}

context(A, B) fun C.f() {}

fun main(a: A, b: B, c: C) {
    with(a) {
        with(b) {
            with(c) {
                f()
            }
        }
    }
    with(b) {
        with(c) {
            with(a) {
                f()
            }
        }
    }
    with(a) {
        with(c) {
            <!NO_CONTEXT_RECEIVER!>f<!>()
        }
    }
}
