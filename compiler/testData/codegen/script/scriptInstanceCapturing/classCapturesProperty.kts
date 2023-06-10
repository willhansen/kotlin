// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM

// expected: rv: abc

// KT-19423
konst used = "abc"
class User {
    konst property = used
}

konst rv = User().property
