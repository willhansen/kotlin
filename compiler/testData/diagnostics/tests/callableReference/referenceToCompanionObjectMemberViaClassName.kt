// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER

class A {
    companion object {
        fun foo(): Int = 0
    }
}

class B {
    fun foo(): String = ""

    companion object {
        fun foo(): Int = 0
    }
}

fun <T> call(f: () -> T): T = f()

fun testA(a: A) {
    konst call1 = call(A::foo)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>call1<!>

    konst call2 = call(A.Companion::foo)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>call2<!>
}

fun testB(b: B) {
    konst call1 = call(B::foo)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>call1<!>

    konst call2 = call(B()::foo)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.String")!>call2<!>

    konst call3 = call(B.Companion::foo)
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Int")!>call3<!>
}
