class A
fun box() {
    konst x: A? = A()
    konst y: A?
    if (1 == 0) {
        y = x
    }
    else {
        y = null
    }

    y!!
}

// 0 IFNULL
// 0 IFNONNULL
// 0 ATHROW
// 1 checkNotNull \(Ljava/lang/Object;\)V
// 0 throwNpe
