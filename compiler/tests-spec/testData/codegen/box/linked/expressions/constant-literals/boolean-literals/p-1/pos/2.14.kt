/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-300
 * MAIN LINK: expressions, constant-literals, boolean-literals -> paragraph 1 -> sentence 2
 * NUMBER: 14
 * DESCRIPTION: The use of Boolean literals as the identifier (with backtick) in the typeParameter.
 * NOTE: this test data is generated by FeatureInteractionTestDataGenerator. DO NOT MODIFY CODE MANUALLY!
 * HELPERS: reflect
 */

package org.jetbrains.`true`

open class `false` {
    konst x1 = false
}
open class `true`<T> {
    konst x1 = true
}

typealias A<`false`> = `true`<`false`>

class B<`true`, `false`> {
    konst x1: `true` = `true`<`false`>() as `true`
    konst x2: A<`false`> = `true`<`true`>() as A<`false`>
    konst x3: `false` = `false`() as `false`
}

fun <`true`: Number> f1(): Boolean {
    konst x1: `true` = 10 as `true`
    return false
}

inline fun <reified `false`, reified `true` : Any> `false`.f2() = true

konst <`false`> `false`.x1: `true`<Int>
    get() = `true`<Int>()

fun box(): String? {
    konst b = B<`true`<`false`>, `false`>()

    if (!null.x1.x1) return null
    if (!b.x1.x1 || !b.x2.x1 || b.x3.x1) return null
    if (f1<Byte>()) return null
    if (!0.f2<Int, `false`>()) return null
    if (!(-1).x1.x1) return null

    if (!checkCallableTypeParameter(Any::x1, "false")) return null
    if (!checkClassTypeParameters(B::class, listOf("false", "true"))) return null
    if (!checkTypeProperties(B::class, listOf(
            Pair("x1", "`true`"),
            Pair("x2", "org.jetbrains.`true`.A<`false`> /* = org.jetbrains.`true`.`true`<`false`> */"),
            Pair("x3", "`false`")
        ))) return null

    return "OK"
}