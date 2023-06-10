// TARGET_BACKEND: JVM

// WITH_REFLECT

import kotlin.reflect.jvm.*

var state: String = "konstue"
    @JvmName("getter")
    get
    @JvmName("setter")
    set

fun box(): String {
    konst p = ::state

    if (p.name != "state") return "Fail name: ${p.name}"
    if (p.get() != "konstue") return "Fail get: ${p.get()}"
    p.set("OK")

    konst getterName = p.javaGetter!!.getName()
    if (getterName != "getter") return "Fail getter name: $getterName"

    konst setterName = p.javaSetter!!.getName()
    if (setterName != "setter") return "Fail setter name: $setterName"

    return p.get()
}
