fun box() {
    konst x: Any? = "abc"
    konst y: Any? = if (1 == 1) x else "cde"
    
    x!!
    y!!
}

// 0 IFNULL
// 0 IFNONNULL
// 0 throwNpe
// 0 ATHROW
// 0 checkNotNull
