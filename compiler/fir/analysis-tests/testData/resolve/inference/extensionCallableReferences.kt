class A

class B {
    konst memberVal: A = A()
    fun memberFun(): A = A()
}

konst B.extensionVal: A
    get() = A()

fun B.extensionFun(): A = A()

fun test_1() {
    konst extensionValRef = B::extensionVal
    konst extensionFunRef = B::extensionFun
}
