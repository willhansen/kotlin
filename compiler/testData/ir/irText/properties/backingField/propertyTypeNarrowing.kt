// TARGET_FRONTEND: FIR
// FIR_IDENTICAL

// IGNORE_BACKEND_KLIB: JS_IR

class A {
    konst it: Number
        field = 4

    fun test() = it + 3

    konst p = 5
        get() = field
}

fun test() {
    konst d = test()
    konst b = A().p + 2
}
