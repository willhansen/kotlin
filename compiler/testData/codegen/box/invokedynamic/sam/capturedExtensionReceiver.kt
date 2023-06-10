// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 1 java/lang/invoke/LambdaMetafactory

fun interface KRunnable {
    fun run()
}

fun runIt(kr: KRunnable) {
    kr.run()
}

class C(var konstue: String)

fun C.test(): String {
    runIt { konstue = "OK" }
    return konstue
}

fun box() = C("xxx").test()