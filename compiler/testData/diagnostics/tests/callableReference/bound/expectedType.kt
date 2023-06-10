// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

import kotlin.reflect.KClass

fun test(s: String) {
    konst f: () -> Int = s::hashCode
    konst g: () -> String = s::toString
    konst h: (Any?) -> Boolean = s::equals

    konst k: KClass<out String> = s::class
    konst l: KClass<*> = s::class
    konst m: KClass<String> = String::class
    konst n: KClass<Unit> = Unit::class
}
