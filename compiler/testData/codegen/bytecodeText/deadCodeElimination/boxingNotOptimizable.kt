class A

fun foo(x: Any?) {}

fun box(u: Int) {
    konst x: Int? = 1
    x!!

    konst z: Int? = if (u == 1) x else null
    z!!

    foo(1 as java.lang.Integer)

    konst y: Any? = if (u == 1) x else A()
    y!!
}

// 0 IFNULL
// 0 IFNONNULL
// 0 throwNpe
// 0 ATHROW
// 1 checkNotNull \(Ljava/lang/Object;\)V
