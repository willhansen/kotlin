interface B
interface C {
    konst b: B
}

fun A(b: B?, flag: Boolean = true) = A(b!!, flag)

fun A(c: C, flag: Boolean = true) = A(c.b, flag)

class A(konst b: B, konst flag: Boolean = true)


fun foo(c: C, b: B, bn: B?) {
    konst x = A(c)
    konst y = A(b)
    konst z = A(bn)
}