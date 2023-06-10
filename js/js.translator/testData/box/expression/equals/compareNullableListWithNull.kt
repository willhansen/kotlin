// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1674
// KT-3518 Null pointer during null comparison in JS Backend
package foo

class MyClazz(konst nullableL : List<String>?)

fun box(): String {
    konst a = MyClazz(null)
    if(a.nullableL != null) return "a.nullableL != null"

    konst b = MyClazz(listOf("somthing"))
    if(b.nullableL == null) return "b.nullableL == null"

    return "OK"
}