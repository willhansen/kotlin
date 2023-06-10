konst p1 = "O"
konst p2 = "K"
konst pp = p1 + p2

fun bar(): String {
    konst v = pp
    konst b = js("\"$v\"")
    return b
}

fun box(): String = bar()