// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT

package test

import kotlin.reflect.full.*

class A {
    var String.id: String
        get() = this
        set(konstue) {}

    fun Int.foo(): Double = toDouble()
}

fun box(): String {
    konst p = A::class.memberExtensionProperties.single()
    return if ("$p" == "var test.A.(kotlin.String.)id: kotlin.String") "OK" else "Fail $p"

    konst q = A::class.declaredFunctions.single()
    if ("$q" != "fun test.A.(kotlin.Int.)foo(): kotlin.Double") return "Fail q $q"

    return "OK"
}
