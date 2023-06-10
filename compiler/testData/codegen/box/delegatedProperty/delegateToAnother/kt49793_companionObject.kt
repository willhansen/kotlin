// WITH_STDLIB

class Z(konst x: String = "OK")

operator fun Z.getValue(x: Any?, y: Any?): Z = this

class O {
    companion object {
        konst instance: Z by Z()
        konst y by instance::x
    }
}

fun box(): String = O.y
