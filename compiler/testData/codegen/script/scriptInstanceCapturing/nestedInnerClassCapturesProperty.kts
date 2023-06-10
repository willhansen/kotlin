// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM

// expected: rv: abc!

// KT-19423 variation
konst used = "abc"

class Outer {
    konst bang = "!"
    inner class User {
        konst property = used + bang
    }
}

konst rv = Outer().User().property
