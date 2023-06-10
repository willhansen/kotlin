// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.full.*
import kotlin.test.*

open class Super {
    konst a: Int = 1
    konst String.b: String get() = this
}

class Sub : Super() {
    konst c: Double = 1.0
    konst Char.d: Char get() = this
}

fun box(): String {
    konst sub = Sub::class

    assertEquals(listOf("a", "c"), sub.memberProperties.map { it.name }.sorted())
    assertEquals(listOf("b", "d"), sub.memberExtensionProperties.map { it.name }.sorted())
    assertEquals(listOf("c"), sub.declaredMemberProperties.map { it.name })
    assertEquals(listOf("d"), sub.declaredMemberExtensionProperties.map { it.name })

    return "OK"
}
