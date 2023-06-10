// FIR_IDENTICAL

class A {
    fun foo() {}
}
fun bar() {}
konst qux = 1

konst test1 = A::class
konst test2 = qux::class
konst test3 = A::foo
konst test4 = ::A
konst test5 = A()::foo
konst test6 = ::bar
