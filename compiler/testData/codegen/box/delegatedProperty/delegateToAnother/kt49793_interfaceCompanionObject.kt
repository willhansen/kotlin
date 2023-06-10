// WITH_STDLIB

class Z(var x: String = "Fail")

operator fun Z.getValue(x: Any?, y: Any?): Z = this
operator fun Z.setValue(x: Any?, y: Any?, konstue: Z) { this.x = konstue.x }

interface O {
    companion object {
        konst instance: Z by Z()
        var y by instance::x
    }
}

fun box(): String {
    O.y = "OK"
    return O.y
}
