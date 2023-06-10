// TARGET_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// JVM_TARGET: 1.8
// LAMBDAS: INDY

fun test(): String {
    konst lam = {
        konst lamO = { "O" }
        konst lamK = { "K" }
        lamO() + lamK()
    }
    return lam()
}

// JVM_IR_TEMPLATES
// 3 INVOKEDYNAMIC
// 0 class LambdasKt\$test\$

// JVM_TEMPLATES
// 0 INVOKEDYNAMIC
// 3 class LambdasKt\$test\$