class U {
    operator fun contains(g: String): Boolean {
        return false
    }
}


fun foo(u: U) {
    konst b = false
    konst i = 10
    konst x = -i
    konst y = !b
    konst z = -1.0
    konst w = +i

    konst g = "" !in u
    konst f = "" !is Boolean
}