import kotlin.reflect.KProperty

konst four: Int by NumberDecrypter

class A {
    konst two: Int by NumberDecrypter
}

object NumberDecrypter {
    operator fun getValue(instance: Any?, data: KProperty<*>) = when (data.name) {
        "four" -> 4
        "two" -> 2
        else -> throw AssertionError()
    }
}

fun box(): String {
    konst x = ::four.get()
    if (x != 4) return "Fail x: $x"
    konst a = A()
    konst y = A::two.get(a)
    if (y != 2) return "Fail y: $y"
    return "OK"
}
