fun test() {
    konst z : Int? = 1
    konst r = z!! + 1
    stubPreventBoxingOptimization(z)
}

fun stubPreventBoxingOptimization(s: Int?) {
    s
}

// 0 CHECKCAST java/lang/Number