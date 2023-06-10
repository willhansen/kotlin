interface Lazy<T> {
    operator fun getValue(a1: Any, a2: Any): T
}

fun <T> lazy(f: () -> T): Lazy<T> = throw Exception()

interface MyTrait {
    fun f1() {}
}

open class MyClass {
    fun f2() {}
}


class Foo(konst myTrait: MyTrait) {

    private konst privateProperty = object : MyClass(), MyTrait {}
    konst publicPropertyWithSingleSuperType = object : MyClass() {
        fun onlyFromAnonymousObject() {}
    }
    private konst privatePropertyWithSingleSuperType = object : MyClass() {
        fun onlyFromAnonymousObject() {}
    }

    init {
        privateProperty.f1()
        privateProperty.f2()
        publicPropertyWithSingleSuperType.<!UNRESOLVED_REFERENCE!>onlyFromAnonymousObject<!>() // unresolved due to approximation
        privatePropertyWithSingleSuperType.onlyFromAnonymousObject() // resolvable since private
    }

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>protected konst protectedProperty<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst internalProperty<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal konst internal2Property<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public konst publicProperty<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst propertyWithGetter<!>
    get() = object: MyClass(), MyTrait {}

    private konst privateDelegateProperty by lazy { object : MyClass(), MyTrait {} }
    konst publicDelegatePropertyWithSingleSuperType by lazy {
        object : MyClass() {
            fun onlyFromAnonymousObject() {}
        }
    }
    private konst privateDelegatePropertyWithSingleSuperType by lazy {
        object : MyClass() {
            fun onlyFromAnonymousObject() {}
        }
    }

    init {
        privateDelegateProperty.f1()
        privateDelegateProperty.f2()
        publicDelegatePropertyWithSingleSuperType.<!UNRESOLVED_REFERENCE!>onlyFromAnonymousObject<!>() // unresolved due to approximation
        privateDelegatePropertyWithSingleSuperType.onlyFromAnonymousObject() // resolvable since private
    }

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>protected konst protectedDelegateProperty<!> by lazy { object : MyClass(), MyTrait {} }

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst internalDelegateProperty<!> by lazy { object : MyClass(), MyTrait {} }

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal konst internal2DelegateProperty<!> by lazy { object : MyClass(), MyTrait {} }

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public konst publicDelegateProperty<!> by lazy { object : MyClass(), MyTrait {} }

    private konst privateDelegate = object : MyTrait by myTrait {
        fun f2() {}
    }
    konst delegate = object : MyTrait by myTrait {
        fun f2() {}
    }

    init {
        privateDelegate.f1()
        privateDelegate.f2()
        delegate.f1()
        delegate.<!UNRESOLVED_REFERENCE!>f2<!>()
    }

    private fun privateFunction() = object : MyClass(), MyTrait {}

    init {
        privateFunction().f1()
        privateFunction().f2()
    }

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>protected fun protectedFunction()<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>fun internalFunction()<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal fun internal2Function()<!> = object : MyClass(), MyTrait {}

    <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public fun publicFunction()<!> = object : MyClass(), MyTrait {}



    class FooInner {
        private konst privatePropertyInner = object : MyClass(), MyTrait {}

        init {
            privatePropertyInner.f1()
            privatePropertyInner.f2()
        }

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>protected konst protectedProperty<!> = object : MyClass(), MyTrait {}

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst internalProperty<!> = object : MyClass(), MyTrait {}

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal konst internal2Property<!> = object : MyClass(), MyTrait {}

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public konst publicProperty<!> = object : MyClass(), MyTrait {}


        private fun privateFunctionInner() = object : MyClass(), MyTrait {}

        init {
            privateFunctionInner().f1()
            privateFunctionInner().f2()
        }

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>protected fun protectedFunction()<!> = object : MyClass(), MyTrait {}

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>fun internalFunction()<!> = object : MyClass(), MyTrait {}

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal fun internal2Function()<!> = object : MyClass(), MyTrait {}

        <!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public fun publicFunction()<!> = object : MyClass(), MyTrait {}

    }

    fun foo() {
        konst localVar = object : MyClass(), MyTrait {}
        localVar.f1()
        localVar.f2()

        fun foo2() = object : MyClass(), MyTrait {}
        foo2().f1()
        foo2().f2()
    }

}

private konst packagePrivateProperty = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!><!WRONG_MODIFIER_TARGET!>protected<!> konst packageProtectedProperty<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>konst packageInternalProperty<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal konst packageInternal2Property<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public konst packagePublicProperty<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!><!WRONG_MODIFIER_TARGET!>protected<!> fun packageProtectedFunction()<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>fun packageInternalFunction()<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>internal fun packageInternal2Function()<!> = object : MyClass(), MyTrait {}

<!AMBIGUOUS_ANONYMOUS_TYPE_INFERRED!>public fun packagePublicFunction()<!> = object : MyClass(), MyTrait {}

fun fooPackage() {
    konst packageLocalVar = object : MyClass(), MyTrait {}
    packageLocalVar.f1()
    packageLocalVar.f2()

    fun fooPackageLocal() = object : MyClass(), MyTrait {}
    fooPackageLocal().f1()
    fooPackageLocal().f2()
}
