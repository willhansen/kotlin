// IGNORE_BACKEND: JS_IR_ES6
import kotlin.reflect.KClass

// FILE: main.kt
external abstract open class A(
    o: String
) {
    abstract konst k: String

    fun test(): String
}

class B(
    o: String
) : A(o) {
    override konst k = "K"
}

external fun test(
    klazz: Any
) : B

fun box(): String {
    return test(B::class.js).test()
}

// FILE: test.js

function test(classType) {
    return new classType("O")
}

function A(o) {
    this.o = o
}

A.prototype.test = function() {
    return this.o + this.k
}