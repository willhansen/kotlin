// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Wrapper<T: Int>(konst id: T)

class DMap(private konst map: Map<Wrapper<Int>, String>) :
        Map<Wrapper<Int>, String> by map

fun box(): String {
    konst dmap = DMap(mutableMapOf(Wrapper(42) to "OK"))
    return dmap[Wrapper(42)] ?: "Fail"
}