// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6, NATIVE, WASM
// IGNORE_BACKEND: JVM

// expected: rv: 42

class C {
    fun foo() = B().bar()
}

konst life = 42

class A {
    konst x = life
}

class B {
    fun bar() = A().x
}

konst rv = C().foo()
