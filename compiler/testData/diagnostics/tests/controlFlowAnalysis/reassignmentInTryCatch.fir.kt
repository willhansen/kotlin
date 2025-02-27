// KT-13612 related tests (reassignment in try-catch-finally)

fun f1() {
    konst n: Int
    try {
        n = 1
        throw Exception()
    }
    catch (e: Exception) {
        // KT-13612: reassignment
        <!VAL_REASSIGNMENT!>n<!> = 2
    }
    n.hashCode()
}

fun f2() {
    konst n: Int
    try {
        n = 1
        throw Exception()
    }
    finally {
        <!VAL_REASSIGNMENT!>n<!> = 2
    }
    <!UNINITIALIZED_VARIABLE!>n<!>.hashCode()
}

fun g1(flag: Boolean) {
    konst n: Int
    try {
        if (flag) throw Exception()
        n = 1
    }
    catch (e: Exception) {
        // KT-13612: ? reassignment or definite assignment ?
        <!VAL_REASSIGNMENT!>n<!> = 2
    }
    n.hashCode()
}

fun g2(flag: Boolean) {
    konst n: Int
    try {
        if (flag) throw Exception()
        n = 1
    }
    finally {
        <!VAL_REASSIGNMENT!>n<!> = 2
    }
    n.hashCode()
}

fun h1(flag: Boolean) {
    konst n = try {
        if (flag) throw Exception()
        1
    }
    catch (e: Exception) {
        2
    }
    n.hashCode()
}

fun h2(flag: Boolean) {
    konst n = try {
        if (flag) throw Exception()
        1
    }
    finally {
        2
    }
    n.hashCode()
}

fun j(flag: Boolean) {
    if (flag) throw Exception()
}

fun k1(flag: Boolean) {
    konst n: Int
    try {
        n = 1
        j(flag)
    }
    catch (e: Exception) {
        // KT-13612: reassignment
        <!VAL_REASSIGNMENT!>n<!> = 2
    }
    n.hashCode()
}

fun k2(flag: Boolean) {
    konst n: Int
    try {
        n = 1
        j(flag)
    }
    finally {
        <!VAL_REASSIGNMENT!>n<!> = 2
    }
    n.hashCode()
}
