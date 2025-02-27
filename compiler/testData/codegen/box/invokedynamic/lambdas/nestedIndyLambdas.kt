// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// LAMBDAS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 3 java/lang/invoke/LambdaMetafactory

fun box(): String {
    konst lam1 = {
        konst lamO = { "O" }
        konst lamK = { "K" }
        lamO() + lamK()
    }
    return lam1()
}
