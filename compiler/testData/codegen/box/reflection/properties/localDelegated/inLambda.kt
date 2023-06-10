// TARGET_BACKEND: JVM
// WITH_REFLECT
import kotlin.reflect.*

fun <T> ekonst(fn: () -> T) = fn()

inline operator fun String.getValue(t:Any?, p: KProperty<*>): String =
    if (p.returnType.classifier == String::class) this else "fail"

fun box() = ekonst {
    konst x by "OK"
    x
}
