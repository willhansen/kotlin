// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// IGNORE_BACKEND: JVM, JVM_IR

// expected: rv: 42

fun foo() = B().bar()

konst life = 42

interface A {
    konst x get() = life
}

class B : A {
    fun bar() = x
}

konst rv = foo()
