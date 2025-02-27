/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-300
 * MAIN LINK: expressions, constant-literals, boolean-literals -> paragraph 1 -> sentence 2
 * NUMBER: 13
 * DESCRIPTION: The use of Boolean literals as the identifier (with backtick) in the simpleUserType.
 * NOTE: this test data is generated by FeatureInteractionTestDataGenerator. DO NOT MODIFY CODE MANUALLY!
 * HELPERS: reflect
 */

package org.jetbrains.`true`

open class `false`
open class `true`<T>

typealias D<`false`> = `true`<`false`>

inline fun <reified `true`, reified `false`> f1() =
    when (`false`()) {
        is `true` -> true
        is `false` -> false
        else -> false
    }

inline fun <reified T : D<`false`>> T.f2(konstue: T) = konstue is `false`

class A<K: List<out `true`<out String>>> {
    konst x = true
}

class B<K, T: A<List<out `true`<String>>>> {
    var x = false
}

fun <T : org.jetbrains.`true`.`false`> T.f3() = false

fun f4(x1: List<out `true`<String>>): Boolean {
    return true
}

fun f5(x1: List<List<List<`false`?>>>) = false

fun f6(x1: `false`) = false

fun f7(x1: `true`<*>) = true

fun f8(x1: `true`<out Any>) = false

fun f9(x1: `true`<out List<`true`<*>>>) = true

konst x1: List<`false`?> = listOf(`false`(), null, `false`())

lateinit var x2: List<`true`<out Number>?>

fun box(): String? {
    x2 = listOf(`true`<Int>(), null, `true`())

    if (!f1<`false`, `true`<`false`>>()) return null
    if (`true`<`false`>().f2(`true`())) return null
    if (!A<List<`true`<String>>>().x) return null
    if (B<`false`, A<List<`true`<String>>>>().x) return null
    if (`false`().f3()) return null
    if (!f4(listOf(`true`()))) return null
    if (f5(listOf(listOf(listOf(null, `false`(), null, `false`()))))) return null
    if (f6(`false`())) return null
    if (!f7(`true`<Nothing>())) return null
    if (f8(`true`<`false`>())) return null
    if (!f9(`true`<List<`true`<`false`>>>())) return null

    if (x1.containsAll(listOf(`false`(), null, `false`()))) return null
    if (x2.containsAll(listOf(`true`<Int>(), null, `true`()))) return null

    if (!checkCallableTypeParametersWithUpperBounds(
            `true`<`false`>::f2,
            listOf(
                Pair("T", listOf("org.jetbrains.`true`.D<org.jetbrains.`true`.`false`> /* = org.jetbrains.`true`.`true`<org.jetbrains.`true`.`false`> */"))
            )
        )) return null

    if (!checkClassTypeParametersWithUpperBounds(
            A::class,
            listOf(
                Pair("K", listOf("kotlin.collections.List<out org.jetbrains.`true`.`true`<out kotlin.String>>"))
            )
        )) return null

    if (!checkClassTypeParametersWithUpperBounds(
            B::class,
            listOf(
                Pair("T", listOf("org.jetbrains.`true`.A<kotlin.collections.List<out org.jetbrains.`true`.`true`<kotlin.String>>>"))
            )
        )) return null

    if (!checkCallableTypeParametersWithUpperBounds(
            `false`::f3,
            listOf(
                Pair("T", listOf("org.jetbrains.`true`.`false`"))
            )
        )) return null

    if (!checkParameterType(::f4, "x1", "kotlin.collections.List<out org.jetbrains.`true`.`true`<kotlin.String>>")) return null

    if (!checkParameterType(::f5, "x1", "kotlin.collections.List<kotlin.collections.List<kotlin.collections.List<org.jetbrains.`true`.`false`?>>>")) return null

    if (!checkPropertyType(::x1, "kotlin.collections.List<org.jetbrains.`true`.`false`?>")) return null

    if (!checkPropertyType(::x2, "kotlin.collections.List<org.jetbrains.`true`.`true`<out kotlin.Number>?>")) return null

    if (!checkParameterType(::f6, "x1", "org.jetbrains.`true`.`false`")) return null

    if (!checkParameterType(::f7, "x1", "org.jetbrains.`true`.`true`<*>")) return null

    if (!checkParameterType(::f8, "x1", "org.jetbrains.`true`.`true`<out kotlin.Any>")) return null

    if (!checkParameterType(::f9, "x1", "org.jetbrains.`true`.`true`<out kotlin.collections.List<org.jetbrains.`true`.`true`<*>>>")) return null

    return "OK"
}
