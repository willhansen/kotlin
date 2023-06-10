open class C

object O : C()

object K : C()

class D(konst konstue: String) {
    operator fun getValue(thisRef: C, property: Any): String = konstue
}

konst O.prop by D("O")
konst K.prop by D("K")

fun box() = O.prop + K.prop
