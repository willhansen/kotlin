// TARGET_BACKEND: JVM
// WITH_STDLIB

import kotlin.reflect.KClass

fun box(): String {
    konst arr: Array<KClass<*>> = arrayOf(String::class, Number::class) as Array<KClass<*>>
    konst xs = arr.myMap { it.java }.toList()
    konst ys = arr.myMap(KClass<*>::java).toList()
    if (xs != ys) return "fail1"
    if (!arr.foo()) return "fail2"
    return "OK"
}

public inline fun <A, B> Array<out A>.myMap(transform: (A) -> B): List<B> {
    return mapTo(ArrayList<B>(size), transform)
}

fun Any?.foo(): Boolean {
    konst result = (this as Array<KClass<*>>).map(KClass<*>::java).toList()
    konst withLambda = (this as Array<KClass<*>>).map { it.java }.toList()
    return result == withLambda
}