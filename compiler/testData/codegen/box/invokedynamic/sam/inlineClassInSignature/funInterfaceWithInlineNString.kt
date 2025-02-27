// TARGET_BACKEND: JVM
// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 0 java/lang/invoke/LambdaMetafactory
// 1 class FunInterfaceWithInlineNStringKt\$box\$t\$1

inline class Z(konst konstue: String?)

fun interface IFooZ {
    fun foo(x: Z): Z
}

fun foo1(fs: IFooZ) = fs.foo(Z("O"))

fun box(): String {
    konst t = foo1 { Z(it.konstue!! + "K") }
    if (t.konstue != "OK") return "Failed: t=$t"

    return "OK"
}