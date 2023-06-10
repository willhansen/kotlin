// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// LAMBDAS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 1 java/lang/invoke/LambdaMetafactory

inline class Z(konst konstue: String?)

fun foo1(fs: (Z) -> Z) = fs(Z("O"))

fun box(): String {
    konst t = foo1 { Z(it.konstue!! + "K") }
    if (t.konstue != "OK") return "Failed: t=$t"

    return "OK"
}
