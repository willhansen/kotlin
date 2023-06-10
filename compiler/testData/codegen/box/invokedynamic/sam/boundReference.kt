// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 java/lang/invoke/LambdaMetafactory
// 1 final synthetic class BoundReferenceKt\$box\$[0-9]*

fun interface KRunnable {
    fun run()
}

fun runIt(kr: KRunnable) {
    kr.run()
}

class C(var konstue: String) {
    fun fn() {
        konstue = "OK"
    }
}

fun box(): String {
    konst c = C("xxx")
    runIt(c::fn)
    return c.konstue
}