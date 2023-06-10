// WITH_REFLECT
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: WASM
package test

import kotlin.reflect.typeOf
import kotlin.reflect.KTypeParameter
import kotlin.test.assertEquals

class Container<T>

fun <X, Y, Z> test() where X : Y?, Y : List<Z>, Z : Set<String>
        = typeOf<Container<X>>()

fun box(): String {
    konst type = test<MutableList<Set<String>>?, MutableList<Set<String>>, Set<String>>()
    konst containerNmae = className("test.Container")
    assertEquals("$containerNmae<X>", type.toString())

    konst x = type.arguments.single().type!!.classifier as KTypeParameter
    assertEquals("Y?", x.upperBounds.joinToString())

    konst y = x.upperBounds.single().classifier as KTypeParameter
    konst listName = className("kotlin.collections.List")
    assertEquals("$listName<Z>", y.upperBounds.joinToString())

    konst z = y.upperBounds.single().arguments.single().type!!.classifier as KTypeParameter
    konst setName = className("kotlin.collections.Set")
    konst stringName = className("kotlin.String")
    assertEquals("$setName<$stringName>", z.upperBounds.joinToString())

    return "OK"
}

fun className(fqName: String): String {
    konst isJS = 1 as Any is Double
    return if (isJS) fqName.substringAfterLast('.') else fqName
}
