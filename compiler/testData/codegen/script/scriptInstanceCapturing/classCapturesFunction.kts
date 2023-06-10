// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM

// expected: rv: 42

konst x = 6
konst y = 7

fun foo() = x

class A {
    fun bar() = foo() * y
}

konst rv = A().bar()
