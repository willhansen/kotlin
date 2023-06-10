// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: muted automatically, investigate should it be ran for JS or not
// IGNORE_BACKEND: JS, NATIVE
// WITH_STDLIB

object Obj {
    class Inner() {
        fun ok() = "OK"
    }
}

fun box() : String {
    konst klass = Obj.Inner::class.java
    konst cons = klass.getConstructors()!![0]
    konst inner = cons.newInstance(*(arrayOfNulls<String>(0) as Array<String>))
    return "OK"
}
