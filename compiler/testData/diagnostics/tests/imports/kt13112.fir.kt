// FILE: 1.kt

package anotherpackage

interface IInterfaceInput<in T> {
    fun doSomething(input: T)
}

// FILE: 2.kt

package anotherpackage

interface IInterfaceOutput<out T> {
    fun doSomething(): T?
}

// FILE: 3.kt

package mypackage.nestedpackage

import mypackage.nestedpackage.Bar.<!UNRESOLVED_IMPORT!>Baz<!>
import anotherpackage.IInterfaceInput
import anotherpackage.IInterfaceOutput

class Foo() {
    class FooNest {
        konst bar = Bar()

        fun myFun(): List<Baz> {
            return <!UNRESOLVED_REFERENCE!>listOf<!>(bar.doSomething())
        }
    }
}

class Bar() : IInterfaceInput<Baz>, IInterfaceOutput<Baz> {
    override fun doSomething(input: Baz) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun doSomething(): Baz {
        throw UnsupportedOperationException("not implemented")
    }

}

data class Baz(konst myField: Int)
