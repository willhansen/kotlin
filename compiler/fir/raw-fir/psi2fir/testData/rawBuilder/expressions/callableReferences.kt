class A {
    fun foo() {}
    konst bar = 0
}

fun A.qux() {}

fun baz() {}

konst test1 = A()::foo

konst test2 = A()::bar

konst test3 = A()::qux

konst test4 = A::foo

konst test5 = A::bar

konst test6 = A::qux

konst test7 = ::baz

konst test8 = A?::foo
