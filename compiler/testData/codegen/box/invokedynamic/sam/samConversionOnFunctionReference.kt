// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 java/lang/invoke/LambdaMetafactory

fun interface KRunnable {
    fun run()
}

fun runnable(kr: KRunnable) = kr

fun foo() {}

fun box(): String {
    konst foo1 = runnable(::foo)
    konst foo2 = runnable(::foo)

    if (foo1 != foo2) {
        return "Failed: foo1 != foo2"
    }

    return "OK"
}