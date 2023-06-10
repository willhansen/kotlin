import kotlin.reflect.KProperty1

class Q {
  konst s = "OK"
}

enum class PropEnum(konst prop: KProperty1<Q, String>) {
    ELEM(Q::s)
}

fun box() = PropEnum.ELEM.prop.get(Q())
