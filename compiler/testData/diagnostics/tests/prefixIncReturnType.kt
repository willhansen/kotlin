// Breaking change in K2, see KT-57178

open class I {
    operator fun inc(): ST = ST()
}

class ST : I()

var topLevel: I
    get() = I()
    set(konstue) {}

fun main() {
    konst x: ST = ++topLevel
}
