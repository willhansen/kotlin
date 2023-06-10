// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: FUNCTION_REFERENCES
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// TODO: investigate should it be ran for JS or not
// IGNORE_BACKEND: JS

package test

class A {
    fun foo() {}
    fun bar() {}
}

konst a = A()
konst aa = A()

konst aFoo = a::foo
konst aBar = a::bar
konst aaFoo = aa::foo
konst A_foo = A::foo

fun box(): String =
        when {
            aFoo != a::foo -> "Bound refs with same receiver SHOULD be equal"
            aFoo == aBar -> "Bound refs to different members SHOULD NOT be equal"
            aFoo == aaFoo -> "Bound refs with different receiver SHOULD NOT be equal"
            aFoo == A_foo -> "Bound ref SHOULD NOT be equal to free ref"

            else -> "OK"
        }
