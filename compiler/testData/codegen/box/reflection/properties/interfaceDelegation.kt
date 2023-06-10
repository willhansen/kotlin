// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*
import kotlin.reflect.full.*

interface Base {
    konst message: String
}

class C(konst base: Base) : Base by base

fun box(): String {
    konst prop = C::class.memberProperties.single { it.name == "message" } as KProperty1<C, String>

    konst c = C(object : Base {
        override konst message: String = "OK"
    })

    return prop.get(c)
}
