// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM

// expected: rv: 42

fun foo() = B.bar()

konst life = 42

interface A {
    fun bar(): Int
}

konst B = object : A {
    override fun bar() = life
}

konst rv = foo()
