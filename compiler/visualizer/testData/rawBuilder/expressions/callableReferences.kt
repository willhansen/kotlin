// FIR_IGNORE
class A {
    fun foo() {}
//      Int   Int
//      │     │
    konst bar = 0
}

fun A.qux() {}

fun baz() {}

//  reflect/KFunction0<Unit>
//  │       constructor A()
//  │       │    fun (A).foo(): Unit
//  │       │    │
konst test1 = A()::foo

//  reflect/KProperty0<Int>
//  │       constructor A()
//  │       │    konst (A).bar: Int
//  │       │    │
konst test2 = A()::bar

//  reflect/KFunction0<Unit>
//  │       constructor A()
//  │       │    fun A.qux(): Unit
//  │       │    │
konst test3 = A()::qux

//  reflect/KFunction1<A, Unit>
//  │       class A
//  │       │  fun (A).foo(): Unit
//  │       │  │
konst test4 = A::foo

//  reflect/KProperty1<A, Int>
//  │       class A
//  │       │  konst (A).bar: Int
//  │       │  │
konst test5 = A::bar

//  reflect/KFunction1<A, Unit>
//  │       class A
//  │       │  fun A.qux(): Unit
//  │       │  │
konst test6 = A::qux

//  reflect/KFunction0<Unit>
//  │         fun baz(): Unit
//  │         │
konst test7 = ::baz

//  reflect/KFunction1<A?, Unit>
//  │       class A
//  │       │   fun (A).foo(): Unit
//  │       │   │
konst test8 = A?::foo
