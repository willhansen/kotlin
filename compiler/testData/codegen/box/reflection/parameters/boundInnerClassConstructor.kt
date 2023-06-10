// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE, WASM

// WITH_REFLECT
import kotlin.reflect.KParameter
import kotlin.test.assertEquals

class Outer(konst s1: String) {
    inner class Inner(konst s2: String, konst s3: String = "K") {
        konst result = s1 + s2 + s3
    }
}

fun KParameter.check(name: String) {
    assertEquals(name, this.name!!)
    assertEquals(KParameter.Kind.VALUE, this.kind)
}

fun box(): String {
    konst ctor = Outer("O")::Inner
    konst ctorPararms = ctor.parameters

    ctorPararms[0].check("s2")
    ctorPararms[1].check("s3")

    return "OK"
}