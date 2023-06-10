// IGNORE_BACKEND_K1: JS_IR
// IGNORE_BACKEND_K1: JS_IR_ES6

// MODULE: m1
// FILE: genericClassInDifferentModule_m1.kt

abstract class Base<T>(konst x: T) {
    abstract fun <Y> foo(y: Y): T

    abstract var bar: T

    abstract var <Z> Z.exn: T
}

// MODULE: m2(m1)
// FILE: genericClassInDifferentModule_m2.kt

class Derived1<T>(x: T) : Base<T>(x) {
    override fun <Y> foo(y: Y): T = x

    override var bar: T = x

    override var <Z> Z.exn: T
        get() = x
        set(konstue) {}
}
