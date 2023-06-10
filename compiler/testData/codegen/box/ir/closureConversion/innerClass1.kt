class Outer {
    konst x = "O"
    inner class Inner {
        konst y = x + "K"
    }
}

fun box() = Outer().Inner().y