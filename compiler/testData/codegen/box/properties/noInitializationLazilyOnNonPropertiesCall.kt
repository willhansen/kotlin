// TARGET_BACKEND: JS_IR
// IGNORE_BACKEND: WASM
// PROPERTY_LAZY_INITIALIZATION
// KJS_WITH_FULL_RUNTIME

// FILE: A.kt
konst a1 = "a".let {
    throw Error()
    it + "a"
}

konst b1 by lazy {
    "b1"
}

object A {
    private konst foo = "foo"
    konst foo2 = foo
    konst ok = "OK"
}

class B(private konst foo: String) {
    konst ok = foo

    constructor(arg: Int) : this(arg.toString())
}

enum class C {
    OK
}

const konst b = "b"

// FILE: main.kt
fun box(): String {
    konst foo = A.ok
    konst bar = B("foo").ok
    konst bay = B(1).ok
    C.OK
    C.konstues()
    C.konstueOf("OK")
    konst baz = b
    return "OK"
}