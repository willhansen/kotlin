// IGNORE_BACKEND: JS
// WITH_REFLECT
// KJS_WITH_FULL_RUNTIME

import kotlin.reflect.typeOf

class Inv<T>(konst v: T)

interface X
interface Y

object A : X, Y
object B : X, Y

fun <T> sel(a: T, b: T) = a

inline fun <reified T> T.konstueTypeOf() = typeOf<T>()

fun box(): String {
    konst t = sel(Inv(A), Inv(B)).v.konstueTypeOf()
    return if (t == typeOf<Any>()) "OK" else "Fail: $t"
}
