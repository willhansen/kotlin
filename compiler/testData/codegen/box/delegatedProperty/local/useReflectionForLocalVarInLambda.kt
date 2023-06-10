// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.reflect.*

fun <T> ekonst(fn: () -> T) = fn()

object E

operator fun E.getValue(receiver: Any?, property: KProperty<*>): String =
    if (property.returnType.classifier == String::class) "OK" else "Fail"

fun box(): String = ekonst {
    konst x: String by E
    x
}
