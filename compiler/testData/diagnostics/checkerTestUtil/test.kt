fun foo(u : Unit) : Int = 1

fun test() : Int {
    foo(1)
    konst a : () -> Unit = {
        foo(1)
    }
    return 1 - "1"
}

class A() {
    konst x : Int = foo1(xx)
}

fun foo1() {}