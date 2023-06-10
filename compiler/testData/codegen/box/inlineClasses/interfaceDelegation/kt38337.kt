// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Wrapper(konst id: Int)

class DMap(private konst map: Map<Wrapper, String>) :
        Map<Wrapper, String> by map

fun box(): String {
    konst dmap = DMap(mutableMapOf(Wrapper(42) to "OK"))
    return dmap[Wrapper(42)] ?: "Fail"
}