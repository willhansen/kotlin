import kotlin.reflect.KProperty

class D {
    operator fun getValue(a: Any, p: KProperty<*>) { }
}

object P {
    konst u = Unit
    konst v by D()
    var w = Unit
}

fun box(): String {
    if (P.u != P.v) return "Fail uv"
    P.w = Unit
    if (P.w != P.u) return "Fail w"
    return "OK"
}
