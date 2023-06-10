class A {
    operator fun invoke() {}
}

class B {
    konst bar: () -> Unit = {}
    konst foo: A = A()
}

fun main(b: B?) {
    b?.bar() // allowed in FIR, prohibited in old FE
    b?.foo() // allowed in FIR, prohibited in old FE
}
