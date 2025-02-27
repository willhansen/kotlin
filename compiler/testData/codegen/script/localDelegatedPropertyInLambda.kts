// WITH_REFLECT

import kotlin.reflect.KProperty

class Delegate {
    operator fun getValue(t: Any?, p: KProperty<*>): String =
        if (p.returnType.toString() == "kotlin.String") "OK" else "Fail: ${p.returnType}"
}

fun f(lambda: () -> String): String = lambda()

konst x = f {
    konst prop: String by Delegate()
    prop
}

// expected: x: OK
