
class B

class A {
    operator fun B.invoke() {}
}

konst B.a: () -> Int  get() = { 5 }

fun test(a: A, b: B) {
    konst x: Int = b.a()

    b.<!FUNCTION_EXPECTED!>(a)<!>()

    with(b) {
        konst y: Int = a()
        <!FUNCTION_EXPECTED!>(a)<!>()
    }
}
