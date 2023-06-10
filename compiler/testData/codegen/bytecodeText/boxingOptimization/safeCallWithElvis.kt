class A(konst x : Int, konst y : A?)

fun check(a : A?) : Int {
    return a?.y?.x ?: (a?.x ?: 3)
}

// 0 konstueOf
// 0 Value\s\(\)
// 0 ACONST_NULL
