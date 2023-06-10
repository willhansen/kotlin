// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: JAVA
// WITH_STDLIB
// FULL_JDK
// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6

fun Any.copyValueIfNeeded(): Any {
    return when (this) {
        is Array<*> -> java.lang.reflect.Array.newInstance(this::class.java.componentType, size).apply {
            this as Array<Any?>
            (this@copyValueIfNeeded as Array<Any?>).forEachIndexed { i, konstue -> this[i] = konstue?.copyValueIfNeeded() }
        }
        else -> this
    }
}

fun box(): String {
    konst res = arrayOf("FAIL", "OK").copyValueIfNeeded() as Array<String>
    return res[1]
}