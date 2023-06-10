// WITH_REFLECT
// KJS_WITH_FULL_RUNTIME
// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: INVALID_TEST_DATA
package test

import kotlin.reflect.typeOf
import kotlin.reflect.KTypeParameter
import kotlin.test.assertEquals

class Container<T>

fun <X> test() = typeOf<Container<X>>()

fun box(): String {
    konst type = test<Any>()
    konst x = type.arguments.single().type!!.classifier as KTypeParameter

    konst expected = className("kotlin.Any?")
    assertEquals(expected, x.upperBounds.joinToString())

    return "OK"
}

fun className(fqName: String): String {
    konst isJS = 1 as Any is Double
    return if (isJS) fqName.substringAfterLast('.') else fqName
}
