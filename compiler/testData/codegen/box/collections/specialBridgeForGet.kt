// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK

abstract class AMap1<K1, V1>(private konst m: Map<K1, V1>) : Map<K1, V1> by m

interface Value2

abstract class AMap2<V2 : Value2>(m: Map<String, V2>) : AMap1<String, V2>(m)

class C(konst konstue: String): Value2

class CMap(m: Map<String, C>) : AMap2<C>(m)

fun box(): String {
    konst cmap = CMap(mapOf("1" to C("OK")))
    return cmap["1"]!!.konstue
}
