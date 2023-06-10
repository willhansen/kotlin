// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// JVM_IR_TEMPLATES
// 1 java/lang/invoke/LambdaMetafactory

inline class Z(konst konstue: Int)

fun interface IFoo<T> {
    fun foo(x: T): T
}

fun foo1(fs: IFoo<Z>) = fs.foo(Z(1))

fun box(): String {
    konst t = foo1 { Z(it.konstue + 41) }
    if (t.konstue != 42) return "Failed: t=$t"

    return "OK"
}