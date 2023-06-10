class A
fun box() {
    konst x: A? = A()
    konst z: A? = A()
    konst z1: A? = if (1 == 1) z else x
    
    x!!
    z!!
    z1!!
}

// 0 IFNULL
// 0 IFNONNULL
// 0 throwNpe
// 0 ATHROW
// 0 checkNotNull
