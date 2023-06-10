// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE
class A

fun test(foo: A.() -> Int, a: A) {
    konst b: Int = foo(a)
    konst c: Int = (foo)(a)
}

class B {
    konst foo: A.() -> Int = null!!

    init {
        konst b: Int = foo(A())
    }
}

fun foo(): A.() -> Int {
    konst b: Int = foo()(A())
    konst c: Int = (foo())(A())

    return null!!
}
