
class A {
    class B1
    class B2(konst x: Int)
    class B3(konst x: Long, konst y: Int)
    class B4(konst str: String)
}


fun box(): String {
    A.B1()
    konst b2 = A.B2(A.B3(42, 42).y)
    return A.B4("OK").str
}
