class GenericTest {
    class A<T>

    class B<T> {
        konst memberVal: A<T> = A()
        fun memberFun(): A<T> = A()
    }

    konst <T> B<T>.extensionVal: A<T>
        get() = A()

    fun <T> B<T>.extensionFun(): A<T> = A()

    fun test_1() {
        konst memberValRef = B<*>::memberVal
        konst memberFunRef = B<*>::memberFun
    }

    fun test_2() {
        konst extensionValRef = B<*>::<!EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED!>extensionVal<!>
        konst extensionFunRef = B<*>::<!EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED!>extensionFun<!>
    }
}

class NoGenericTest {
    class A

    class B {
        konst memberVal: A = A()
        fun memberFun(): A = A()
    }

    konst B.extensionVal: A
        get() = A()

    fun B.extensionFun(): A = A()

    fun test_1() {
        konst extensionValRef = B::<!EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED!>extensionVal<!>
        konst extensionFunRef = B::<!EXTENSION_IN_CLASS_REFERENCE_NOT_ALLOWED!>extensionFun<!>
    }

    fun test_2() {
        konst memberValRef = B::memberVal
        konst memberFunRef = B::memberFun
    }
}
