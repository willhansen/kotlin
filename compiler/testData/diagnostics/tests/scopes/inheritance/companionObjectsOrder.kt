// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
open class A {
    companion object {
        fun bar() = 1
    }
    init {
        konst a: Int = foo()
        konst b: Int = bar()
    }
}

open class B: A() {
    companion object {
        fun bar() = ""
    }
    init {
        konst a: String = foo()
        konst b: String = bar()
    }
}

fun A.Companion.foo() = 1
fun B.Companion.foo() = ""

class C: A() {
    init {
        konst a: Int = foo()
        konst b: Int = bar()
    }
}

class D: B() {
    init {
        konst a: String = foo()
        konst b: String = bar()
    }
}