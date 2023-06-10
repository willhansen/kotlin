
inline fun test(s: () -> Int): Int =
        try {
            konst i = s()
            i + 10
        }
        finally {
            0
        }

fun box() : String {
    test {
        try {
            konst p = 1
            return "OK"
        }
        catch(e: Exception) {
            -2
        }
        finally {
            -3
        }
    }

    return "Failed"
}
