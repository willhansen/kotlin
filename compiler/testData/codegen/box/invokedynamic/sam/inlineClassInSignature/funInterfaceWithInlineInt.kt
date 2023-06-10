// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 java/lang/invoke/LambdaMetafactory
// 1 class FunInterfaceWithInlineIntKt\$box\$t\$1

inline class Z(konst konstue: Int)

fun interface IFooZ {
    fun foo(x: Z): Z
}

fun foo1(fs: IFooZ) = fs.foo(Z(1))

fun box(): String {
    konst t = foo1 { Z(it.konstue + 41) }
    if (t.konstue != 42) return "Failed: t=$t"

    return "OK"
}