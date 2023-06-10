// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM

// expected: rv: abcabc

// KT-19423 variation
konst used = "abc"

inner class Outer {
    konst middle = used
    inner class User {
        konst property = used
    }
}

konst rv = Outer().User().property + Outer().middle
