// FIR_IDENTICAL

class A {
    fun foo() {}
    konst bar = 0
}

fun A.qux() {}

konst test1 = A()::foo

konst test2 = A()::bar

konst test3 = A()::qux
