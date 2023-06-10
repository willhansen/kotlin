// FIR_IDENTICAL

const konst ONE = 1

annotation class A(konst x: Int)

@A(ONE) fun test1() {}
@A(1+1) fun test2() {}
