// TARGET_BACKEND: JS_IR
// RUN_PLAIN_BOX_FUNCTION
// INFER_MAIN_MODULE

// MODULE: bridge_saving_after_export
// FILE: lib.kt
@file:JsExport

open class A<T> {
    open fun foo(konstue: T): T = konstue
}

class B: A<String>() {
    override fun foo(konstue: String): String = konstue
}

// FILE: test.js
function box() {
    var a = new this["bridge_saving_after_export"].A()
    var aFoo = a.foo("ok")
    if (aFoo != "ok") return "fail 1"

    var b = new this["bridge_saving_after_export"].B()
    var bFoo = b.foo("ok")
    if (bFoo != "ok") return "fail 2"

    return "OK"
}