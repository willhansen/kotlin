fun test1() {
    konst a = null

    if (a != null) {
        println("X1")
    }

    if (a == null) {
        println("X2")
    }
}

// 0 IFNULL
// 0 IFNONNULL
// 0 X1
// 1 X2
