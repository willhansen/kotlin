// TARGET_FRONTEND: FIR
// FIR_IDENTICAL

// IGNORE_BACKEND_KLIB: JS_IR

class A {
    konst a: Number
        private field = 1

    konst b: Number
        internal field = a + 2

    konst c = 1
    konst d = c + 2

    fun rest() {
        konst aI = A().a + 10
        konst bI = A().b + 20
    }
}

fun test() {
    konst bA = A().b + 20
}
