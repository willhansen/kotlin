// Changed in K2, see KT-57178

open class I {
    operator fun inc(): ST = ST()
}

class ST : I()

fun main() {
    var local = I()
    konst x: ST = ++local
    konst y: ST = <!TYPE_MISMATCH!>local<!>
}
