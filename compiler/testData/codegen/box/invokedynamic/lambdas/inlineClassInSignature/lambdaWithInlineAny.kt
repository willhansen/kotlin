// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// LAMBDAS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 java/lang/invoke/LambdaMetafactory
// 1 class LambdaWithInlineAnyKt\$box\$t\$1

inline class Z(konst konstue: Any)

fun foo1(fs: (Z) -> Z) = fs(Z(1))

fun box(): String {
    konst t = foo1 { Z((it.konstue as Int) + 41) }
    if (t.konstue != 42) return "Failed: t=$t"

    return "OK"
}
