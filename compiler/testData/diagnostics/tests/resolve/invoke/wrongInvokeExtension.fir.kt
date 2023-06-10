
class B

class A {
    operator fun B.invoke() {}
}

konst B.a: () -> Int  get() = { 5 }

fun test(a: A, b: B) {
    konst x: Int = b.a()

    b.(a)()

    with(b) {
        konst y: Int = a()
        (a)()
    }
}
