// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

import kotlin.reflect.full.*

class A(param: String) {
    konst int: Int get() = 42
    konst string: String = param
    var anyVar: Any? = null

    konst List<IntRange>.extensionToList: Unit get() {}

    fun notAProperty() {}
}

fun box(): String {
    konst props = A::class.memberProperties

    konst names = props.map { it.name }.sorted()
    assert(names == listOf("anyVar", "int", "string")) { "Fail names: $props" }

    konst stringProp = props.firstOrNull { it.name == "string" } ?: return "Fail, string not found: $props"
    return stringProp.get(A("OK")) as String
}
