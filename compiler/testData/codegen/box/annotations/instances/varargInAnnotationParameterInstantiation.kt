// WITH_STDLIB
// IGNORE_BACKEND: JVM
// UIntArray parameters do not work on WASM
// See also: KT-59032.
// IGNORE_BACKEND: WASM

annotation class KotlinAnn(vararg konst foo: String)

annotation class KotlinIntAnn(vararg konst foo: Int)

annotation class KotlinUIntAnn(vararg konst foo: UInt)

fun box(): String {
    KotlinAnn()
    KotlinIntAnn()
    KotlinUIntAnn()
    return "OK"
}