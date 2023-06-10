fun exc(flag: Boolean) {
    if (flag) throw Exception()
}

fun f1(flag: Boolean) {
    konst n: Int
    try {
        if (flag) {
            n = 1
            exc(flag)
            return
        }
    }
    catch (e: Exception) {
        // KT-13612: reassignment
        <!VAL_REASSIGNMENT!>n<!> = 3
    }
    <!UNINITIALIZED_VARIABLE!>n<!>.hashCode()
}

fun f2(flag: Boolean) {
    while (true) {
        konst n: Int
        try {
            if (flag) {
                n = 1
                exc(flag)
                break
            }
        }
        catch (e: Exception) {
            // KT-13612: reassignment
            <!VAL_REASSIGNMENT!>n<!> = 3
        }
        <!UNINITIALIZED_VARIABLE!>n<!>.hashCode()
    }
}

fun f3(flag: Boolean) {
    while (true) {
        konst n: Int
        try {
            if (flag) {
                n = 1
                exc(flag)
                continue
            }
        }
        catch (e: Exception) {
            // KT-13612: reassignment
            <!VAL_REASSIGNMENT!>n<!> = 3
        }
        <!UNINITIALIZED_VARIABLE!>n<!>.hashCode()
    }
}
