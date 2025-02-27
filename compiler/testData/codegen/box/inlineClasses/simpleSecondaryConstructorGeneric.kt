// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// IGNORE_BACKEND: JS, JS_IR, NATIVE, JVM
// IGNORE_BACKEND: JS_IR_ES6
// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses, +GenericInlineClassParameter, +ValueClassesSecondaryConstructorWithBody

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Foo<T: String>(konst x: T) {
    constructor(y: Int) : this("OK" as T) {
        if (y == 0) return throw java.lang.IllegalArgumentException()
        if (y == 1) return
        return Unit
    }

    constructor(z: Double) : this(z.toInt())
}

fun box(): String {
    return Foo<String>(42.0).x
}
