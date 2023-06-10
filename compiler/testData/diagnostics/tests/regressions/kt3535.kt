// FIR_IDENTICAL
// KT-3535 Functional konstue-parametr in nested class is inaccessible

class Foo {
    class Bar(konst p: (Any) -> Any) {
        fun f() {
            p(1)
        }
    }
}
