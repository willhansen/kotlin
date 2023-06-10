// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
open class A {
    companion object {
        fun foo() = 1
        fun bar(a: String) = a
    }
}

open class B: A() {
    companion object {
        fun foo() = ""
        fun bar(a: Int) = a
    }
}

class C: B() {
    init {
        konst a: String = foo()
        konst b: Int = bar(1)
        konst c: String = bar("")
    }
}